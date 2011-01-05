package com.chozabu.android.BikeGame;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.animator.AlphaMenuAnimator;
import org.anddev.andengine.entity.scene.menu.animator.IMenuAnimator;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.util.AdWhirlUtil;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.chozabu.android.BikeGame.R;
import com.chozabu.android.BikeGame.StatStuff;
import com.flurry.android.FlurryAgent;
import com.openfeint.api.OpenFeint;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

public class AEMainMenu extends LayoutGameActivity implements
		IOnMenuItemClickListener, AdWhirlInterface {

	protected static final int MENU_START = 0;
	protected static final int MENU_GO_ROOT = MENU_START + 1;
	protected static final int MENU_LOAD = MENU_GO_ROOT + 1;
	protected static final int MENU_HELP = MENU_LOAD + 1;
	protected static final int MENU_OPTIONS = MENU_HELP + 1;
	protected static final int MENU_CREDITS = MENU_OPTIONS + 1;
	protected static final int MENU_QUIT = MENU_CREDITS + 1;
	protected static final int MENU_BUY_GAME = MENU_QUIT + 1;
	protected static final int MENU_MORE_LEVELS = MENU_BUY_GAME + 1;
	protected static final int MENU_ORIGNAL_PACK = MENU_MORE_LEVELS + 1;
	protected static final int MENU_XCLASSIC_PACK = MENU_ORIGNAL_PACK + 1;

	protected static final int MENU_LEVELS = MENU_XCLASSIC_PACK + 1;

	ZoomCamera camera;
	Textures textures = new Textures();
	Sounds sounds = new Sounds();
	private MenuScene mainMenu;
	private MenuScene levelPackMenu;
	private MenuScene lockedClassicMenu;
	private MenuScene completedMenu;

	GameWorld gameWorld = new GameWorld();
	private Scene mScene;
	int levelsFrom = 1;

	SharedPreferences prefs;
	// private MenuScene levelsMenu;

	//String currentPack = "orignal";
	int currentPackID = 0;
	private Builder instructionsDialog;

	private boolean loadFinished = false;
	
	String getPackName(int ID){
		return StatStuff.packNames[ID];
	}


	@Override
	public Engine onLoadEngine() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		camera = new ZoomCamera(0, 0, StatStuff.CAMERA_WIDTH,
				StatStuff.CAMERA_HEIGHT);
		// camera.setRotation(180.f);
		// camera.setCameraSceneRotation(50.f);
		// camera.setZoomFactor(.5f);
		gameWorld.initEngine(this, camera);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(StatStuff.CAMERA_WIDTH,
						StatStuff.CAMERA_HEIGHT), camera)
				.setNeedsSound(true));
	}

	@Override
	public void onPause() {
		super.onPause();
		sounds.stop();
		// this.
	}
	

	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, "3HXVXYADVUY16UGGWCWZ");
	   // your code
	}
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	   // your code
	}

	@Override
	public void onResume() {
		super.onResume();
		// if(this.getEngine()!=null)
		// Sounds.init(this);
	    	OpenFeint.setCurrentActivity(this);
		sounds.start();
	}

	public void onLoadResources() {

		textures.init(this);
		sounds.init(this);
		gameWorld.initRes(textures, sounds);

	}

	@Override
	public Scene onLoadScene() {
		this.mainMenu = this.createMenuScene();
		this.levelPackMenu = this.createLevelPackMenuScene();
		this.lockedClassicMenu = this.createLockedClassicMenuScene();
		this.completedMenu = this.createPackCompleteMenuScene();
		// this.levelsMenu = this.createLevelMenuScene();

		this.mScene = new Scene(4);

		gameWorld.initScene(this.mScene);

		this.mScene.clearChildScene();
		this.mScene.setChildScene(mainMenu);
		return this.mScene;
	}

	@Override
	public void onLoadComplete() {
		if (StatStuff.isDemo)loadAds();
		gameWorld.initLoaded();

		boolean completed = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			completed = extras.getBoolean(
					"com.chozabu.android.BikeGame.gameComplete", false);
		}
		if (completed) {
			gameWorld.loadFromAsset("level/ending.lvl");
			this.mScene.clearChildScene();
			this.mScene.setChildScene(this.completedMenu);
		} else {
			if (Math.random() > 0.1)
				gameWorld.loadFromAsset("level/intro.lvl");
			else
				gameWorld.loadFromAsset("level/intro2.lvl");
		}

		// camera.setChaseShape(gameWorld.bike.mBodyImg);
		// new Bike(gameWorld, gameWorld.playerStart);
		gameWorld.unPause();

		gameWorld.bike.setSpeed(.3f + (float) Math.random() * .7f);

		gameWorld.mPhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void beginContact(Contact contact) {

				boolean reset = false;
				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();

				if (gameWorld.endList.contains(bodyA)
						|| gameWorld.endList.contains(bodyB))
				 if (gameWorld.bike.containsBody(bodyA)
				 || gameWorld.bike.containsBody(bodyB))
				{

					gameWorld.bike.mBody
							.applyAngularImpulse((float) (-50f + Math.random() * 100f));
					return;
				}

				// truck roof hit!
				if (bodyA == gameWorld.bike.roofSensor
						|| bodyB == gameWorld.bike.roofSensor) {
					if (Math.random() > 0.95)
						gameWorld.bike.flipDirecion();

					gameWorld.bike.setSpeed(.4f + (float) Math.random() * .6f);
					float angle = gameWorld.bike.mBody.getAngle();
					Vector2 v = new Vector2((float)Math.sin(angle)*-150f,(float)Math.cos(angle)*150f);
					gameWorld.bike.mBody.applyLinearImpulse(v, gameWorld.bike.mBody.getPosition());
					gameWorld.bike.mBody
							.applyAngularImpulse((float) (-100f + Math.random() * 200f));
				}
			}

			@Override
			public void endContact(Contact contact) {

			}

		});

		this.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				sounds.start();
			}
		});

		//backward compatability
		int atLevelnew = prefs.getInt("atLevel" + getPackName(currentPackID), 2);
		int atLevel = prefs.getInt("atLevel", 2);
		int atLevelx = prefs.getInt("atLevelorignal", 2);
		atLevel=Math.max(atLevel, atLevelx);

		if (atLevelnew < atLevel) {
			Editor edit = prefs.edit();
			edit.putInt("atLevel" + getPackName(currentPackID), atLevel);
			edit.commit();
		}

		//Editor edit = prefs.edit();
		//edit.remove("showIntro");
		//edit.commit();
		int playCount = prefs.getInt("playCount", 0);
		boolean seenInfo = prefs.getBoolean("seenInfo", false);
		Log.d("ABike","\n\n\n\n\n PLAY COUNT: "+playCount);
		if(playCount==9 && !seenInfo){
			makeInfo();
			this.instructionsDialog.show();
			Editor edit = prefs.edit();
			edit.putBoolean("seenInfo", true);
			edit.commit();
		} else if(prefs.getBoolean("showIntro", true)){
			makeIntro();
			this.instructionsDialog.show();
		}
		

		loadFinished = true;

	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(!loadFinished)return super.onKeyDown(pKeyCode, pEvent);
		if(this.mainMenu == null) return true;
		if (pEvent.getAction() != KeyEvent.ACTION_DOWN)
			return super.onKeyDown(pKeyCode, pEvent);
		if (pKeyCode == KeyEvent.KEYCODE_BACK) {
			if (this.mScene.getChildScene() != this.mainMenu) {
				return true;
			}
		}

		return super.onKeyDown(pKeyCode, pEvent);
	}

	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		if(!loadFinished)return super.onKeyUp(pKeyCode, pEvent);
		if(this.mainMenu == null) return true;
		if (this.mScene == null)
			return super.onKeyUp(pKeyCode, pEvent);
		if (pKeyCode == KeyEvent.KEYCODE_BACK) {
			if (this.mScene.getChildScene() != this.mainMenu) {

				this.mScene.clearChildScene();
				this.mScene.setChildScene(mainMenu);
				return true;
			}
		}
		return super.onKeyUp(pKeyCode, pEvent);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene,
			final IMenuItem pMenuItem, final float pMenuItemLocalX,
			final float pMenuItemLocalY) {
		this.sounds.mBeBoopSound.play();
		int itemId = pMenuItem.getID();
		switch (itemId) {
		case MENU_START:
			this.mScene.clearChildScene();
			// levelsFrom = 1;
			this.mScene.setChildScene(this.levelPackMenu);
			return true;
		case MENU_ORIGNAL_PACK:
			this.mScene.clearChildScene();
			levelsFrom = 1;
			currentPackID = StatStuff.originalPackID;
			this.mScene.setChildScene(createLevelMenuScene(5));
			return true;
		case MENU_XCLASSIC_PACK:
			if(StatStuff.isDemo){
				this.mScene.setChildScene(this.lockedClassicMenu);
				//Toast.makeText(this, "This 32 level pack only Avalable in full game!", Toast.LENGTH_LONG).show();
				return true;
			}
			this.mScene.clearChildScene();
			levelsFrom = 1;
			currentPackID = StatStuff.xmClassicPackID;
			this.mScene.setChildScene(createLevelMenuScene(5));
			return true;
		case MENU_LOAD:
			Intent LoadGameIntent = new Intent(AEMainMenu.this, LoadList.class);
			startActivity(LoadGameIntent);
			this.finish();
			return true;

		case MENU_HELP:
			Intent GameHelpIntent = new Intent(AEMainMenu.this, Help.class);
			startActivity(GameHelpIntent);
			return true;

		case MENU_BUY_GAME:
			StatStuff.marketFull(this);
			this.finish();
			return true;
		case MENU_GO_ROOT:
			this.mScene.clearChildScene();
			this.mScene.setChildScene(mainMenu);
			return true;
			
		case MENU_OPTIONS:
			Intent GameOptionsIntent = new Intent(AEMainMenu.this,
					GameOptions.class);
			startActivity(GameOptionsIntent);
			this.finish();
			return true;
		case MENU_CREDITS:
			Intent CreditsIntent = new Intent(AEMainMenu.this, Credits.class);
			startActivity(CreditsIntent);
			return true;
		case MENU_MORE_LEVELS:
			this.mScene.clearChildScene();
			levelsFrom += 5;
			this.mScene.setChildScene(createLevelMenuScene(5));
			return true;
		case MENU_QUIT:
			this.finish();
			return true;
		}

		if (itemId > MENU_LEVELS) {

			Intent StartGameIntent = new Intent(AEMainMenu.this, GameRoot.class);
			StartGameIntent.putExtra("com.chozabu.android.BikeGame.toLoadId",
					itemId - MENU_LEVELS);
			StartGameIntent.putExtra("com.chozabu.android.BikeGame.levelPack",
					currentPackID);
			startActivity(StartGameIntent);
			
			
			float time = Float.parseFloat("0.5");
			time = (float) Math.sqrt(time);
			if(time<1) time=1;
			
			this.finish();
			return true;
		}

		return true;
	}

	protected MenuScene createMenuScene() {
		final MenuScene menuScene = new MenuScene(camera);
		// menuScene.

		final TextMenuItem titleMenuItem = new TextMenuItem(-1, textures.mFont,
				" WHEELZ");
		titleMenuItem.setScale(1.2f);
		titleMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(titleMenuItem);

		final TextMenuItem newGameMenuItem = new TextMenuItem(MENU_START,
				textures.mFont, "START GAME");
		newGameMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(newGameMenuItem);

		final TextMenuItem helpMenuItem = new TextMenuItem(MENU_HELP,
				textures.mFont, "HELP");
		helpMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(helpMenuItem);

		final TextMenuItem optionsMenuItem = new TextMenuItem(MENU_OPTIONS,
				textures.mFont, "CONTROLS + SETTINGS");
		optionsMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(optionsMenuItem);

		final TextMenuItem creditsMenuItem = new TextMenuItem(MENU_CREDITS,
				textures.mFont, "CREDITS");
		creditsMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(creditsMenuItem);

		final TextMenuItem quitMenuItem = new TextMenuItem(MENU_QUIT,
				textures.mFont, "QUIT");
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		IMenuAnimator ma = new AlphaMenuAnimator(StatStuff.menuSpacing);
		menuScene.setMenuAnimator(ma);
		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}

	protected MenuScene createLevelPackMenuScene() {
		final MenuScene menuScene = new MenuScene(camera);
		// menuScene.

		final TextMenuItem orignalMenuItem = new TextMenuItem(
				MENU_ORIGNAL_PACK, textures.mFont, "Original Levels");
		orignalMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(orignalMenuItem);

		final TextMenuItem xclassicMenuItem = new TextMenuItem(
				MENU_XCLASSIC_PACK, textures.mFont, "Xmoto classic");
		xclassicMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(xclassicMenuItem);

		final TextMenuItem loadGameMenuItem = new TextMenuItem(MENU_LOAD,
				textures.mFont, "CUSTOM LEVEL");
		loadGameMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(loadGameMenuItem);

		IMenuAnimator ma = new AlphaMenuAnimator(StatStuff.menuSpacing);
		menuScene.setMenuAnimator(ma);
		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}

	protected MenuScene createLockedClassicMenuScene() {
		final MenuScene menuScene = new MenuScene(camera);
		// menuScene.

		final TextMenuItem orignalMenuItem = new TextMenuItem(
				MENU_BUY_GAME, textures.mFont, "This 32 level pack available in full game");
		orignalMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(orignalMenuItem);

		final TextMenuItem buyMenuItem = new TextMenuItem(
				MENU_BUY_GAME, textures.mFont, "Touch here to buy");
		buyMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(buyMenuItem);
		

		final TextMenuItem newGameMenuItem = new TextMenuItem(MENU_START,
				textures.mFont, "Return to Main Menu");
		newGameMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(newGameMenuItem);


		IMenuAnimator ma = new AlphaMenuAnimator(StatStuff.menuSpacing);
		menuScene.setMenuAnimator(ma);
		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}
	protected MenuScene createPackCompleteMenuScene() {
		final MenuScene menuScene = new MenuScene(camera);
		// menuScene.

		final TextMenuItem orignalMenuItem = new TextMenuItem(
				MENU_GO_ROOT, textures.mFont, "Level Pack Completed!");
		orignalMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(orignalMenuItem);
		

		final TextMenuItem newGameMenuItem = new TextMenuItem(MENU_GO_ROOT,
				textures.mFont, "Go Back");
		newGameMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(newGameMenuItem);


		IMenuAnimator ma = new AlphaMenuAnimator(StatStuff.menuSpacing);
		menuScene.setMenuAnimator(ma);
		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}

	protected MenuScene createLevelMenuScene(int count) {
		final MenuScene menuScene = new MenuScene(camera);

		final TextMenuItem levelMenuTitle = new TextMenuItem(MENU_LEVELS
				+ levelsFrom, textures.mFont, "Pick a Level");
		levelMenuTitle.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(levelMenuTitle);

		int atLevel;
		int maxLvl = 0;

		/*if (currentPack.compareTo(StatStuff.orignalPack) == 0) {
			maxLvl = StatStuff.lvlMax;
		} else if (currentPack.compareTo(StatStuff.xmClassicPack) == 0) {
			maxLvl = StatStuff.lvlMaxClassic;
		}*/
		maxLvl = StatStuff.packLevelCount[currentPackID];
		atLevel = prefs.getInt("atLevel" + this.getPackName(currentPackID), 2);

		int end = levelsFrom + count;
		for (int levelId = levelsFrom; levelId < end; levelId++) {
			if (levelId < atLevel && levelId < maxLvl) {
				final TextMenuItem leveleMenuItem = new TextMenuItem(
						MENU_LEVELS + levelId, textures.mFont, "- LEVEL "
								+ levelId + " -");
				leveleMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				menuScene.addMenuItem(leveleMenuItem);
			} else {
				if (levelId + 1 > maxLvl) {
					/*
					 * final TextMenuItem leveleMenuItem = new TextMenuItem(-1 ,
					 * textures.mFont, "- LEVEL Under Construction -");
					 * leveleMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
					 * GL10.GL_ONE_MINUS_SRC_ALPHA);
					 * menuScene.addMenuItem(leveleMenuItem);
					 */
					break;
				}
				final TextMenuItem leveleMenuItem = new TextMenuItem(-1,
						textures.mFont, "- LEVEL LOCKED -");
				leveleMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				menuScene.addMenuItem(leveleMenuItem);
			}
		}
		if (atLevel > end) {
			final TextMenuItem leveleMenuItem = new TextMenuItem(
					MENU_MORE_LEVELS, textures.mFont, "More");
			leveleMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(leveleMenuItem);
		}
		IMenuAnimator ma = new AlphaMenuAnimator(StatStuff.menuSpacing);
		menuScene.setMenuAnimator(ma);
		menuScene.buildAnimations();
		menuScene.setBackgroundEnabled(false);
		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
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
	
	private void makeIntro(){
        instructionsDialog = new AlertDialog.Builder(this);
        instructionsDialog.setTitle("Important");
        String dTxt = "";
        //if (isDemo) dTxt = "Lite Version - cannot save or upload.\n";
        instructionsDialog.setMessage(dTxt+"" +
        		"Look in Options to change the controls \n" +
        		"Help will give you details of controls \n" +
        		"email me with any questions or requests! \n" +
        		"There is no way to respond to market place comments," +
        		" if you email me I will let you know when a bug/feature is done :)");
        instructionsDialog.setPositiveButton("Don't show Again", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor edit = prefs.edit();
				edit.putBoolean("showIntro", false);
				edit.commit();
			}
        	
        });
        instructionsDialog.setNegativeButton("OK", null);
	}
	
	private void makeInfo(){
        instructionsDialog = new AlertDialog.Builder(this);
        instructionsDialog.setTitle("Having Fun?");
        String dTxt = "";
        if (StatStuff.isDemo) dTxt = "\nAlso the full version of the game has Alot more levels and no Adverts!";
        instructionsDialog.setMessage(
        		"Looks Like you've been playing for a while :) \n" +
        		"I hope you are enjoying it! Please consider leaving a nice comment and 5-Star rating"+dTxt);
        instructionsDialog.setPositiveButton("Leave comment", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(StatStuff.isDemo)
					StatStuff.marketLite(AEMainMenu.this);
				else
					StatStuff.marketFull(AEMainMenu.this);
			}
        	
        });
        if (StatStuff.isDemo)
        instructionsDialog.setNeutralButton("Full version", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatStuff.marketFull(AEMainMenu.this);
			}
        	
        });
        instructionsDialog.setNegativeButton("No Thanks!", null);
	}
	

	private void loadAds() {

		//RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_main);
		//if (!StatStuff.isDemo) {
			//layout.removeView(findViewById(R.id.adwhirl_layout));
			
			//return;
		//}

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

}
