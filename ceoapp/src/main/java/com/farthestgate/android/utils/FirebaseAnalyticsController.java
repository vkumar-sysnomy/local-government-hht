package com.farthestgate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activeandroid.app.Application;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FirebaseAnalyticsController {

    private Context mContext;
    public static FirebaseAnalyticsController firebaseAnalyticsController;
    private static FirebaseAnalytics firebaseAnalytics = null;

    private FirebaseAnalyticsController(final Context context) {
        mContext = context;
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setSessionTimeoutDuration(20000);
    }

    public static FirebaseAnalyticsController getInstance() {
        return firebaseAnalyticsController;
    }

    public static void initialize(final Application application) {
        if (firebaseAnalyticsController == null) {
            firebaseAnalyticsController = new FirebaseAnalyticsController(application.getApplicationContext());
        }
    }


    public synchronized void firebaseTrack(final String key,
                                           final Bundle bundle) {
        firebaseAnalytics.logEvent(key, bundle);
    }
}
