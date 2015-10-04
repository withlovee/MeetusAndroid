package xyz.meetus.meetus;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();

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

                if (user == null) {
                    tvStatus.setText("You cancelled the Facebook login. Let's try again one more time.");
                    Log.d("FacebookLogin", "Uh oh. The user cancelled the Facebook login.");
                    if (err != null) {
                        Log.d("FacebookLogin", err.toString());
                    }
                } else if (user.isNew()) {
                    Toast.makeText(mainActivity, "You're logged in! Welcome! Please wait...", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("");
                    Log.d("FacebookLogin", "User signed up and logged in through Facebook!" + user.getSessionToken());
                    getFacebookId(user);
                } else {
                    Toast.makeText(mainActivity, "You're in! Loading...", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("");
                    Log.d("FacebookLogin", "You're in!" + user.getSessionToken());
                    startMapActivity(user.getSessionToken());
                }
            }
        });

    }

    private void createLocation(final ParseUser user, String facebookId, String facebookName) {

        final ParseObject location = new ParseObject("Location");
        location.put("track", true);
        location.put("note", "");

        location.put("fbId", facebookId);
        location.put("fbName", facebookName);
        location.put("userId", user);

        ParseACL locationACL = new ParseACL(user);
        locationACL.setPublicReadAccess(true);
        location.setACL(locationACL);

        try {
            location.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        location.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                String locationId = parseObject.getObjectId();

                location.saveInBackground();

                user.put("locationId", location);
                user.saveInBackground();

                startMapActivity(user.getSessionToken());
            }
        });



    }

    private void getFacebookId(final ParseUser user) {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            String facebookId = object.getString("id");
                            String facebookName = object.getString("name");
                            createLocation(user, facebookId, facebookName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private void startMapActivity(String sessionToken) {
        Log.d("startMapActivity", "start another activity");
        // Create an intent
        Intent i = new Intent(MainActivity.this, MapsActivity.class);

        // Pass sessionToken
        i.putExtra("session", sessionToken);

        // Close this activity
        finish();

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
