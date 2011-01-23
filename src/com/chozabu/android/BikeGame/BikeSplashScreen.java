package com.chozabu.android.BikeGame;


import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.opengl.texture.source.AssetTextureSource;
import org.anddev.andengine.opengl.texture.source.ITextureSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;

import com.flurry.android.FlurryAgent;
import com.nullwire.trace.ExceptionHandler;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;


import android.os.Handler;

public class BikeSplashScreen extends BaseSplashActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int SPLASH_DURATION = 2;
	private static final float SPLASH_SCALE_FROM = 0.5f;
	public void onStart()
	{
	   super.onStart();
		//ExceptionHandler.register(this, "http://chozabu.net/wheelogz"); 
	   FlurryAgent.onStartSession(this, StatStuff.flurryKey);
	   // your code
	}
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	   // your code
	}
	

	@Override
	public void onLoadComplete() {
		//Debug.startMethodTracing("abike");
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	int playCount = prefs.getInt("playCount", 0);
    	playCount++;

		Editor edit = prefs.edit();
		edit.putInt("playCount", playCount);
		edit.commit();
		
	}
	


    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
    @Override
    protected void onDestroy() {
		super.onDestroy();
	    //Toast.makeText(this.getApplicationContext(), "Loading", Toast.LENGTH_SHORT).show();
	}
	
    
	@Override
	protected ScreenOrientation getScreenOrientation() {
		// TODO check real orientation ?
		return ScreenOrientation.LANDSCAPE;
	}

	@Override
	protected ITextureSource onGetSplashTextureSource() {
		return new AssetTextureSource(this, "gfx/doublelogo.png");
	}

	@Override
	protected float getSplashDuration() {
		return SPLASH_DURATION;
	}

	@Override
    protected float getSplashScaleFrom() {
            return SPLASH_SCALE_FROM;
    }
	
	@Override
	protected Class<? extends Activity> getFollowUpActivity() {
		return MainActivity.class;
	}

}