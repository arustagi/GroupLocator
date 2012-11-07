package org.grouplocator;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Invitations extends Activity implements OnItemClickListener{
	private static EfficientAdapter adap;
	private static Invitations me;
	static class Group{
		int id;
		String name;
		String invited_by;
		String message;
	}
	private static ArrayList<Group> data;
	private ListView ls; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invitations_layout);
	
		ls = (ListView) findViewById(R.id.list_custom);

		me = this;
		data = new ArrayList<Group>();
		new GLHttpRequest(this){
    		@Override
    		protected void onPostExecute(JSONObject result) {
    			try
    			{

    				JSONArray groups = result.getJSONArray("groups");

    				for( int i =0; i<groups.length(); i++){
    					 JSONObject group = groups.getJSONObject(i);
    					 Group g = new Group();
    					 g.id=group.getInt("id");
    					 g.name=group.getString("name");
    					 g.invited_by = group.getString("invited_by");
    					 g.message = group.getString("message");
    					 data.add(g);
    				}
    				adap = new EfficientAdapter(me);
    				ls.setAdapter(adap);
    				ls.setOnItemClickListener(me);
    				
    					
    			}catch (Exception e){
    				
    			}
    		}
    	}.get_groups(String.valueOf(Utility.get_GL_ID()), "I");	

	}
	
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	data.clear();
        	finish();
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	 
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) 
	{
		final int pos = position;
		new AlertDialog.Builder(this).setTitle(data.get(pos).name)
         .setMessage("Accept or Decline the invitaion. Press back button to cancel.")
         .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
            	 new GLHttpRequest(me){
     	    		@Override
     	    		protected void onPostExecute(JSONObject result) {
     	    			if (!result.has("success"))
     	    				Toast.makeText(me, "Sorry!! Request not processed.", Toast.LENGTH_SHORT).show();
     	    			else{
     	    				data.remove(pos);
     	    				adap.notifyDataSetChanged();
     	    			}
     	    		}
     	    	}.update_group_status(String.valueOf(data.get(pos).id), "A");
             }
         }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
              	 new GLHttpRequest(me){
      	    		@Override
      	    		protected void onPostExecute(JSONObject result) {
      	    			if (!result.has("success"))
      	    				Toast.makeText(me, "Sorry!! Request not processed.", Toast.LENGTH_SHORT).show();
      	    			else{
      	    				data.remove(pos);
      	    				adap.notifyDataSetChanged();
      	    			}
      	    		}
      	    	}.update_group_status(String.valueOf(data.get(pos).id), "D");
             }
          })
         .show();
	}
	
	
	public static class EfficientAdapter extends BaseAdapter implements Filterable {
		private LayoutInflater mInflater;
		//private Context context;

		public EfficientAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
			//this.context = context;
		}

		/**
		 * Make a view to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.custom_list_view, null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.textLine = (TextView) convertView.findViewById(R.id.txt_cl_group_name);
				holder.invitedBy = (TextView) convertView.findViewById(R.id.txt_cl_group_invitor);
				holder.message = (TextView) convertView.findViewById(R.id.txt_cl_group_message);
//
//				convertView.setOnClickListener(new OnClickListener() {
//					private int pos = position;
//
//					@Override
//					public void onClick(View v) {
//						Toast.makeText(context, "Click-" + String.valueOf(pos),
//								Toast.LENGTH_SHORT).show();
//						
//						on
//					}
//				});


				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			try{
				holder.textLine.setText(data.get(position).name);
				holder.invitedBy.setText("Invited by : " + data.get(position).invited_by);	
				holder.message.setText(data.get(position).message);	
			}
			catch(Exception e){
				
			}
			
			
			return convertView;
		}

		static class ViewHolder {
			TextView textLine;
			TextView invitedBy;
			TextView message;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return data.get(position).id;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position).name;
		}
		
	}
}
