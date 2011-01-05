package com.chozabu.android.BikeGame;

import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.graphics.Color;

public class Textures {

	Texture mFrostGroundTex;
	Texture mDarkFrostGroundTex;
	Texture mMoltenRockTex;
	Texture mBricksTex;
	Texture mGrassTex;
	Texture mGrass2Tex;
	Texture mSnowTex;
	Texture mIce2Tex;
	Texture mWoodTex;
	Texture mWood2Tex;
	Texture mWood3Tex;
	Texture mEarthTex;
	Texture mDarkEarthTex;
	Texture mFaceTexture;//NOT USED?
	Texture mBoxTexture;//NOT USED?
	TextureRegion mFaceTextureRegion;//NOT USED
	TextureRegion mBoxFaceTextureRegion;//NOT USED

	Texture mFinishTexture;
	TextureRegion mFinishTextureRegion;

	Texture mStrawBerryTexture;
	TextureRegion mStrawBerryTextureRegion;

	Texture mWreckerTexture;
	TextureRegion mWreckerTextureRegion;

	Texture mTreeTexture;
	TextureRegion mTreeTextureRegion;
	
	Texture mTruckTex;
	TextureRegion mTruckBodyTextureRegion;
	TextureRegion mTruckFWheelTextureRegion;
	TextureRegion mTruckBWheelTextureRegion;
	TextureRegion mDirtClodTextureRegion;
	
	Texture mBackGroundTexture;
	TextureRegion mBackGroundTextureRegion;

	Texture defaultTexture;
	
	Texture mOnScreenControlTexture;
	Texture mOnScreenControlTexture2d;
	TextureRegion mOnScreenControlBaseTextureRegion;
	TextureRegion mOnScreenControlBaseTextureRegion2d;
	TextureRegion mOnScreenControlKnobTextureRegion;

	Texture mFlipTexture;
	TextureRegion mFlipTextureRegion;
	Texture mControlButtonsTexture;
	TextureRegion mAccelerateButtonRegion;
	TextureRegion mBrakeButtonRegion;
	TextureRegion mLeanLeftButtonRegion;
	TextureRegion mLeanRightButtonRegion;

	Texture mAccelBarTexture;
	TextureRegion mAccelBarTextureRegion;

	Texture mFontTexture;
	Font mFont;
	
	BaseGameActivity root = null;
	TextureRegion mEarthTextureRegion;
	
	void init(BaseGameActivity pRoot){
		this.root = pRoot;
		
		this.mFaceTexture = new Texture(64, 64,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFaceTextureRegion = TextureRegionFactory.createFromAsset(
				this.mFaceTexture, root, "gfx/ball.png", 0, 0);

		this.mBoxTexture = new Texture(32, 32,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBoxFaceTextureRegion = TextureRegionFactory.createFromAsset(
				this.mBoxTexture, root, "gfx/face_box.png", 0, 0);

		this.mTruckTex = new Texture(128, 128, TextureOptions.DEFAULT);
		this.mTruckBodyTextureRegion = TextureRegionFactory.createFromAsset(
				this.mTruckTex, root, "gfx/carbody.png", 0, 0);
		this.mTruckFWheelTextureRegion = TextureRegionFactory.createFromAsset(
				this.mTruckTex, root, "gfx/leftWheel.png", 0, 49);
		this.mTruckBWheelTextureRegion = TextureRegionFactory.createFromAsset(
				this.mTruckTex, root, "gfx/rightWheel.png", 42, 49);
		this.mDirtClodTextureRegion = TextureRegionFactory.createFromAsset(
				this.mTruckTex, root, "gfx/dirtclod.png", 84, 49);

		this.mBackGroundTexture = new Texture(256, 256,
				TextureOptions.REPEATING_BILINEAR);
		this.mBackGroundTextureRegion = TextureRegionFactory.createFromAsset(
				mBackGroundTexture, root, "gfx/stars.jpg", 0, 0);

		//
		this.mOnScreenControlTexture = new Texture(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, root,
						"gfx/onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, root,
						"gfx/onscreen_control_knob.png", 128, 0);

		this.mOnScreenControlTexture2d = new Texture(128, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion2d = TextureRegionFactory
		.createFromAsset(this.mOnScreenControlTexture2d, root,
				"gfx/onscreen_control_base_2d.png", 0, 0);


		this.mFlipTexture = new Texture(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFlipTextureRegion = TextureRegionFactory
				.createFromAsset(this.mFlipTexture, root,
						"gfx/flip.png", 0, 0);

		this.mControlButtonsTexture = new Texture(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mAccelerateButtonRegion = TextureRegionFactory
		.createFromAsset(this.mControlButtonsTexture, root,
				"gfx/accelerate.png", 0, 0);
		this.mBrakeButtonRegion = TextureRegionFactory
		.createFromAsset(this.mControlButtonsTexture, root,
				"gfx/brake.png", 128, 0);
		this.mLeanLeftButtonRegion = TextureRegionFactory
		.createFromAsset(this.mControlButtonsTexture, root,
				"gfx/leanleft.png", 0, 128);
		this.mLeanRightButtonRegion = TextureRegionFactory
		.createFromAsset(this.mControlButtonsTexture, root,
				"gfx/leanright.png", 128, 128);

		this.mAccelBarTexture = new Texture(64, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mAccelBarTextureRegion = TextureRegionFactory
				.createFromAsset(this.mAccelBarTexture, root,
						"gfx/accelBar.png", 0, 0);


		this.mDarkFrostGroundTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mDarkFrostGroundTex, root, "gfx/darkfrostground.jpg", 0, 0);
		this.mFrostGroundTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mFrostGroundTex, root, "gfx/frostground.jpg", 0, 0);

		this.mMoltenRockTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mMoltenRockTex, root, "gfx/moltenrock.jpg", 0, 0);
		
		this.mBricksTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mBricksTex, root, "gfx/Bricks.png", 0, 0);
		
		this.mGrassTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mGrassTex, root, "gfx/grass.jpg", 0, 0);
		this.mGrass2Tex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mGrass2Tex, root, "gfx/Grass2.jpg", 0, 0);

		this.mSnowTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mSnowTex, root, "gfx/snow.jpg", 0, 0);
		this.mIce2Tex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mIce2Tex, root, "gfx/ice.jpg", 0, 0);

		this.mWood2Tex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mWood2Tex, root, "gfx/wood.jpg", 0, 0);
		this.mWoodTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mWoodTex, root, "gfx/Wood2.jpg", 0, 0);
		this.mWood3Tex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mWood3Tex, root, "gfx/Wood3.jpg", 0, 0);
		this.mEarthTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);

		this.mEarthTextureRegion = TextureRegionFactory.createFromAsset(
				this.mEarthTex, root, "gfx/earth.jpg", 0, 0);
		//TextureRegionFactory.addTextureSourceFromAsset(this.mEarthTex, root, "gfx/earth.jpg", 0, 0);
		this.mDarkEarthTex = new Texture(128, 128, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.mDarkEarthTex, root, "gfx/earth2.jpg", 0, 0);

		this.mFinishTexture = new Texture(64, 64, TextureOptions.REPEATING_BILINEAR);
		this.mFinishTextureRegion = TextureRegionFactory.createFromAsset(
				this.mFinishTexture, root, "gfx/checksphere.png", 0, 0);

		this.mStrawBerryTexture = new Texture(64, 64, TextureOptions.REPEATING_BILINEAR);
		this.mStrawBerryTextureRegion = TextureRegionFactory.createFromAsset(
				this.mStrawBerryTexture, root, "gfx/strawBerry.png", 0, 0);

		this.mWreckerTexture = new Texture(64, 64, TextureOptions.REPEATING_BILINEAR);
		this.mWreckerTextureRegion = TextureRegionFactory.createFromAsset(
				this.mWreckerTexture
				, root, "gfx/wrecker.png", 0, 0);
		
		this.mTreeTexture = new Texture(512, 512, TextureOptions.REPEATING_BILINEAR);
		this.mTreeTextureRegion = TextureRegionFactory.createFromAsset(
				this.mTreeTexture
				, root, "gfx/quaking_aspen.png", 0, 0);
		

		
		//default tex
		this.defaultTexture = new Texture(32, 32, TextureOptions.REPEATING_BILINEAR);
		TextureRegionFactory.addTextureSourceFromAsset(this.defaultTexture, root, "gfx/face_box.png", 0, 0);
		
		root.getEngine().getTextureManager().loadTextures(this.mFaceTexture,
				this.mBoxTexture, this.mTruckTex, this.mBackGroundTexture,
				this.mOnScreenControlTexture, this.mOnScreenControlTexture2d,
				this.mGrassTex, this.mGrass2Tex, this.mBricksTex,
				this.mEarthTex, this.mDarkEarthTex,
				this.mWoodTex, this.mWood2Tex,this.mWood3Tex,
				this.mIce2Tex,this.mSnowTex,
				this.mFinishTexture,this.mFlipTexture,this.mAccelBarTexture,this.mControlButtonsTexture,
				this.mStrawBerryTexture,this.mWreckerTexture,
				this.mFrostGroundTex, this.mDarkFrostGroundTex,this.mMoltenRockTex,
				this.mTreeTexture
		);
		
		
		//font tex
		loadFont(root);

		root.getEngine().getTextureManager().loadTexture(this.defaultTexture);
	}
	public void loadFont(BaseGameActivity root){
		this.mFontTexture = new Texture(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, root, "Abduction.ttf", 42, true, Color.WHITE);
		root.getEngine().getTextureManager().loadTexture(this.mFontTexture);
		root.getEngine().getFontManager().loadFont(this.mFont);
	}
}
