package com.garciacristian.dico;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    Button startButton;
    Button quitButton;

    static final int LOGIN_REQUEST = 1;
    static String EXTRA_TOKEN = "EXTRA_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(SpotifyLoginActivity.EXTRA_TOKEN == null) {
            Intent spotifyIntent = new Intent(MainActivity.this, SpotifyLoginActivity.class);
            startActivity(spotifyIntent);
        } else {
            startButton = (Button) findViewById(R.id.btnStart);
            quitButton = (Button) findViewById(R.id.btnQuit);

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(MainActivity.this, VoiceActivity.class);
                    startActivity(myIntent);
                }
            });

            quitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    System.exit(0);
                }
            });
        }

    }

    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
}