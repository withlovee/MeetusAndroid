package xyz.meetus.meetus;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();

        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseFacebookUtils.initialize(this);

        doFacebookLogin();

    }

    private void setupView() {

        tvStatus = (TextView) findViewById(R.id.tvStatus);

    }

    private void doFacebookLogin(){

        final MainActivity mainActivity = this;
        final List<String> permissions = Arrays.asList("public_profile");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
                if (user == null) {
//                    Toast.makeText(mainActivity, "Uh oh. The user cancelled the Facebook login.", Toast.LENGTH_LONG).show();
                    tvStatus.setText("You cancelled the Facebook login. Let's try again one more time.");
                    Log.d("FacebookLogin", "Uh oh. The user cancelled the Facebook login.");
                    if (err != null) {
                        Log.d("FacebookLogin", err.toString());
                    }
//                    doFacebookLogin();
                } else if (user.isNew()) {
                    Toast.makeText(mainActivity, "You're logged in! Welcome! Please wait...", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("");
                    Log.d("FacebookLogin", "User signed up and logged in through Facebook!" + user.getSessionToken());
                    startMapActivity(user.getSessionToken());
                } else {
                    Toast.makeText(mainActivity, "You're in! Loading...", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("");
                    Log.d("FacebookLogin", "You're in!" + user.getSessionToken());
                    startMapActivity(user.getSessionToken());
                }
            }
        });

    }

    private void startMapActivity(String sessionToken) {
        Log.d("startMapActivity", "start another activity");
        // Create an intent
        Intent i = new Intent(MainActivity.this, MapsActivity.class);

        // Pass sessionToken
        i.putExtra("session", sessionToken);

        // Launch the new activity
        startActivity(i);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
