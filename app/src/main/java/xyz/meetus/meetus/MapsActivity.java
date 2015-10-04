package xyz.meetus.meetus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Marker now;
    private LatLng currentPoint;
    private PendingIntent pendingIntent;
    private LocationRequest mLocationRequest;


    private long UPDATE_INTERVAL = 30000;  /* 30 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private Switch sLocation;
    private EditText etStatus;
    private ParseUser user;
    private ParseObject locationObj;
    private RelativeLayout rlOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setupUser();
        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();


        // Connect the client.
        mGoogleApiClient.connect();

    }

    private void setupView() {

        rlOverlay = (RelativeLayout) findViewById(R.id.rlOverlay);

        sLocation = (Switch) findViewById(R.id.sLocation);
        Boolean switchedOn = fetchSwitch();
        sLocation.setChecked(switchedOn);
        setGPSService(switchedOn);

        etStatus = (EditText) findViewById(R.id.etStatus);
        etStatus.setText(fetchStatus());

        setupListeners();

    }

    private void setGPSService(Boolean switchedOn) {
        if(switchedOn){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, pendingIntent);
            rlOverlay.setVisibility(RelativeLayout.GONE);
            Toast.makeText(MapsActivity.this, "Location tracking turned on", Toast.LENGTH_SHORT).show();
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
            rlOverlay.setVisibility(RelativeLayout.VISIBLE);
            Toast.makeText(MapsActivity.this, "Location tracking turned off", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        etStatus.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(MapsActivity.this, "Saving...", Toast.LENGTH_SHORT).show();
                    String newStatus = etStatus.getText().toString();
                    setStatus(newStatus);
                    return true;
                }
                return false;
            }
        });

        sLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(MapsActivity.this, "Saving...", Toast.LENGTH_SHORT).show();
                setSwitch(isChecked);
            }
        });
    }

    private void setStatus(String status){
        locationObj.put("note", status);
        try {
            locationObj.save();
            Toast.makeText(MapsActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
            Log.d("setStatus", "saved " + status);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setSwitch(boolean on){
        locationObj.put("track", on);
        try {
            locationObj.save();
            setGPSService(on);
            Log.d("setSwitch", "saved");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        } else {
            updateMarker();
        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if(lastLocation == null){
            updateMarker();
//        } else {
//            updateMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
//        }
    }

    private void updateMarker() {

        if(now != null){
            now.remove();
        }

        // If there is no location found
        if(currentPoint == null) {
            // Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        now = mMap.addMarker(new MarkerOptions().position(currentPoint).title("Current location"));

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPoint));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }


    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null) {
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            currentPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        startLocationUpdates();
        setupView();

//        LocationRequest locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
//                .setFastestInterval(5000L)
//                .setInterval(10000L)
//                .setSmallestDisplacement(75.0F);

    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Update in foreground
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);

        // Update in background
        pendingIntent = PendingIntent.getService(this, 0,
                new Intent(this, LocationIntentService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        Log.d("onDestroy", "Destroy called");
        setSwitch(false);

        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();

        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        currentPoint = new LatLng(location.getLatitude(), location.getLongitude());
        updateMarker();
        Log.d("Updated: ", msg);
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }


    private void setupUser() {

        String session = getIntent().getStringExtra("session");

        try {

            user = ParseUser.become(session);
            locationObj = user.getParseObject("locationId");
            locationObj.fetch();

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private String fetchStatus() {

        String status = locationObj.getString("note");

        if (status != null) {
            return status;
        } else {
            return "";
        }

    }

    private Boolean fetchSwitch() {

        Boolean switchedOn = locationObj.getBoolean("track");

        if (switchedOn != null) {
            return switchedOn;
        } else {
            setSwitch(true);
            return true;
        }

    }
}