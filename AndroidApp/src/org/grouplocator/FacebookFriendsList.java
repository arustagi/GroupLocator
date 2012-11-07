package org.grouplocator;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FacebookFriendsList extends Activity implements OnItemClickListener {
    private Handler mHandler;

    protected ListView friendsList;
    protected static JSONArray jsonArray;
    protected String selectedFriends = "";
    protected String selectedFriendNames = "";
    /*
     * Layout the friends' list
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        setContentView(R.layout.friends_list);

        Bundle extras = getIntent().getExtras();
        String apiResponse = extras.getString("API_RESPONSE");
        try {
        	jsonArray = new JSONObject(apiResponse).getJSONArray("data");
        } catch (JSONException e) {
            showToast("Error: " + e.getMessage());
            return;
        }
        
        int length = jsonArray.length();
        List<String> listContents = new ArrayList<String>(length);
        for (int i = 0; i < length; i++){
        	try{
            	listContents.add(jsonArray.getJSONObject(i).getString("name"));	
        	}
        	catch(Exception e){
        		
        	}
        	
        }
        friendsList = (ListView) findViewById(R.id.friends_list);
        friendsList.setOnItemClickListener(this);
        friendsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContents));
    }

    /*
     * Clicking on a friend should popup a dialog for user to post on friend's
     * wall.
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        try {
            final long friendId;
            
            friendId = jsonArray.getJSONObject(position).getLong("id");
            final String name = jsonArray.getJSONObject(position).getString("name");

            new AlertDialog.Builder(this).setTitle("Invite")
                    .setMessage("Do you want to add "+name+"?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	selectedFriends += String .valueOf(friendId) + "*" ;
                        	selectedFriendNames += name + "\n";
                        }

                    }).setNegativeButton("No", null).show();
        } catch (JSONException e) {
            showToast("Error: " + e.getMessage());
        }
    }
        
    public void showToast(final String msg) {
    	mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(FacebookFriendsList.this, msg, Toast.LENGTH_LONG);
                toast.show();
            }
    	});
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	Intent resultIntent = new Intent();
        	resultIntent.putExtra("friendsList", this.selectedFriends);
        	resultIntent.putExtra("friendsNames", this.selectedFriendNames);
        	setResult(Activity.RESULT_OK, resultIntent);
        	finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
