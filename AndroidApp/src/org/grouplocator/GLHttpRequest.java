package org.grouplocator;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class GLHttpRequest extends AsyncTask<Void, Void, JSONObject>{
	
	private String url;
	private ArrayList<NameValuePair> postParameters;
	private ProgressDialog dialog=null;
	
	public GLHttpRequest(){}
	
	public GLHttpRequest(Context context){
		dialog = ProgressDialog.show(context, "", "Please wait...", true, true);
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
		String response = null;
		try {
			response = GLHttpClient.executeHttpPost(url, postParameters).toString();
			Log.e("GLHttpRequest", response);
		} catch (Exception e) {
			Log.e("GLHttpRequest", e.toString());
		}		

		if(dialog != null){
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}

	    JSONObject json = null;
		try {
			json = new JSONObject(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}

	
	public static String getAbsoluteUrl(String relativeUrl) {
		return Utility.BASE_URL + relativeUrl;
	}
	
	public void login(String fbAccessToken){
		url = getAbsoluteUrl("login/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("fb_access_token", fbAccessToken.toString()));
		
		this.execute(new Void[]{});
	}
	
	public void logout(){
		url = getAbsoluteUrl("logout/");
		postParameters = new ArrayList<NameValuePair>();

		this.execute(new Void[]{});
	}
	
	
	public void settings(){
		url = getAbsoluteUrl("settings/");
		postParameters = new ArrayList<NameValuePair>();

		this.execute(new Void[]{});
	}
	

	public void update_settings(String range, String frequency){
		url = getAbsoluteUrl("update_settings/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("loc_freq", frequency.substring(0,1)));
		postParameters.add(new BasicNameValuePair("loc_range", range.substring(0,1)));
		
		this.execute(new Void[]{});
	}

	
	public void create_group(String name, String friends, String message){
		url = getAbsoluteUrl("create_group/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_name", name));
		postParameters.add(new BasicNameValuePair("g_add", friends));
		postParameters.add(new BasicNameValuePair("g_msg", message));
		this.execute(new Void[]{});
	}
	
	public void add_friends_to_group(String g_id, String friends){
		url = getAbsoluteUrl("add_subscribers_to_group/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_id", g_id));
		postParameters.add(new BasicNameValuePair("g_add", friends));
		
		this.execute(new Void[]{});
	}
	
	public void get_groups(String g_id, String status){
		url = getAbsoluteUrl("get_member_groups/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_id", g_id));
		postParameters.add(new BasicNameValuePair("g_status", status));
		
		this.execute(new Void[]{});
	}
	
	public void update_group_status(String g_id, String status){
		url = getAbsoluteUrl("update_membership_status/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_id", g_id));
		postParameters.add(new BasicNameValuePair("g_status", status));
		
		this.execute(new Void[]{});
	}
	
	public void get_group(String g_id){
		url = getAbsoluteUrl("get_group_by_id/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_id", g_id));
		this.execute(new Void[]{});
	}
	
	
	public void get_group_members(String g_id, String status){
		url = getAbsoluteUrl("get_group_members/");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("g_id", g_id));
		postParameters.add(new BasicNameValuePair("g_status", status));
		
		this.execute(new Void[]{});
	}
}
