package com.robotemplates.webviewapp;

import android.app.Application;
import android.content.Context;
import com.parse.Parse;


public class WebViewAppApplication extends Application
{
	private static WebViewAppApplication mInstance;

	public WebViewAppApplication()
	{
		mInstance = this;
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// force AsyncTask to be initialized in the main thread due to the bug:
		// http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
		try
		{
			Class.forName("android.os.AsyncTask");
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// initialize Parse
		if(WebViewAppConfig.PARSE_APPLICATION_ID != null && !WebViewAppConfig.PARSE_APPLICATION_ID.equals("") &&
				WebViewAppConfig.PARSE_CLIENT_KEY != null && !WebViewAppConfig.PARSE_CLIENT_KEY.equals(""))
		{
			Parse.initialize(this, WebViewAppConfig.PARSE_APPLICATION_ID, WebViewAppConfig.PARSE_CLIENT_KEY);
		}
	}


	public static Context getContext()
	{
		return mInstance;
	}
}
