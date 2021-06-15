package com.ai.wifidetection.utils;

import android.util.Log;


public class LogUtil {

	public static final int RUNNING_MODE_DEBUG = 1;
	public static final int RUNNING_MODE_INFO = 2;
	public static final int RUNNING_MODE_WRING = 3;
	public static final int RUNNING_MODE_ERROR = 4;
	public static final String LOG_TAG = "xbb";
	
	public static void d(String message){
		if(message == null){
			return;
		}
		Log.d(LOG_TAG,message);
	}
	
	public static void d(String tag,String message){
		if(null== message || null== tag){
			return;
		}
		Log.d(tag,message);
	}
	
	public static void d(String message,Throwable t){
		if(message == null){
			return;
		}
		Log.d(LOG_TAG, message, t);
	}
	
	public static void i(String message){
		if(message == null){
			return;
		}
		Log.i(LOG_TAG, message);
	}
	
	public static void i(String message,Throwable t){
		if(message == null){
			return;
		}
		Log.i(LOG_TAG, message,t);
	}
	
	public static void w(String message){
		if(message == null){
			return;
		}
		Log.w(LOG_TAG, message);
	}
	
	public static void w(String message,Throwable t){
		if(message == null){
			return;
		}
		Log.w(LOG_TAG, message,t);
	}
	public static void e(String message){
		if(message == null){
			return;
		}
		Log.e(LOG_TAG, message);
	}
	
	public static void e(String message,Throwable t){
		if(message == null){
			return;
		}
		Log.e(LOG_TAG, message,t);
	}
}
