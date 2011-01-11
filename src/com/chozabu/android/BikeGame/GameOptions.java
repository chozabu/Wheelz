package com.chozabu.android.BikeGame;

import com.chozabu.android.BikeGame.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;


public class GameOptions extends PreferenceActivity {
	
	@Override
	 public void onCreate(Bundle icicle) {
	  super.onCreate(icicle);
	  addPreferencesFromResource(R.xml.gamerefs);
	 }

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if (pEvent.getAction() != KeyEvent.ACTION_DOWN)
			return super.onKeyDown(pKeyCode, pEvent);
	  if (pKeyCode == KeyEvent.KEYCODE_BACK) {
		Intent mainMenuIntent = new Intent(GameOptions.this, AEMainMenu.class);
		startActivity(mainMenuIntent);
		quitFunc();
		return true;
	  }
		return super.onKeyUp(pKeyCode, pEvent);
	}
	
	void quitFunc(){
		this.finish();
		System.exit(0);
	}
}
