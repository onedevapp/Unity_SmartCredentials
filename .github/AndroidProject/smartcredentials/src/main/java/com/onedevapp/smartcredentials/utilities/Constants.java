package com.onedevapp.smartcredentials.utilities;

import android.util.Log;

public class Constants {

    public static final int AUTH_HINTS_AVAILABLE = 0;
    public static final int AUTH_UNAVAILABLE_PLAY_SERVICE_ERROR = 1;
    public static final int AUTH_PLAY_SERVICE_UNSUPPORTED_VERSION_ERROR = 2;
    public static final int AUTH_NO_HINTS_AVAILABLE = 3;
    public static final int AUTH_INTERNAL_ERROR = 4;

    public static final int SMS_RECEIVER_OPT_RECEIVED = 0;
    public static final int SMS_RECEIVER_ERROR = 1;
    public static final int SMS_RECEIVER_TIMEOUT = 2;
    public static final int SMS_RECEIVER_API_NOT_CONNECTED = 3;
    public static final int SMS_RECEIVER_NETWORK_ERROR = 4;

    public static final int REQUEST_HINT_MOBILE_NO = 9861;
    public static final int REQUEST_HINT_EMAIL_ID = 9862;
    public static final int REQUEST_SMS_CONSENT = 9863;
    public static final int REQUEST_READ_CREDENTIALS = 9864;
    public static final int REQUEST_SAVE_CREDENTIALS = 9865;

    public static final String regexOTPPattern = "(\\d{4})";

    public static final String UNAVAILABLE_ERROR_MESSAGE = "Google Play Services is not available.";
    public static final String UNSUPPORTED_VERSION_ERROR_MESSAGE = "The device version of Google Play Services is not supported.";
    /**
     * To write library messages to logcat
     */
    public static boolean enableLog = true;

    /**
     * WriteLog to log library messages to logcat
     * Can toggle on/off with enableLog boolean at any time
     *
     * @param message Log Message
     */
    public static void WriteLog(String message) {
        if (enableLog) Log.d("AuthManager", message);
    }

    public static String GetValueWithoutNull(String value){
        return (value == null ? "" : value);
    }
}
