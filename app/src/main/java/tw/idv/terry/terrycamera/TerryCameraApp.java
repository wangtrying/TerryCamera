package tw.idv.terry.terrycamera;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by wangtrying on 2015/5/28.
 */
public class TerryCameraApp extends Application {
    public static final String DEFAULT_PREV_IMAGE_NAME = "DEFAULT";
    private static GoogleAnalytics analytics;
    private static Tracker tracker;
    private static Context mContext;

    public static final boolean IS_DEBUG = true;

    public static final String KEY_IMAGE_TAKEN_PREVIOUSLY = "prev_taken_image_name";

    public static GoogleAnalytics getAnalytics() {
        return analytics;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Context getContext(){
        return mContext;
    }




    @Override
    public void onCreate() {
        if (IS_DEBUG == false) {
            analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(1800);

            tracker = analytics.newTracker("UA-58953649-2"); // Replace with actual tracker/property Id
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
        }
        mContext = getApplicationContext();



    }
}
