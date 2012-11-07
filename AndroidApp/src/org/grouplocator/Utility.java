package org.grouplocator;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import android.app.Application;


public class Utility extends Application{
	public static final String BASE_URL = "http://ec2-23-21-34-217.compute-1.amazonaws.com:8000/locatorapp/";
	public static final String FB_APP_ID = "152145051574383";
	public static final String PUBNUB_PUBLISH_KEY = "pub-360d8a46-0582-46cb-8e9f-783bde4c592e";
	public static final String PUBNUB_SUBSCRIBE_KEY = "sub-b56ab535-78b8-11e1-afdc-fb12d581f217";
	public static final String PUBNUB_SECRET_KEY = "sec-b4df37ac-1e50-468d-8401-bcc7a65d3756";
	private static String MY_GL_ID = null;
	public static Facebook mFacebook;
    public static AsyncFacebookRunner mAsyncRunner;
    public static boolean fb_session_valid;
    
	public static String get_GL_ID(){
		return MY_GL_ID;
	}
	
	public static void unset_GL_ID(){
		MY_GL_ID = null;
	}
	
	public static void  set_GL_ID(String id){
		MY_GL_ID = id;
	}
	
}
