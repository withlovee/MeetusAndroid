package xyz.meetus.meetus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Messenger;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LocationIntentService extends IntentService {

    private ParseUser user;
    private ParseObject locationObj;

    public LocationIntentService() {

        super("LocationIntentService");
        user = ParseUser.getCurrentUser();

        locationObj = user.getParseObject("locationId");
        try {
            locationObj.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

            Log.d("LocationIntentService", location.getLatitude() + " " + location.getLongitude());

            // send req to server
            sendLocation(location.getLatitude(), location.getLongitude());

        }
    }


    private void setupUser(String session) {
        try {
            user = ParseUser.become(session);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private void sendLocation(Double lat, Double lng){

        ParseGeoPoint point = new ParseGeoPoint(lat, lng);

        try {
            locationObj.fetchIfNeeded();
            locationObj.put("location", point);
            locationObj.saveInBackground();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseObject location = new ParseObject("Log");
        location.put("userId", user.getObjectId());
        location.put("location", point);
        location.saveInBackground();

    }
}
