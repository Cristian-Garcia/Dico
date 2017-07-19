package com.garciacristian.dico;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.Serializable;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.squareup.picasso.Picasso;

public class SpotifyActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback, Serializable
{

    SpotifyService spotify;
    ArrayList<String> PlaylistTrackURI;
    String Credentials = "";

    private static final String CLIENT_ID = "22a9d054d3e7497cbdd69cc24d2124c5";
    private static final String REDIRECT_URI = "Dico://callback";

    private Player mPlayer;

    private static final int REQUEST_CODE = 1337;

    private static final String TAG = SpotifyActivity.class.getSimpleName();

    static final String EXTRA_TOKEN = null;
    // user speech
    private String userSpeech = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        TextView txtSpeechOutput = (TextView) findViewById(R.id.txtSpeechOutput);

        // added 2:22pm 7/18
        // retrieve string from intent
        // should display to screen
        String data = getIntent().getExtras().getString("user_text");
        txtSpeechOutput.setText(data);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // check if result comes from correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(SpotifyActivity.this);
                        mPlayer.addNotificationCallback(SpotifyActivity.this);

                        searchForTrack();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    private void searchForTrack() {
        SpotifyApi api = new SpotifyApi();
        Credentials = CredentialsHandler.getToken(this);
        api.setAccessToken(CredentialsHandler.getToken(this));
        spotify = api.getService();

        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                retrieveTrackURI(userPrivate, "");
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void retrieveTrackURI(UserPrivate userPrivate, String tURI) {

        // retrieve userspeech from calling activity
        Bundle bundle = getIntent().getExtras();
        userSpeech = bundle.getString("user_text");

        spotify.searchTracks(userSpeech, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {

                // for album image
                String albumImageURI = tracksPager.tracks.items.get(0).album.images.get(0).url;
                ImageView imageView = (ImageView) findViewById(R.id.albumImage);
                Picasso.with(getBaseContext()).load(albumImageURI).into(imageView);

                // parse json for track uri
                String userSongURI = tracksPager.tracks.items.get(0).uri;
                mPlayer.playUri(null, userSongURI, 0, 0);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        //playTracks();
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void playTracks() {

        // retrieve data from voice activity
        Bundle bundle = getIntent().getExtras();
        userSpeech = bundle.getString("user_text");

        // render the speech
        logMessage("User said: " + userSpeech);


        // play passionfruit
        mPlayer.playUri(null, "spotify:track:5mCPDVBb16L4XQwDdbRUpz", 0, 0);
    }

    private void logMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }
}
