package com.chozabu.android.BikeGame;

import com.chozabu.android.BikeGame.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Credits extends Activity implements OnClickListener {
	
	private Button emailButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits);      
        


		// Get referenc to Email Button
		this.emailButton = (Button) this.findViewById(R.id.EmailButton);

		// Sets the Event Listener onClick
		this.emailButton.setOnClickListener(this);
              
    }
    

	@Override
	public void onClick(View view) {
			if (view == this.emailButton)
				{
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("text/html");
					emailIntent.putExtra(android.content.Intent.EXTRA_TITLE, getString(R.string.email_name));
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_name));
					String[] address = new String[1];
					address[0] = "chozabu@gmail.com";
					//String[] address2 = String[new String("a")];
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, address);

					// Obtain refenerenc to String and pass it to Intent
					//emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.my_text));
					startActivity(emailIntent);
				}
		}
}