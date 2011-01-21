package com.chozabu.android.BikeGame;

import org.anddev.andengine.entity.scene.Scene;

import android.view.KeyEvent;

public interface GameScene {
	
	Scene onLoadScene();
	void onLoadComplete();

	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent);
	
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent);
	void frameUpdate(float pSecondsElapsed);
}
