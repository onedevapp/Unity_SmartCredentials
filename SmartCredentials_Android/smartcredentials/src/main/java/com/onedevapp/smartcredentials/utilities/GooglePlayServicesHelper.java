package com.onedevapp.smartcredentials.utilities;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayServicesHelper {

    private static final int MINIMAL_VERSION = 10200000;

    private GooglePlayServicesHelper() { }

    public static boolean isAvailable(@NonNull final Context context) {
        final int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        return (result != ConnectionResult.SERVICE_MISSING
                && result != ConnectionResult.SERVICE_DISABLED
                && result != ConnectionResult.SERVICE_INVALID);
    }

    public static boolean hasSupportedVersion(@NonNull final Context context) {
        final PackageManager manager = context.getPackageManager();

        try {
            int version = manager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode;
            return (version >= MINIMAL_VERSION);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
