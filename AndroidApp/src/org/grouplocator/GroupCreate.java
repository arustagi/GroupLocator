package org.grouplocator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.android.BaseRequestListener;
import com.facebook.android.FacebookError;
import com.facebook.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GroupCreate extends Activity {
	ProgressDialog dialog;
	GroupCreate me;
	private String friend_ids;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_new);

		me = this;
		Button btn_friendsList = (Button) this
				.findViewById(R.id.btn_friendsList);
		
		
		
		
		btn_friendsList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addFriends();
			}
		});

		Button btn_grp_create = (Button) this.findViewById(R.id.btn_grp_create);
		btn_grp_create.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGroup();
			}
		});

	}

	public void createGroup() {
		TextView grp_name = (TextView) this.findViewById(R.id.txt_groupName);
		EditText txt_message = (EditText)findViewById(R.id.txt_message);
		//showAlert(this.friend_ids);
		// frnd_list.getText().toString());
		new GLHttpRequest(me) {
			@Override
			protected void onPostExecute(JSONObject json) {
				try {
					//String groupId = json.getString("group_id");
					String msg = "";
					if (json.has("success")) {
						msg = "Group created.";
						if (! json.has("friends_not_added")){
							msg += " But no friends were not added.";
						}
						else{ 
							JSONArray x = json.getJSONArray("friends_not_added");
							if (x.length() > 0) 
							msg += " But some friends were not added.";
						}
						showAlert(msg);
						
					}else{
						showAlert("Error while creating the group.");
					}
					gotoMain();
				} catch (Exception e) {
					showAlert("Error while creating the group.");
				}
			}
		}.create_group(grp_name.getText().toString(), this.friend_ids, txt_message.getText().toString());
	}

	public void addFriends() {
		dialog = ProgressDialog.show(this, "", "Please Wait...", true, true);
		Bundle params = new Bundle();
		params.putString("fields", "name");
		Utility.mAsyncRunner.request("me/friends", params,
				new FriendsRequestListener());
	}

	/*
	 * callback after friends are fetched via me/friends or fql query.
	 */
	public class FriendsRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			dialog.dismiss();
			Intent myIntent = new Intent(getApplicationContext(),
					FacebookFriendsList.class);
			myIntent.putExtra("API_RESPONSE", response);
			startActivityForResult(myIntent, 0);
		}

		public void onFacebookError(FacebookError error) {
			dialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (0): {
			if (resultCode == Activity.RESULT_OK) {
				TextView txt_friendsList = (TextView) this
						.findViewById(R.id.txt_friendsList);
				txt_friendsList.setText(data.getStringExtra("friendsNames"));
				this.friend_ids = data.getStringExtra("friendsList");
			}
			break;
		}
		}
	}

	protected void showAlert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	protected void gotoMain() {
		Intent intent = new Intent();
		intent.setClass(this, Main.class);
		startActivity(intent);
	}

}
