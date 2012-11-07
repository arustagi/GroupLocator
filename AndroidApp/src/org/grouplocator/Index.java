package org.grouplocator;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.R;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.SessionStore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Index extends Activity {
   
    private LoginButton mLoginButton;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.index);
        
        mLoginButton = (LoginButton) findViewById(R.id.login);
        Utility.mFacebook = new Facebook(Utility.FB_APP_ID);
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
       	
       	SessionStore.restore(Utility.mFacebook, this);
        SessionEvents.addAuthListener(new GLAuthListener());
        SessionEvents.addLogoutListener(new GLLogoutListener());
        
        mLoginButton.init(this, Utility.mFacebook);
        if(Utility.mFacebook.isSessionValid())
        	locatorLogin();
    }

    
    protected void locatorLogin(){
    	//showAlert("Inside locator, check your app");
    	new GLHttpRequest(this){
    		@Override
    		protected void onPostExecute(JSONObject json) {
    			//showAlert(json.toString());
    			if (json.has("error")){
    				showAlert("Error in locator login");
    			}
    			else{
    				try {
						Utility.set_GL_ID(json.getString("member_id"));
						locatorLoginSucceed();
					} catch (JSONException e) {
						showAlert("Login failed, Please try again!");
						e.printStackTrace();
					}
    			}
    		}
    	}.login(Utility.mFacebook.getAccessToken());
        
    }
    
    protected void locatorLoginSucceed(){
    	Intent intent = new Intent();
    	intent.setClass(this, Main.class);
    	startActivity(intent);
    }
    
    protected void showAlert(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
    }


	public class GLAuthListener implements AuthListener {
		
        public void onAuthSucceed() {
        	locatorLogin();
        }

        public void onAuthFail(String error) {
        	String msg = "Facebook login failed.\nPlease try again!!"; 
        	showAlert(msg);
        }
    }

    public class GLLogoutListener implements LogoutListener {
    	
        public void onLogoutBegin() {
        }

        public void onLogoutFinish() {
        	String msg = "Logged out of Facebook!!"; 
        	showAlert(msg);
        }
    }
}