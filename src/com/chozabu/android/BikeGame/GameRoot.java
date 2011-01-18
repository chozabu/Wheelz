package com.chozabu.android.BikeGame;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.shape.GLShape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.modifier.IModifier;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.flurry.android.FlurryAgent;
import com.nullwire.trace.ExceptionHandler;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.resource.Achievement;
import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View; //import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GameRoot<BaseGameActivity> extends LayoutGameActivity implements
		IOnSceneTouchListener, IAccelerometerListener, AdWhirlInterface {

	final String dbt = StatStuff.dbt;
	FPSLogger fpsLog = new FPSLogger();

	GameWorld gameWorld = new GameWorld();

	final static float World_Scale = 10.f;

	private Scene mScene = null;

	Textures textures = new Textures();
	Sounds sounds = new Sounds();

	ZoomCamera camera;

	HUD inGameHud = new HUD(1);
	HUD menuHud = new HUD(1);
	HUD noHud = new HUD(1);
	Sprite flipButton;
	Sprite accelBar;
	Sprite accelButton;
	Sprite brakeButton;
	Sprite leanLeftButton;
	Sprite leanRightButton;

	final Menus menus = new Menus();

	private AnalogOnScreenControl analogOnScreenControl;
	private AnalogOnScreenControl2d analogOnScreenControl2d;

	private float hitTime = 0;

	SharedPreferences prefs;

	// String currentPack = null;
	int currentPackID = -1;
	private boolean isPaused;
	private boolean tiltOn;
	private boolean driveStickLean = false;
	private String ABControls = "";
	private String LRControls = "";
	// private boolean barOn;

	float timeTaken = 0f;
	ChangeableText timeTakenText;
	ChangeableText recordTimeText;
	ChangeableText recordTimeUserNameText;
	ChangeableText berrysLeftText;

	ChangeableText personalRecordTimeText;
	ChangeableText personalRecordTimeLabelText;
	ChangeableText personalRankText;
	ChangeableText personalRankLabelText;

	ChangeableText timeTakenLabelText;
	ChangeableText recordTimeLabelText;
	// ChangeableText berrysLeftLabelText;
	private boolean loadFinished = false;
	private float accelerometerSensitivity = 1f;
	private float accelerometerDeadZone = 1f;

	private boolean canCrash = true;
	private float mStepLength;
	private boolean controlsOnRight = false;

	public Bike getBike() {
		return gameWorld.bike;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (sounds != null)
			sounds.stop();
		// this.
	}

	public void onStart() {
		super.onStart();
		ExceptionHandler.register(this,
				"http://chozabu.net/wheelogz/server.php");
		FlurryAgent.onStartSession(this, StatStuff.flurryKey);
		// your code
	}

	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
		// your code
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			OpenFeint.setCurrentActivity(this);
		} catch (Exception e) {

		}
		// if(this.getEngine()!=null)
		// Sounds.init(this);
		sounds.start();
	}

	@Override
	public Engine onLoadEngine() {
		// Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		/*
		 * Editor edit = prefs.edit(); edit.clear(); edit.commit();
		 */
		String cheatsString = prefs.getString("cheatsString", "");
		canCrash = !cheatsString.contains("ChozabuIsGod");
		if (!StatStuff.isDev)
			StatStuff.isDev = cheatsString.contains("IAmChozabu");

		ABControls = prefs.getString("ABControls", "Buttons");
		LRControls = prefs.getString("LRControls", "Buttons");
		controlsOnRight = prefs.getBoolean("controlsOnRight", false);
		tiltOn = LRControls.equals("Accelerometer");
		accelerometerSensitivity = Float.parseFloat(prefs.getString(
				"tiltSensitivity", "1.0"));
		accelerometerDeadZone = Float.parseFloat(prefs.getString(
				"tiltDeadZone", "1.0"));

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
				/*
				 * (if(MultiTouch.isSupportedDistinct(this)) {
				 * //Toast.makeText(this,
				 * "MultiTouch detected --> Both controls will work properly!",
				 * Toast.LENGTH_LONG).show(); } else {
				 * this.mPlaceOnScreenControlsAtDifferentVerticalLocations =
				 * true; //Toast.makeText(this,
				 * "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations."
				 * , Toast.LENGTH_LONG).show(); }
				 */
			} else {
				// Toast.makeText(this,
				// "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.",
				// Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			// Toast.makeText(this,
			// "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.",
			// Toast.LENGTH_LONG).show();
		}

		return engine;
	}

	@Override
	public void onLoadResources() {
		System.gc();
		textures.init(this);
		sounds.init(this);

		gameWorld.initRes(textures, sounds);

		if (tiltOn)
			this.enableAccelerometerSensor(this);
	}

	@Override
	public Scene onLoadScene() {
		//this.mEngine.registerUpdateHandler(fpsLog);

		menus.init(this);

		final Scene scene = new Scene(4);
		this.mScene = scene;
		scene.setOnSceneTouchListener(this);

		this.mScene.clearChildScene();
		this.mScene.setChildScene(menus.mMenuLoading);

		gameWorld.initScene(mScene);

		gameWorld.mPhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void beginContact(Contact contact) {
				if (getBike().isDead())
					return;

				if (getBike().containsBody(contact.getFixtureA().getBody())
						|| getBike().containsBody(
								contact.getFixtureB().getBody())) {
					//sounds - body impact
					if (getBike().mBody == (contact.getFixtureA().getBody())
							|| getBike().mBody == (contact.getFixtureB()
									.getBody())) {
						Vector2 va = contact.getFixtureA()
						.getBody().getLinearVelocity();
						va.sub(contact.getFixtureB()
						.getBody().getLinearVelocity());
						float iForce = va.len2();
						 
						if(iForce>1.75f){
							
							if (hitTime <= 0) {
								hitTime = 0.1f;
								float iVol = iForce-0.8f;
								iVol*=0.35f;
								iVol = MathUtils.bringToBounds(0f, 1f, iVol);
								sounds.mHitBodySound.stop();
								sounds.mHitBodySound.setVolume(iVol);
								sounds.mHitBodySound.play();
							}
							
						}
					}
					//sounds - wheel impact
					/*if (getBike().fWheel == (contact.getFixtureA().getBody())
							|| getBike().fWheel == (contact.getFixtureB()
									.getBody()) || 
						getBike().bWheel == (contact.getFixtureA().getBody())
								|| getBike().bWheel == (contact.getFixtureB()
										.getBody()))
					{
						Vector2 contactPos = contact.GetWorldManifold().getPoints()[0];
						

						Vector2 va = contact.getFixtureA()
						.getBody().getLinearVelocityFromWorldPoint(contactPos);
						va.sub(contact.getFixtureB()
						.getBody().getLinearVelocityFromWorldPoint(contactPos));

						Vector2 vb = contact.getFixtureA()
						.getBody().getLinearVelocity();
						vb.sub(contact.getFixtureB()
						.getBody().getLinearVelocity());
						float iForce = Math.min(va.len(),vb.len());
						iForce = Math.min((va.len()+vb.len())/2f,iForce);
						 
						if(iForce>7.f){
							
							if (hitTime <= 0) {
								hitTime = 0.05f;
								float iVol = iForce-6.2f;
								iVol*=0.35f;
								iVol = MathUtils.bringToBounds(0f, 1f, iVol);
								sounds.mHitWheelSound.setVolume(iVol);
								sounds.mHitWheelSound.play();
								//sounds.mMThunkSound.
							}
							
						}
					}*/
					
					
					// finish line Hit!
					if (gameWorld.endList.contains(contact.getFixtureA()
							.getBody())
							|| gameWorld.endList.contains(contact.getFixtureB()
									.getBody())) {
						

						
						if (gameWorld.berryCount > 0)
							return;
						GameRoot.this.sounds.mCollectedSound.play();
						// GameRoot.this.pause();

						completeLevel();
						GameRoot.this.getBike().setDead(true);

						mScene.clearChildScene();
						scene.setChildScene(menus.mMenuComplete);
						camera.setHUD(menuHud);
						// mHUD.setVisible(false);
						// GameRoot.this.getBike().stopWheels();
						analogOnScreenControl.resetControlKnobPosition();
						analogOnScreenControl2d.resetControlKnobPosition();
						return;
					}

					// got berry
					if (gameWorld.strawBerryList.contains(contact.getFixtureA()
							.getBody())
							|| gameWorld.strawBerryList.contains(contact
									.getFixtureB().getBody())) {
						Body berry = contact.getFixtureA().getBody();
						if (gameWorld.strawBerryList.contains(contact
								.getFixtureB().getBody())) {
							berry = contact.getFixtureB().getBody();
						}
						GLShape berryPic = ((UserData) berry.getUserData()).sprite;
						if (!berryPic.isVisible())
							return;
						GameRoot.this.sounds.mCollectedSound.play();
						berryPic.setVisible(false);
						gameWorld.berryCount--;
						gameWorld.checkCanFinish();
						return;

					}

					if (gameWorld.wreckerList.contains(contact.getFixtureA()
							.getBody())
							|| gameWorld.wreckerList.contains(contact
									.getFixtureB().getBody())) {
						GameRoot.this.crashBike();
						return;
					}

				}

				// truck roof hit!
				if (contact.getFixtureA().getBody() == getBike().roofSensor
						|| contact.getFixtureB().getBody() == getBike().roofSensor)
					if (contact.getFixtureA().getBody() != getBike().mBody
							&& contact.getFixtureB().getBody() != getBike().mBody
							&& contact.getFixtureA().getBody() != getBike().fWheel
							&& contact.getFixtureB().getBody() != getBike().fWheel
							&& contact.getFixtureA().getBody() != getBike().bWheel
							&& contact.getFixtureB().getBody() != getBike().bWheel) {
						GameRoot.this.crashBike();
						return;
					}
			}

			@Override
			public void endContact(Contact contact) {
				//contact.GetWorldManifold().
				// TODO Auto-generated method stub
			}

		});

		analogOnScreenControl = new AnalogOnScreenControl(
				15,// 15,
				StatStuff.CAMERA_HEIGHT
						- textures.mOnScreenControlBaseTextureRegion
								.getHeight() - 15, this.camera,
				textures.mOnScreenControlBaseTextureRegion,
				textures.mOnScreenControlKnobTextureRegion, 0.1f, 200,
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						getBike().setSpeed(-pValueY);

						if (GameRoot.this.driveStickLean)
							getBike().modRot(pValueX);
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
						// GameRoot.this.getBike().flipDirecion();
					}
				});
		analogOnScreenControl.getControlBase().setBlendFunction(
				GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.getControlBase().setScaleCenter(0, 0);
		analogOnScreenControl.getControlBase().setScale(1.25f);
		analogOnScreenControl.getControlKnob().setScale(1.25f);
		analogOnScreenControl.refreshControlKnobPosition();

		analogOnScreenControl2d = new AnalogOnScreenControl2d(
				(int) (StatStuff.CAMERA_WIDTH
						- textures.mOnScreenControlBaseTextureRegion2d
								.getWidth() * 1.35f - 10),
				(int) (StatStuff.CAMERA_HEIGHT
						/ 2
						- textures.mOnScreenControlBaseTextureRegion2d
								.getHeight() * 1.35f - 10), this.camera,
				textures.mOnScreenControlBaseTextureRegion2d,
				textures.mOnScreenControlKnobTextureRegion, 0.1f, 200,
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						getBike().modRot(pValueX);
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
					}
				});
		analogOnScreenControl2d.getControlBase().setBlendFunction(
				GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl2d.getControlBase().setAlpha(0.5f);
		analogOnScreenControl2d.getControlBase().setScaleCenter(0, 0);
		analogOnScreenControl2d.getControlBase().setScale(1.25f);
		analogOnScreenControl2d.getControlKnob().setScale(1.25f);
		analogOnScreenControl2d.refreshControlKnobPosition();

		int FlipY = StatStuff.CAMERA_HEIGHT
				- textures.mFlipTextureRegion.getHeight();
		int FlipY2 = FlipY - textures.mFlipTextureRegion.getHeight();

		int lflipy;
		int lflipx;
		if (controlsOnRight) {
			lflipy = FlipY2;
			lflipx = StatStuff.CAMERA_WIDTH
					- textures.mFlipTextureRegion.getWidth();
		} else {
			lflipy = FlipY;
			lflipx = StatStuff.CAMERA_WIDTH / 2
					- textures.mFlipTextureRegion.getWidth() / 2;
		}

		flipButton = new Sprite(lflipx, lflipy, textures.mFlipTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.setScale(0.85f);
					flipBike();
					break;
				case MotionEvent.ACTION_UP:
					this.setScale(0.75f);
					break;
				}
				return true;
			}

		};
		flipButton.setScale(0.75f);

		accelButton = new Sprite(0, FlipY, textures.mAccelerateButtonRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.setScale(1.25f);
					getBike().setSpeed(1);
					break;
				case MotionEvent.ACTION_UP:
					getBike().setSpeed(0);
					this.setScale(1.f);
					break;
				}
				return true;
			}
		};
		brakeButton = new Sprite(textures.mBrakeButtonRegion.getWidth(), FlipY,
				textures.mBrakeButtonRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.setScale(1.25f);
					getBike().setSpeed(-1);
					break;
				case MotionEvent.ACTION_UP:
					getBike().setSpeed(0);
					this.setScale(1.f);
					break;
				}
				return true;
			}
		};
		leanRightButton = new Sprite(StatStuff.CAMERA_WIDTH
				- textures.mLeanRightButtonRegion.getWidth(), FlipY,
				textures.mLeanRightButtonRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.setScale(1.25f);
					getBike().modRot(1);
					break;
				case MotionEvent.ACTION_UP:
					getBike().modRot(0);
					this.setScale(1.f);
					break;
				}
				return true;
			}
		};
		leanLeftButton = new Sprite(StatStuff.CAMERA_WIDTH
				- textures.mLeanLeftButtonRegion.getWidth() * 2, FlipY,
				textures.mLeanLeftButtonRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.setScale(1.25f);
					getBike().modRot(-1);
					break;
				case MotionEvent.ACTION_UP:
					getBike().modRot(0);
					this.setScale(1.f);
					break;
				}
				return true;
			}
		};
		accelBar = new Sprite(0, 0, 64, StatStuff.CAMERA_HEIGHT,
				textures.mAccelBarTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch (pSceneTouchEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					float chq = StatStuff.CAMERA_HEIGHT / 4f;
					float y = -pTouchAreaLocalY + StatStuff.CAMERA_HEIGHT;
					y -= chq;
					y = (y < 0) ? y / chq : y / (chq * 4f);
					getBike().setSpeed(y);
					break;
				case MotionEvent.ACTION_UP:
					getBike().setSpeed(0);
					break;
				}
				return true;
			}

		};

		// inGameHud.setVisible(false);
		inGameHud.getTopLayer().addEntity(flipButton);
		inGameHud.registerTouchArea(flipButton);

		if (ABControls.equals("Buttons")) {
			inGameHud.getTopLayer().addEntity(accelButton);
			inGameHud.registerTouchArea(accelButton);
			inGameHud.getTopLayer().addEntity(brakeButton);
			inGameHud.registerTouchArea(brakeButton);
		} else if (ABControls.equals("accelBar")) {
			inGameHud.getTopLayer().addEntity(accelBar);
			inGameHud.registerTouchArea(accelBar);
		} else if (ABControls.equals("accelStick")) {
			inGameHud.setChildScene(analogOnScreenControl);
		}

		if (LRControls.equals("leanStick")) {
			if (ABControls.equals("accelStick"))
				analogOnScreenControl.setChildScene(analogOnScreenControl2d);
			else
				inGameHud.setChildScene(analogOnScreenControl2d);
		} else if (LRControls.equals("Buttons")) {
			inGameHud.getTopLayer().addEntity(leanLeftButton);
			inGameHud.registerTouchArea(leanLeftButton);
			inGameHud.getTopLayer().addEntity(leanRightButton);
			inGameHud.registerTouchArea(leanRightButton);
		} else if (LRControls.equals("accelStick")) {
			driveStickLean = true;
			if (!ABControls.equals("accelStick")) {
				inGameHud.setChildScene(analogOnScreenControl2d);
			}
		}

		timeTakenText = new ChangeableText(64, 0, textures.mFont, "0",
				HorizontalAlign.CENTER, 5);

		recordTimeText = new ChangeableText(64, 64, textures.mFont, "56789",
				HorizontalAlign.CENTER, 5);
		recordTimeText.setText("01234");
		recordTimeText.setText("...");

		berrysLeftText = new ChangeableText(64, 128, textures.mFont, "0",
				HorizontalAlign.CENTER, 5);
		inGameHud.getTopLayer().addEntity(timeTakenText);
		inGameHud.getTopLayer().addEntity(recordTimeText);
		inGameHud.getTopLayer().addEntity(berrysLeftText);
		// camera.setHUD(inGameHud);

		timeTakenLabelText = new ChangeableText(64 + 128, 0, textures.mFont,
				"-Your Time", HorizontalAlign.CENTER, 10);

		recordTimeLabelText = new ChangeableText(64 + 128, 64, textures.mFont,
				"-", HorizontalAlign.CENTER, 10);
		recordTimeUserNameText = new ChangeableText(8
				+ recordTimeLabelText.getBaseX()
				+ recordTimeLabelText.getBaseWidth(), 64, textures.mFont,
				"-World Record", HorizontalAlign.CENTER, 20);

		personalRecordTimeText = new ChangeableText(64,
				StatStuff.CAMERA_HEIGHT - 64, textures.mFont, "...",
				HorizontalAlign.CENTER, 5);

		personalRankText = new ChangeableText(64,
				StatStuff.CAMERA_HEIGHT - 128, textures.mFont, "...",
				HorizontalAlign.CENTER, 5);

		personalRecordTimeLabelText = new ChangeableText(64 + 128,
				StatStuff.CAMERA_HEIGHT - 64, textures.mFont, "-Your Best",
				HorizontalAlign.CENTER, 10);

		personalRankLabelText = new ChangeableText(64 + 128,
				StatStuff.CAMERA_HEIGHT - 128, textures.mFont, "-lvl Rank",
				HorizontalAlign.CENTER, 20);

		menuHud.getTopLayer().addEntity(personalRecordTimeText);
		menuHud.getTopLayer().addEntity(personalRankText);
		menuHud.getTopLayer().addEntity(personalRecordTimeLabelText);
		menuHud.getTopLayer().addEntity(personalRankLabelText);

		// berrysLeftLabelText = new ChangeableText(64+64, 128, textures.mFont,
		// "-PickUps",
		// HorizontalAlign.CENTER, 10);
		menuHud.getTopLayer().addEntity(timeTakenText);
		menuHud.getTopLayer().addEntity(recordTimeText);
		menuHud.getTopLayer().addEntity(berrysLeftText);
		menuHud.getTopLayer().addEntity(timeTakenLabelText);
		menuHud.getTopLayer().addEntity(recordTimeLabelText);
		menuHud.getTopLayer().addEntity(recordTimeUserNameText);
		// menuHud.getTopLayer().addEntity(berrysLeftLabelText);
		camera.setHUD(menuHud);

		String inStr = prefs.getString("fpsLowLimit", "30");
		int minFps = Integer.parseInt(inStr);

		this.mStepLength = 1.0f / (float) minFps;

		scene.registerUpdateHandler(new IUpdateHandler() {
			int fc = 0;

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (!GameRoot.this.isPaused) {
					if (pSecondsElapsed >= GameRoot.this.mStepLength) {
						pSecondsElapsed = GameRoot.this.mStepLength;
						if (hitTime > 0)
							hitTime -= pSecondsElapsed;
						// Log.i("ABike","WARNING LOW FPS - GOING SLOWMO!");
					}
					if (!GameRoot.this.getBike().isDead()) {
						timeTaken += pSecondsElapsed;
						fc++;
						if (fc > 5) {
							fc = 0;
							timeTakenText.setText(String.valueOf(timeTaken));
							berrysLeftText.setText(String
									.valueOf(gameWorld.berryCount));
						}
					}
				}
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}
		});

		return scene;
	}

	void completeLevel() {
		if (currentPackID == -1){
			try {
				

					new Achievement("782992").unlock(new Achievement.UnlockCB () {
						@Override
						public void onSuccess(boolean newUnlock) {
							//Toast.makeText(GameRoot.this, "Pack Completed!.", Toast.LENGTH_SHORT).show();
						}
						@Override public void onFailure(String exceptionMessage) {
							//Toast.makeText(GameRoot.this, "Error (" + exceptionMessage + ") unlocking achievement.", Toast.LENGTH_SHORT).show();
						}
					});
			}catch(Exception e){
				
			}
				
			return;
		}
		if (!(gameWorld.levelId < StatStuff.packLevelCount[currentPackID]))
			return;
		if (!prefs.getString("cheatsString", "").equals("")) {
			// Toast.makeText(GameRoot.this,
			// "score submssion blocked by cheats!",
			// Toast.LENGTH_SHORT).show();
			passLevel();
			return;
			// user is cheatin! no highscores :)
		}
		timeTakenText.setText(String.valueOf(timeTaken));
		int scoreValue = (int) (this.timeTaken * 1000f);
		String textValue = "controls: "+ABControls+" & "+LRControls;
		try {
			
			if ((gameWorld.levelId+1 == StatStuff.packLevelCount[currentPackID])){
				/*Achievement a = new Achievement(StatStuff.packCompletedID[currentPackID]);
				if(!a.isUnlocked){
					a.unlock(null);//
				}*/

				new Achievement(StatStuff.packCompletedID[currentPackID]).unlock(new Achievement.UnlockCB () {
					@Override
					public void onSuccess(boolean newUnlock) {
						//Toast.makeText(GameRoot.this, "Pack Completed!.", Toast.LENGTH_SHORT).show();
					}
					@Override public void onFailure(String exceptionMessage) {
						//Toast.makeText(GameRoot.this, "Error (" + exceptionMessage + ") unlocking achievement.", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			
			String mLeaderboardID = StatStuff.levelScoreIDs[currentPackID][gameWorld.levelId - 1];
			Score s = new Score(scoreValue, (textValue.length() > 0 ? textValue
					: null));
			Leaderboard l = new Leaderboard(mLeaderboardID);

			s.submitTo(l, new Score.SubmitToCB() {

				@Override
				public void onSuccess(boolean newHighScore) {
					// sweet, pop the thingerydingery
					// GameRoot.this.setResult(Activity.RESULT_OK);
					// ScorePoster.this.finish();
					// Toast.makeText(GameRoot.this, "score submitted",
					// Toast.LENGTH_SHORT).show();
					showRank();

				}

				@Override
				public void onFailure(String exceptionMessage) {
					/*
					 * Toast.makeText(GameRoot.this, "Error (" +
					 * exceptionMessage + ") posting score.",
					 * Toast.LENGTH_SHORT).show();
					 */
					// ScorePoster.this.setResult(Activity.RESULT_CANCELED);
					// ScorePoster.this.finish();
				}
			});
		} catch (Exception e) {

		}
		passLevel();
	}

	public void passLevel() {
		/*Log.d(dbt, "+++++++++++++++");
		Log.d(dbt, "in pack:" + currentPackID);
		Log.d(dbt, "completed level:" + gameWorld.levelId);
		Log.d(dbt, "+++++++++++++++");*/

		if (currentPackID == -1)
			return;

		int atLevel = prefs.getInt("atLevel"
				+ StatStuff.packNames[currentPackID], 2);
		if (atLevel < gameWorld.levelId + 2) {
			Editor edit = prefs.edit();
			edit.putInt("atLevel" + StatStuff.packNames[currentPackID],
					gameWorld.levelId + 2);
			edit.commit();
		}

	}

	private void crashBike() {
		if (!canCrash) {
			// float angle = gameWorld.bike.mBody.getAngle();
			// Vector2 v = new
			// Vector2((float)Math.sin(angle)*-150f,(float)Math.cos(angle)*150f);
			// gameWorld.bike.mBody.applyLinearImpulse(v,
			// gameWorld.bike.mBody.getPosition());
			// gameWorld.bike.mBody
			// .applyAngularImpulse((float) (-100f + Math.random() * 200f));
			return;
		}

		this.camera.setHUD(menuHud);
		GameRoot.this.sounds.mCrashSound.play();
		// this.pause();
		gameWorld.bike.detachWheels();
		getBike().setDead(true);
		// isPaused=true;
		mScene.clearChildScene();
		mScene.setChildScene(menus.mMenuDead);
		// mHUD.setVisible(false);
		analogOnScreenControl.resetControlKnobPosition();
		analogOnScreenControl2d.resetControlKnobPosition();
		// GameRoot.this.getBike().stopWheels();

	}

	private void flipBike() {
		gameWorld.bike.flipDirecion();
		flipButton.getTextureRegion().setFlippedHorizontal(
				!flipButton.getTextureRegion().isFlippedHorizontal());
	}

	void showRank() {
		int pack = this.currentPackID;
		int level = this.gameWorld.levelId;
		if (pack != -1) {
			String mLeaderboardID = StatStuff.levelScoreIDs[pack][level - 1];
			// Score s = new Score(scoreValue, (textValue.length() > 0 ?
			// textValue : null));
			Leaderboard l = new Leaderboard(mLeaderboardID);
			// Toast.makeText(GameRoot.this, "collecting high score",
			// Toast.LENGTH_SHORT).show();

			try {
				if (OpenFeint.getCurrentUser() != null)
					l.getUserScore(OpenFeint.getCurrentUser(),
							new Leaderboard.GetUserScoreCB() {

								@Override
								public void onSuccess(Score arg0) {
									if (arg0 != null) {
										// Toast.makeText(GameRoot.this,
										// "Current Rank: "+arg0.rank,
										// Toast.LENGTH_SHORT).show();
										int rank = arg0.rank;
										float score = arg0.score / 1000f;
										personalRankText.setText("" + rank);
										personalRecordTimeText.setText(""
												+ score);
									}

								}

							});

			} catch (Exception e) {

			}
		}
	}

	void getTopScore(int pack, int level) {
		try {
			if (pack != -1) {
				if (!(level < StatStuff.packLevelCount[pack]))
					return;
				// String mLeaderboardID = StatStuff.getScoreID(pack,level - 1)
				String mLeaderboardID = StatStuff.levelScoreIDs[pack][level - 1];
				// Score s = new Score(scoreValue, (textValue.length() > 0 ?
				// textValue : null));
				Leaderboard l = new Leaderboard(mLeaderboardID);
				// Toast.makeText(GameRoot.this, "collecting high score",
				// Toast.LENGTH_SHORT).show();

				l.getScores(new Leaderboard.GetScoresCB() {
					@Override
					public void onSuccess(final List<Score> scores) {
						if (scores == null)
							return;
						if (scores.size() < 1) {
							recordTimeText.setText("None");
							return;
						}
						Score topScore = scores.get(0);
						float a = (float) topScore.score / 1000f;
						;
						recordTimeText.setText("" + a);
						recordTimeUserNameText.setText(topScore.user.name);
						// Toast.makeText(GameRoot.this, "score collected",
						// Toast.LENGTH_SHORT).show();

					}

					@Override
					public void onFailure(String exceptionMessage) {
						recordTimeText.setText("?");
						// Toast.makeText(GameRoot.this, "score not collected",
						// Toast.LENGTH_SHORT).show();
						// setListAdapter(new
						// ArrayAdapter<String>(ScoreExplorer.this,
						// R.layout.main_menu_item, new String[] { "Error (" +
						// exceptionMessage + ")" }));
					}
				});
			}

		} catch (Exception e) {

		}
	}

	@Override
	public void onLoadComplete() {
		if (StatStuff.isDemo)
			loadAds();
		this.isPaused = true;
		gameWorld.initLoaded();

		boolean chosenLvl = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String toLoad = extras
					.getString("com.chozabu.android.BikeGame.toLoad");
			int toLoadId = extras
					.getInt("com.chozabu.android.BikeGame.toLoadId");
			if (toLoad != null) {
				chosenLvl = true;
				gameWorld.loadFromFile(toLoad);
			} else if (toLoadId > 0) {
				currentPackID = extras
						.getInt("com.chozabu.android.BikeGame.levelPack");
				gameWorld.setLevelPack(currentPackID);
				chosenLvl = true;
				gameWorld.levelId = toLoadId;
				gameWorld.loadFromAsset(gameWorld.levelPrefix + toLoadId
						+ ".lvl");
				IRcommon();
			}
		}
		if (!chosenLvl) {
			gameWorld.loadFromAsset("level/intro.lvl");
		}

		this.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				sounds.start();

			}
		});

		loadFinished = true;
		mScene.setChildScene(menus.mMenuBegin);

	}

	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		if (pKeyCode == KeyEvent.KEYCODE_BACK) {
			Intent mainMenuIntent = new Intent(GameRoot.this, AEMainMenu.class);
			startActivity(mainMenuIntent);
			if (sounds != null)
				sounds.stop();
			this.finish();
			// System.exit(0);
			return true;
		}

		if (pEvent.getAction() != KeyEvent.ACTION_UP || !loadFinished
				|| getBike().isDead())
			return super.onKeyDown(pKeyCode, pEvent);
		if (pKeyCode == KeyEvent.KEYCODE_DPAD_UP
				|| pKeyCode == KeyEvent.KEYCODE_W) {
			this.gameWorld.bike.setSpeed(0f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| pKeyCode == KeyEvent.KEYCODE_S) {
			this.gameWorld.bike.setSpeed(0f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| pKeyCode == KeyEvent.KEYCODE_A) {
			this.gameWorld.bike.modRot(0f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| pKeyCode == KeyEvent.KEYCODE_D) {
			this.gameWorld.bike.modRot(0f);
			return true;
		}
		return super.onKeyUp(pKeyCode, pEvent);
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if (pEvent.getAction() != KeyEvent.ACTION_DOWN || !loadFinished
				|| getBike().isDead())
			return super.onKeyDown(pKeyCode, pEvent);
		if (pKeyCode == KeyEvent.KEYCODE_MENU) {
			if (this.mScene.getChildScene() == menus.mMenuFromButton) {
				this.unPause();
				//Debug.startMethodTracing("abike");
			} else if (!this.isPaused) {
				/* Attach the menu. */
				this.pause();
				this.mScene.clearChildScene();
				this.mScene.setChildScene(menus.mMenuFromButton, false, true,
						true);
			}
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_BACK) {
			//Debug.stopMethodTracing();
			// caught ya!
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| pKeyCode == KeyEvent.KEYCODE_SPACE) {
			this.flipBike();
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_UP
				|| pKeyCode == KeyEvent.KEYCODE_W) {
			this.gameWorld.bike.setSpeed(1f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| pKeyCode == KeyEvent.KEYCODE_S) {
			this.gameWorld.bike.setSpeed(-1f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| pKeyCode == KeyEvent.KEYCODE_A) {
			this.gameWorld.bike.modRot(-1f);
			return true;
		} else if (pKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| pKeyCode == KeyEvent.KEYCODE_D) {
			this.gameWorld.bike.modRot(1f);
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
		// if(getBike().isDead())return false;
		if (mScene.getChildScene() == menus.mMenuBegin) {
			this.begin();
			return true;
		}
		return false;
	}

	@Override
	public void onAccelerometerChanged(
			final AccelerometerData pAccelerometerData) {
		// float x = pAccelerometerData.getX() * 0.3f;
		float y = pAccelerometerData.getY() * 0.25f * accelerometerSensitivity;
		// x=MathUtils.bringToBounds(-1f, 1f, x);
		float threshHold = 0.15f * accelerometerDeadZone;
		if (y > -threshHold && y < threshHold) {
			return;
		}
		if (y > 0)
			y -= threshHold;
		else
			y += threshHold;

		y = MathUtils.bringToBounds(-1f, 1f, y);

		// getBike().setSpeed(x);

		getBike().modRot(y);
		// Log.i("ABike", "accel data is X=" + x + "  y=" + y);
		// this.analogOnScreenControl.setControlKnobPosition(y, -x);
	}

	public Scene getScene() {
		return this.mEngine.getScene();
	}

	void begin() {
		if (!gameWorld.bike.facingRight)
			flipBike();
		gameWorld.mPhysicsWorld.clearForces();
		// berrysLeftText.setVisible(gameWorld.berryCount > 0);
		this.timeTaken = 0f;
		unPause();
		getBike().setDead(false);
		Vector2 bPos = getBike().mBody.getPosition().mul(32f);
		camera.setCenter(bPos.x, bPos.y);
	}

	void pause() {
		if (this.isPaused)
			return;
		gameWorld.pause();
		this.isPaused = true;
		// inGameHud.setVisible(false);
		this.camera.setHUD(menuHud);
	}

	void unPause() {
		if (!this.isPaused)
			return;
		this.isPaused = false;
		this.mScene.clearChildScene();
		analogOnScreenControl.resetControlKnobPosition();
		analogOnScreenControl2d.resetControlKnobPosition();
		gameWorld.unPause();
		inGameHud.setVisible(true);
		this.camera.setHUD(inGameHud);
	}

	void restartLevel() {
		
		if(gameWorld.levelFromFile){
			//this.getScene().clearChildScene();
			//this.getScene().setChildScene(this.menus.mMenuLoading);
			this.nextLevel();
			return;
		}
		this.showRank();
		this.pause();
		
		// gameWorld.loadCurrentLevel();
		gameWorld.restartLevel();
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

	private void loadAds() {
		/*
		 * if(Detector.isAdFreeInstalled(this)){
		 * Log.i(dbt,"\n\n\n\n\n\n ADBLOCK DETECTED \n\n\n\n"); }
		 */

		// RelativeLayout layout = (RelativeLayout)
		// findViewById(R.id.layout_main);
		// if (!StatStuff.isDemo) {
		// layout.removeView(findViewById(R.id.adwhirl_layout));

		// return;
		// }

		AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);

		/*
		 * AdWhirlTargeting.setAge(23);
		 * AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
		 * AdWhirlTargeting.setKeywords("online games gaming");
		 * AdWhirlTargeting.setPostalCode("94123");
		 * AdWhirlTargeting.setTestMode(false);
		 */

		AdWhirlLayout adWhirlLayout = (AdWhirlLayout) findViewById(R.id.adwhirl_layout);
		// Log.d("Abike AdWhirl", "layout is: "+adWhirlLayout);

		// TextView textView = new TextView(this);
		/*
		 * RelativeLayout.LayoutParams layoutParams = new
		 * RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		 * LayoutParams.WRAP_CONTENT);
		 */
		int diWidth = 320;
		int diHeight = 52;
		float density = getResources().getDisplayMetrics().density;

		adWhirlLayout.setAdWhirlInterface(this);
		adWhirlLayout.setMaxWidth((int) (diWidth * density));
		adWhirlLayout.setMaxHeight((int) (diHeight * density));

		adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);

		// layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// textView.setText("Below AdWhirlLayout");

		/*
		 * LinearLayout layout = (LinearLayout)findViewById(R.id.layout_main);
		 * 
		 * layout.setGravity(Gravity.CENTER_HORIZONTAL);
		 * layout.addView(adWhirlLayout, layoutParams); layout.addView(textView,
		 * layoutParams); layout.invalidate();
		 */
	}

	public void nextLevel() {
		this.pause();
		this.getScene().clearChildScene();
		this.getScene().setChildScene(this.menus.mMenuLoading);
		this.gameWorld.nextLevel();
		this.recordTimeText.setText("-");
		this.timeTakenText.setText("-");
		this.personalRankText.setText("-");
		this.personalRecordTimeText.setText("-");

		this.IRcommon();

		this.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				GameRoot.this.getScene().setChildScene(
						GameRoot.this.menus.mMenuBegin);

			}
		});

	}

	private void IRcommon() {

		recordTimeUserNameText.setText("-World Record");
		this.getTopScore(this.currentPackID, this.gameWorld.levelId);
		berrysLeftText.setVisible(gameWorld.berryCount > 0);

		this.showRank();
		berrysLeftText.setText(String.valueOf(gameWorld.berryCount));
		// berrysLeftLabelText.setVisible(gameWorld.berryCount > 0);

	}

}
