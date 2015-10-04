package xyz.meetus.meetus;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by vee on 9/24/15.
 */
public class MeetusApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseFacebookUtils.initialize(this);

    }
}
