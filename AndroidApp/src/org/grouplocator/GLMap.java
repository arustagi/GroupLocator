package org.grouplocator;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import pubnub.android.Callback;
import pubnub.android.Pubnub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.facebook.android.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class GLMap extends MapActivity {
    private GLMap me;
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private MyLocationOverlay myLocationOverlay;
    private int loc_range=2;
    private int loc_time=2*1000;
	private int group_id;
    private String channel_name;
    private String group_name;
    private String user_name;
	
	
	private Button btn_send; 
    private MultiAutoCompleteTextView txt_msg;
    private TextView view_msg;
    private static Pubnub pubnub = null;
    
    MessageHandler mMessageHandler;
    MessageReceiver mMessageReceiver;
	MessageListener mMessageListener;
	

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        me = this;
        

        if (pubnub == null){
        	pubnub = new Pubnub(Utility.PUBNUB_PUBLISH_KEY, // PUBLISH_KEY
                    Utility.PUBNUB_SUBSCRIBE_KEY, // SUBSCRIBE_KEY
                    Utility.PUBNUB_SECRET_KEY, // SECRET_KEY
                    false // SSL_ON?
            );
        }
    	Bundle extras = getIntent().getExtras(); 
    	group_id = extras.getInt("channel_id");
    	Log.e("debug","Error occurred while loading seetings" + group_id);
    	
        mMessageHandler = new MessageHandler();
        mMessageReceiver = new MessageReceiver();
    	mMessageListener = new MessageListener();
    	
        
        btn_send = (Button)findViewById(R.id.btn_send);
        txt_msg = (MultiAutoCompleteTextView)findViewById(R.id.txt_msg);
        view_msg = (TextView)findViewById(R.id.view_msg);
        view_msg.setText("");
        txt_msg.setMaxLines(1);
        
        TabHost tabHost;
        tabHost=(TabHost)findViewById(R.id.tabHost);
	    tabHost.setup();

	    TabSpec spec1=tabHost.newTabSpec("map");
	    spec1.setIndicator("Map");
	    spec1.setContent(R.id.tab_map);
	    
	    TabSpec spec2=tabHost.newTabSpec("chat");
	    spec2.setIndicator("Chat");
	    spec2.setContent(R.id.tab_chat);
	    
	    tabHost.addTab(spec1);
	    tabHost.addTab(spec2);
	            
        
        mapView = (MapView) findViewById(R.id.mapview);       
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
       
        btn_send.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	    	publishMessage(txt_msg.getText().toString());
    	    	txt_msg.setText("");
    	    }
    	});
		
   	 locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	  
    //Better solution would be to display a dialog and suggesting to 
    // go to the settings
    if (!enabled) {
    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    	startActivity(intent);
    }
    
    mapController = mapView.getController();
	
	myLocationOverlay = new MyLocationOverlay(this, mapView);
	mapView.getOverlays().add(myLocationOverlay);
	new GLHttpRequest(me) {
		@Override
		protected void onPostExecute(JSONObject result) {
			try {
					if (result.getString("loc_frequency")
							.toUpperCase().contentEquals("H"))
						loc_time = 2 * 1000 ;
					else if (result.getString("loc_frequency")
							.toUpperCase().contentEquals("M"))
						loc_time = 10 * 1000 ;
					else if (result.getString("loc_frequency")
							.toUpperCase().contentEquals("L"))
						loc_time = 30 * 1000 ;

					if (result.getString("loc_range")
							.toUpperCase().contentEquals("H"))
						loc_range = 2;
					else if (result.getString("loc_range")
							.toUpperCase().contentEquals("M"))
						loc_range = 20;
					else if (result.getString("loc_range")
							.toUpperCase().contentEquals("L"))
						loc_range = 50;

			} catch (Exception e) {
				Log.e("debug","Error occurred while loading seetings");
			}
			afterInit();
		}
	}.settings();
	
	}
    
    
    public void afterInit(){

		new GLHttpRequest(me) {
			@Override
			protected void onPostExecute(JSONObject result) {
				try {
					channel_name = result.getString("channel_name");
					group_name = result.getString("name");
					user_name = result.getString("user_name");
					
			        mMessageListener.execute(channel_name);
					_startService(channel_name);
		
				} catch (Exception e) {
					Log.e("debug","Error occurred while loading group details");
				}
			}
		}.get_group(String.valueOf(group_id));
		
		
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    

    @Override
    public void onDestroy() {
    	try{
    		locationManager.removeUpdates(locationListener);
    		mMessageListener.cancel(true);
    	}catch(Exception e){
    		
    	}
	    super.onDestroy();
    }


    private Map<Integer, MyOverlays> user_overlays= new HashMap<Integer, MyOverlays> (); 
    private Map<Integer, Integer> user_color= new HashMap<Integer, Integer> ();
    private Map<Integer, String> user_names= new HashMap<Integer, String> ();
    
    public void processLocationMessage(GeoPoint point, Integer user_id){
    	
    	if (user_overlays.size() ==0){
    		mapController.animateTo(point);
    		mapController.setCenter(point);
    		mapController.setZoom(14); // Zoon 1 is world view
    	}
    	
    	List<Overlay> overlays = mapView.getOverlays();
    	if(user_overlays.containsKey(user_id)){
    		overlays.remove(user_overlays.get(user_id));
    		user_overlays.remove(user_id);
    	}

    	
    	user_overlays.put(user_id, new MyOverlays( point, user_color.get(user_id)));
    	overlays.add(user_overlays.get(user_id));
    	mapView.invalidate();
    }
    
    static int last_user_id = -1; 
    public void postMessage(String msg, Integer user_id){
    	if (last_user_id == user_id)
    		view_msg.append(Html.fromHtml("<font color=\"" + user_color.get(user_id) + "\">"+msg+"</font><br />"));
    	else
    		view_msg.append(Html.fromHtml("<font color=\"" + user_color.get(user_id) + "\"><b>"+user_names.get(user_id) + "</b> : " + msg+"</font><br />"));
    	last_user_id = user_id;
    
    }
    
    public void publishMessage(final String message){
    	Log.e(getClass().getSimpleName(), "publishing message inside");
    	if(pubnub == null){
    		Log.e(getClass().getSimpleName(), "pubnub not exisits");
    		return;
    	}
    	
    	Thread mMessageSender = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                	final JSONObject json = new JSONObject();
                    try { 
                    	json.put( "member_id", Utility.get_GL_ID());
                    	json.put( "message", message);
                    	json.put( "user_name", me.user_name);
                    }
                    catch (org.json.JSONException jsonError) {}
                    // Publish Message
                    JSONArray info = pubnub.publish(
                    	channel_name, 		// Channel Name
                        json    	// JSON Message
                    );
                    // Print Response from PubNub JSONP REST Service
                    Log.e(getClass().getSimpleName(), "publishing" + "channel" + " " + info.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    	mMessageSender.start();
    }
    

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	stopService();
        }
        return super.onKeyDown(keyCode, event);
    }

    
    
    public void stopService(){
    	if(pubnub == null){
    		Log.e(getClass().getSimpleName(), "pubnub not exisits");
    		return;
    	}
    	
    	Thread mMessageSender = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                	final JSONObject json = new JSONObject();
                    try { 
                    	json.put( "member_id", Utility.get_GL_ID());
                    	json.put( "kill_me", "kill_me");
                    	json.put( "user_name", me.user_name);
                    }
                    catch (org.json.JSONException jsonError) {}
                    // Publish Message
                    JSONArray info = pubnub.publish(
                    		channel_name, 		// Channel Name
                        json    	// JSON Message
                    );
                    // Print Response from PubNub JSONP REST Service
                    Log.e(getClass().getSimpleName(), "publishing" + "channel" + " " + info.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    	mMessageSender.start();
    	
    	while(!stop){
    		
    	}
    }
    
    public void createUser(JSONObject json){
    	try{
    		int user_id = json.getInt("member_id");
        	String user_name =json.getString("user_name"); 
        	
        	if(! user_names.containsKey(user_id)){
        		user_names.put(user_id, user_name);
        	}
        	if(! user_color.containsKey(user_id)){
        		switch(user_color.size()){
    			case 0 : user_color.put(user_id, Color.BLUE);
    			break;
    			case 1 : user_color.put(user_id, Color.CYAN);
    			break;
    			case 2 : user_color.put(user_id, Color.MAGENTA);
    			break;
    			case 3 : user_color.put(user_id, Color.DKGRAY);
    			break;
    			case 4 : user_color.put(user_id, Color.RED);
    			break;
    			case 5 : user_color.put(user_id, Color.GREEN);
    			break;
        		}
    		}
        		
    	}catch(Exception e){
    		Log.e("debug","Error occurred while creating user");
    		e.printStackTrace();
    		
    	}
    	
    }
    
    public void processIt(JSONObject json){
    	try {

    	Log.e(getClass().getSimpleName(), json.toString());
		int user_id = Integer.parseInt( json.getString("member_id"));
		if(!user_color.containsKey(user_id))
			createUser(json);
		
		
		if (json.has("isLocation")){
    		if(json.getString("isLocation").compareTo("1") == 0){
    			int lat = (int) (json.getDouble("lat") * 1E6);
    			int lng = (int) (json.getDouble("lon") * 1E6);
    			GeoPoint point = new GeoPoint(lat, lng);
    			processLocationMessage(point, user_id);
    		}
		}
		if (json.has("message")){
			postMessage(json.getString("message"), user_id);
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}

    }
    

	
	   class MessageHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	            try {
	                String m = msg.getData().getString("message");
	                JSONObject message = (JSONObject) new JSONTokener(m).nextValue();
	                processIt(message);
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }

	        }

	    };
	
	 public static boolean stop = false;
 // Callback Interface when a Message is Received
    class MessageReceiver implements Callback {
        public boolean execute(JSONObject json) {
        	try {
                Message m = Message.obtain();
                Bundle b = new Bundle();
                b.putString("message", json.toString());
                m.setData(b);
                mMessageHandler.sendMessage(m);
                if (json.has("kill_me")){
                	if(Utility.get_GL_ID().contains(json.getString("member_id"))){
                		stop = true;
                		return false;
                	}
                		
        		}
            } catch (Exception e) {
                e.printStackTrace();
            }
        	return true;
        }
    }
    
    class MessageListener extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            {
                try {
                	Log.e(getClass().getSimpleName(), "subscribing" + params[0]);
                	pubnub.subscribe(params[0], mMessageReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return Boolean.TRUE;
        }
    }
    
    
    
    
    
    LocationListener locationListener;
    private void _startService(final String channel) {
    	// Acquire a reference to the system Location Manager
    	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	// Define a listener that responds to location updates
    	locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	    	// Called when a new location is found by the network location provider.
    	    	Log.e(getClass().getSimpleName(), "Location changed!!!");
    	    	
    	    	final JSONObject message = new JSONObject();
                try { 
                	message.put( "member_id", Utility.get_GL_ID());
                	message.put( "isLocation", "1");
                	message.put( "lat", location.getLatitude());
                	message.put( "lon", location.getLongitude());
                	message.put( "user_name", me.user_name);
                }
                catch (org.json.JSONException jsonError) {}
    	    	Thread mMessageSender = new Thread(new Runnable(){
    	            @Override
    	            public void run() {
    	                try {
    	                	Log.e(getClass().getSimpleName(), "Inside message sender");                	
    	                    
    	                    // Publish Message
    	                    JSONArray info = pubnub.publish(
    	                    	channel, 		// Channel Name
    	                        message        	// JSON Message
    	                    );
    	                    // Print Response from PubNub JSONP REST Service
    	                    Log.e(getClass().getSimpleName(), "publishing" + channel + " " + info.toString());
    	                } catch (Exception e) {
    	                    e.printStackTrace();
    	                }
    	            }
    	        });
    	    	mMessageSender.start();
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.loc_time, this.loc_range, locationListener);
	}
    
}