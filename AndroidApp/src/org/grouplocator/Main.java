package org.grouplocator;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONObject;

import com.facebook.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Main extends Activity {

	private TabHost tabHost;
	private Spinner spinner_time, spinner_range;
	private Main main;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		main = this;

		ListView listView = (ListView) findViewById(R.id.mylist);
		String[] values = new String[] { "Create", "Invitations",
				"Subscriptions" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		// Assign adapter to ListView
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					gotoGroupCreate();
					break;
				case 1:
					gotoInvi();
					break;
				case 2:
					gotoSubs();
					break;
				}
			}
		});

		String[] items = new String[] { "High", "Medium", "Low" };

		ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, android.R.id.text1, items);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner_time = (Spinner) findViewById(R.id.spinner_time);
		spinner_range = (Spinner) findViewById(R.id.spinner_range);

		spinner_time.setAdapter(spinner_adapter);
		spinner_range.setAdapter(spinner_adapter);

		tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId == "logout") {
					logout();
				}
				if (tabId == "settings") {
					new GLHttpRequest(main) {
						@Override
						protected void onPostExecute(JSONObject result) {
							try {
								if (result.has("error")) {
									showAlert("Error occurred while loading settings");
								} else {
									if (result.getString("loc_frequency")
											.toUpperCase().contentEquals("H"))
										spinner_time.setSelection(0);
									else if (result.getString("loc_frequency")
											.toUpperCase().contentEquals("M"))
										spinner_time.setSelection(1);
									else if (result.getString("loc_frequency")
											.toUpperCase().contentEquals("L"))
										spinner_time.setSelection(2);

									if (result.getString("loc_range")
											.toUpperCase().contentEquals("H"))
										spinner_range.setSelection(0);
									else if (result.getString("loc_range")
											.toUpperCase().contentEquals("M"))
										spinner_range.setSelection(1);
									else if (result.getString("loc_range")
											.toUpperCase().contentEquals("L"))
										spinner_range.setSelection(2);

								}
							} catch (Exception e) {
								showAlert("Error occurred while loading seetings");
							}
						}
					}.settings();
				}
				// showAlert(tabId);
			}
		});

		TabSpec spec1 = tabHost.newTabSpec("groups");
		spec1.setIndicator("Groups");
		spec1.setContent(R.id.tab_groups);

		TabSpec spec2 = tabHost.newTabSpec("settings");
		spec2.setIndicator("Settings");
		spec2.setContent(R.id.tab_settings);

		TabSpec spec3 = tabHost.newTabSpec("logout");
		spec3.setIndicator("Logout");
		spec3.setContent(R.id.tab_logout);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);

		Button btn_settings_update = (Button) this
				.findViewById(R.id.btn_settings_update);
		btn_settings_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new GLHttpRequest(main) {
					@Override
					protected void onPostExecute(JSONObject result) {
						try {
							if (!result.has("success"))
								showAlert("Update failed!!!");
							else if (!(result.getString("success")
									.compareToIgnoreCase("yes") == 0))
								showAlert("Update failed!!!");
						} catch (Exception e) {
							showAlert("Update failed!!!");
						}
					}
				}.update_settings(spinner_range.getSelectedItem().toString(),
						spinner_time.getSelectedItem().toString());
			}
		});
	}

	public void switchToIndex() {

	}

	public void gotoGroupCreate() {
		Intent intent = new Intent();
		intent.setClass(main, GroupCreate.class);
		startActivity(intent);
	}

	public void gotoSubs() {
		Intent intent = new Intent();
		intent.setClass(main, Subscriptions.class);
		startActivity(intent);
	}

	public void gotoInvi() {
		Intent intent = new Intent();
		intent.setClass(main, Invitations.class);
		startActivity(intent);
	}

	public void logout_failed() {
		showAlert("Logout failed!!");
		tabHost.setCurrentTabByTag("groups");
	}

	public void logout() {
		new GLHttpRequest(this) {
			@Override
			protected void onPostExecute(JSONObject result) {
				try {

					if (result.has("success")) {

						if (result.getString("success").compareToIgnoreCase("yes") == 0) {
							Thread mMessageSender = new Thread(
									new Runnable() {
										@Override
										public void run() {
											try {
												Utility.mFacebook.logout(main);
												Intent intent = new Intent();
									    		intent.setClass(main, Index.class);
									    		startActivity(intent);
											} catch (MalformedURLException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									});
							mMessageSender.start();

//							//
//							new AlertDialog.Builder(main)
//									.setTitle("Logout")
//									.setMessage(
//											"Do you want to logout from Facebook?")
//									.setPositiveButton(
//											"Yes",
//											new DialogInterface.OnClickListener() {
//												@Override
//												public void onClick(
//														DialogInterface dialog,
//														int which) {
//													
//												}
//											})
//									.setNegativeButton(
//											"No",
//											new DialogInterface.OnClickListener() {
//												@Override
//												public void onClick(
//														DialogInterface dialog,
//														int which) {
//													switchToIndex();
//												}
//
//											}).show();
						}
					} else {
						// Log.e("GLHttpRequest", "failed");
						logout_failed();
					}
				} catch (Exception e) {
					// Log.e("GLHttpRequest", "failed");
					logout_failed();
				}

			}
		}.logout();
	}

	protected void showAlert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
