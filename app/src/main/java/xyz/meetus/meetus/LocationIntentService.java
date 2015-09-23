package xyz.meetus.meetus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Messenger;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LocationIntentService extends IntentService {

    public LocationIntentService() {

        super("LocationIntentService");
        user = ParseUser.getCurrentUser();

    }
    private ParseUser user;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            Log.d("LocationIntentService", location.getLatitude() + " " + location.getLongitude());

            // send req to server
            sendLocation(location.getLatitude(), location.getLongitude());

            // vibrate
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(300);

        }
    }


    private void sendLocation(Double lat, Double lng){

        ParseGeoPoint point = new ParseGeoPoint(lat, lng);
        user.put("location", point);
        user.saveInBackground();

    }
}
