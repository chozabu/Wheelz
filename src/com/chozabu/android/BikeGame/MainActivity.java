package com.chozabu.android.BikeGame;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchException;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.flurry.android.FlurryAgent;
import com.nullwire.trace.ExceptionHandler;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;



public class MainActivity extends LayoutGameActivity implements
		 AdWhirlInterface {
	

	ZoomCamera camera;
	Textures textures = new Textures();
	Sounds sounds = new Sounds();
	GameWorld gameWorld = new GameWorld();
	SharedPreferences prefs;
	GameScene currentGameScene;

	@Override
	public void onPause() {
		super.onPause();
		if (sounds != null)
			sounds.stop();
	}
	
	

	@Override
	protected void onCreate(final Bundle pSavedInstanceState)  {
		super.onCreate(pSavedInstanceState);
		//Debug.startMethodTracing("abike");

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//this.currentGameScene = new MainMenuGameScene();
		m_Handler = new Handler();
		
		//final OpenFeintSettings settings = new OpenFeintSettings("Wheelz", "lkhhpfoiA4J4vSxYjXJeA", "fqLtx1prnMHFyNceL543Pim3QFtT9xHi71oH3T0HuLE", "213402");
		
		
		  new Thread(){
            @Override
            public void run() {
            	initOpenFeint("Wheelz", "lkhhpfoiA4J4vSxYjXJeA", "fqLtx1prnMHFyNceL543Pim3QFtT9xHi71oH3T0HuLE", "213402");
            }
		  }.start();
		
	}
	

	private Handler m_Handler;
	public void initOpenFeint(final String name, final String key, final String secret, final String id) 
	{
		m_Handler.post(new Runnable() // you need to run this in separate thread or will end up with application crash
		{
			@Override
			public void run() 
			{
				OpenFeintSettings settings = new OpenFeintSettings(name, key, secret, id);
				//OpenFeint.initialize(MainActivity.this, settings, new OpenFeintDelegate() {});//new CustomOpenFeintDelegate());

				if(prefs.getBoolean("autoFeint", false)){
					OpenFeint.initialize(MainActivity.this, settings,new OpenFeintDelegate() {});
				}else{
					OpenFeint.initializeWithoutLoggingIn(MainActivity.this, settings,new OpenFeintDelegate() {});
				}
				
				//doResumeFeintActivity(); // !!!! you need this call
			}
		});
	}


	@Override
	public void onStart() {
		super.onStart();
		ExceptionHandler.register(this,
				"http://chozabu.net/wheelogz/server.php");
		FlurryAgent.onStartSession(this, StatStuff.flurryKey);
	}
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	   // your code
		if (sounds != null)
			sounds.stop();
	}

    protected void onDestroy() {
    	super.onDestroy();
		if (sounds != null)
			sounds.stop();
    }
	

	@Override
	public void onResume() {
		super.onResume();
		try {
			OpenFeint.setCurrentActivity(this);
		} catch (Exception e) {

		}
	    if(sounds!=null)
		sounds.start();
	}

	@Override
	protected int getLayoutID() {
		return R.layout.simplelayout;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.flip_render;
	}



	@Override
	public void adWhirlGeneric() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Engine onLoadEngine() {
		camera = new ZoomCamera(0, 0, StatStuff.CAMERA_WIDTH,
				StatStuff.CAMERA_HEIGHT);
		gameWorld.initEngine(this, camera);
		// camera.setRotation(180.f);
		// camera.setCameraSceneRotation(50.f);
		// camera.setZoomFactor(.05f);

		Engine engine = new Engine(new EngineOptions(true,
				ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
						StatStuff.CAMERA_WIDTH, StatStuff.CAMERA_HEIGHT),
				camera).setNeedsSound(true));

		try {
			if (MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
			}
		} catch (final MultiTouchException e) {
		}

		return engine;
	}
	
	public void enableAccel(IAccelerometerListener pAccelerometerListener){
		this.enableAccelerometerSensor(pAccelerometerListener);
	}

	@Override
	public void onLoadResources() {
		System.gc();
		textures.init(this);
		sounds.init(this);
		sounds.start();

		gameWorld.initRes(textures, sounds);

		System.gc();
		
	}

	@Override
	public Scene onLoadScene() {
		//return null;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String toLoad = extras
					.getString("com.chozabu.android.BikeGame.toLoad");
			
			extras.clear();
			

			currentGameScene = new GameRoot(MainActivity.this, -1, -1, toLoad);
			Scene nScene = currentGameScene.onLoadScene();
			nScene.registerUpdateHandler(new IUpdateHandler() {
			
				@Override
				public void onUpdate(float pSecondsElapsed) {
					MainActivity.this.frameUpdate(pSecondsElapsed);
				}

				@Override
				public void reset() {

				}
			});
			return nScene;
		}
		
		
		currentGameScene = new AEMainMenu(this);
		Scene nScene = currentGameScene.onLoadScene();
		nScene.registerUpdateHandler(new IUpdateHandler() {
		
			@Override
			public void onUpdate(float pSecondsElapsed) {
				MainActivity.this.frameUpdate(pSecondsElapsed);
			}

			@Override
			public void reset() {

			}
		});
		return nScene;
	}
	public void frameUpdate(float pSecondsElapsed){
		this.currentGameScene.frameUpdate(pSecondsElapsed);
	}

	public void setMainMenu() {
		this.disableAccelerometerSensor();
		this.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				BufferObjectManager.getActiveInstance().clear();
		currentGameScene = new AEMainMenu(MainActivity.this);

		Scene nScene = currentGameScene.onLoadScene();
		nScene.registerUpdateHandler(new IUpdateHandler() {
		
			@Override
			public void onUpdate(float pSecondsElapsed) {
				MainActivity.this.frameUpdate(pSecondsElapsed);
			}

			@Override
			public void reset() {

			}
		});
		MainActivity.this.getEngine().setScene(nScene);
		currentGameScene.onLoadComplete();

			}
		});
	}
	public void setInGame(final int packID, final int levelID) {
		this.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {

				BufferObjectManager.getActiveInstance().clear();
		currentGameScene = new GameRoot(MainActivity.this, packID, levelID, null);
		Scene nScene = currentGameScene.onLoadScene();
		nScene.registerUpdateHandler(new IUpdateHandler() {
		
			@Override
			public void onUpdate(float pSecondsElapsed) {
				MainActivity.this.frameUpdate(pSecondsElapsed);
			}

			@Override
			public void reset() {

			}
		});
		
		MainActivity.this.getEngine().setScene(nScene);
		currentGameScene.onLoadComplete();

	}
});
	}

	@Override
	public void onLoadComplete() {
		if (StatStuff.isDemo)loadAds();
		currentGameScene.onLoadComplete();
		
	}
	

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(currentGameScene==null)
			return super.onKeyDown(pKeyCode, pEvent);

		//Debug.stopMethodTracing();
		if(currentGameScene.onKeyDown(pKeyCode, pEvent))
			return true;
		return super.onKeyDown(pKeyCode, pEvent);
	}
	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		if(currentGameScene==null)
			return super.onKeyUp(pKeyCode, pEvent);
		if(currentGameScene.onKeyUp(pKeyCode, pEvent))
			return true;
		return super.onKeyUp(pKeyCode, pEvent);
	}
	

	public Scene getScene() {
		return this.mEngine.getScene();
	}
	

	private void loadAds() {

		AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);

		/*
		 * AdWhirlTargeting.setAge(23);
		 * AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
		 * AdWhirlTargeting.setKeywords("online games gaming");
		 * AdWhirlTargeting.setPostalCode("94123");
		 * AdWhirlTargeting.setTestMode(false);
		 */

		AdWhirlLayout adWhirlLayout = (AdWhirlLayout) findViewById(R.id.adwhirl_layout);
		int diWidth = 320;
		int diHeight = 52;
		float density = getResources().getDisplayMetrics().density;

		adWhirlLayout.setAdWhirlInterface(this);
		adWhirlLayout.setMaxWidth((int) (diWidth * density));
		adWhirlLayout.setMaxHeight((int) (diHeight * density));

		adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);

	}
	

	void quitFunc(){
		if(sounds!=null)
			sounds.stop();
		this.finish();
	}
	void superQuitFunc(){
		quitFunc();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
	}

}
