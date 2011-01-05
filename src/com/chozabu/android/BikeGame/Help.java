package com.chozabu.android.BikeGame;

import com.chozabu.android.BikeGame.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Help extends Activity implements OnClickListener {

	private Button emailButton;
	private Button optionsButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		// Get referenc to Email Button
		this.emailButton = (Button) this.findViewById(R.id.EmailButton);
		// this.optionsButton = (Button) this.findViewById(R.id.OptionsButton);

		// Sets the Event Listener onClick
		this.emailButton.setOnClickListener(this);
		// this.optionsButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view == this.emailButton) {
			StatStuff.sendMail(this);
		} else if (view == this.optionsButton) {

		}
	}

}