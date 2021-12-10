package com.onedevapp.smartcredentials.listeners;


import com.google.android.gms.auth.api.credentials.Credential;

public interface OnCredentialsResultListener {

    /**
     * Retrieve stored credentials
     * @param credential value of credentials
     * @param statusCode status of credential 0 - Available, 3 - Not Available
     */
    void onCredentialsResult(Credential credential, int statusCode);
}
