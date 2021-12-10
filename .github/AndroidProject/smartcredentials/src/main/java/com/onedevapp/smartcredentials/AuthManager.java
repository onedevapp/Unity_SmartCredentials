package com.onedevapp.smartcredentials;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.onedevapp.smartcredentials.listeners.OnCredentialsResultListener;
import com.onedevapp.smartcredentials.listeners.OnCredentialsStatusListener;
import com.onedevapp.smartcredentials.listeners.OnResultListener;
import com.onedevapp.smartcredentials.ui.AuthHeadlessActivity;
import com.onedevapp.smartcredentials.utilities.AppSignatureHelper;
import com.onedevapp.smartcredentials.utilities.Constants;
import com.onedevapp.smartcredentials.utilities.GooglePlayServicesHelper;

import java.lang.ref.WeakReference;

public class AuthManager {

    // region Declarations
    private static final AuthManager instance = new AuthManager();

    public static AuthManager getInstance() {
        return instance;
    }

    private AuthManager() {
    }

    private WeakReference<Activity> mActivityWeakReference; //Activity references

    //The result returned from this plugin
    private OnResultListener mOnResultListener; //Callback listener
    private OnCredentialsResultListener mOnCredentialsResultListener; //Callback listener
    private OnCredentialsStatusListener mOnCredentialsStatusListener; //Callback listener

    private String regexOTPPattern = "";
    //endregion

    //region Constructor
    //Private constructor with activity
    public AuthManager setActivity(Activity activity) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        return this;
    }

    /**
     * Set the OTP Pattern used to parse the exact OTP value from the SMS
     *
     * @param regexOTPPattern the handler
     * @return the auth manager instance
     */
    public AuthManager setOTPPattern(String regexOTPPattern) {
        this.regexOTPPattern = regexOTPPattern;
        return this;
    }
    //endregion
    // region helper functions

    /**
     * Returns the current activity
     */
    protected Activity getActivity() {
        return mActivityWeakReference.get();
    }
    //endregion

    public void GetAppSignatures() {
        if (Constants.enableLog)
            new AppSignatureHelper(getActivity()).getAppSignatures();
    }

    public void RequestMobileNoHint(OnResultListener resultListener) {
        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            this.mOnResultListener = resultListener;

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 0);
            getActivity().startActivity(intent);
        });
    }

    public void RequestEmailAddressHint(String[] accountTypes, OnResultListener resultListener) {
        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            this.mOnResultListener = resultListener;

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("accountTypes", accountTypes);
            getActivity().startActivity(intent);
        });
    }

    public void StopListening() {
        getActivity().runOnUiThread(() -> {

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 5);
            getActivity().startActivity(intent);
        });
    }

    public void StartListening(OnResultListener resultListener) {
        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            this.mOnResultListener = resultListener;

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 2);
            if (!regexOTPPattern.isEmpty())
                intent.putExtra("regexOTP", regexOTPPattern);
            getActivity().startActivity(intent);
        });
    }

    public void GetStoredCredentials(String[] accountTypes, OnCredentialsResultListener onRetrieveListener) {
        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            mOnCredentialsResultListener = onRetrieveListener;

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 3);
            intent.putExtra("accountTypes", accountTypes);
            getActivity().startActivity(intent);
        });
    }

    public void SaveToStoredCredentials(String emailOrMobile, String password, String accountType, String displayName, String profilePicUrl, OnCredentialsStatusListener mOnStatusListener) {

        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            if (emailOrMobile.isEmpty()) {
                Constants.WriteLog("Email Id cant be empty");
                mOnStatusListener.onCredentialsStatus(false);
                return;
            }

            this.mOnCredentialsStatusListener = mOnStatusListener;

            Intent intent = new Intent(getActivity(), AuthHeadlessActivity.class);
            intent.putExtra("type", 4);
            intent.putExtra("email", emailOrMobile);
            intent.putExtra("password", password);
            intent.putExtra("accountType", accountType);
            intent.putExtra("displayName", displayName);
            intent.putExtra("profilePicUrl", profilePicUrl);
            getActivity().startActivity(intent);
        });
    }

    public void DeleteStoredCredentials(Credential credential, OnCredentialsStatusListener mOnStatusListener) {
        getActivity().runOnUiThread(() -> {
            if (IsPlayServiceAvailable()) return;

            CredentialsOptions options = new CredentialsOptions.Builder()
                    .forceEnableSaveDialog()
                    .build();

            CredentialsClient mCredentialsApiClient = Credentials.getClient(getActivity(), options);
            mCredentialsApiClient.delete(credential).addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Credential deletion succeeded.
                                // ...
                                mOnStatusListener.onCredentialsStatus(true);
                                return;
                            }
                            mOnStatusListener.onCredentialsStatus(false);
                        }
                    });
        });
    }


    public boolean IsPlayServiceAvailable() {

        if (!GooglePlayServicesHelper.isAvailable(getActivity())) {
            OnHintSelected(Constants.UNAVAILABLE_ERROR_MESSAGE, Constants.AUTH_UNAVAILABLE_PLAY_SERVICE_ERROR);
            return true;
        }

        if (!GooglePlayServicesHelper.hasSupportedVersion(getActivity())) {
            OnHintSelected(Constants.UNSUPPORTED_VERSION_ERROR_MESSAGE, Constants.AUTH_PLAY_SERVICE_UNSUPPORTED_VERSION_ERROR);
            return true;
        }

        return false;
    }

    public void OnCredentialsRetrieved(Credential credential, int errorCode) {
        if (mOnCredentialsResultListener != null) {
            mOnCredentialsResultListener.onCredentialsResult(credential, errorCode);
            mOnCredentialsResultListener = null;
        }
    }

    public void OnCredentialsStatus(boolean status) {
        if (mOnCredentialsStatusListener != null) {
            mOnCredentialsStatusListener.onCredentialsStatus(status);
            mOnCredentialsStatusListener = null;
        }
    }

    public void OnOTPRetrieved(String value, int errorCode) {
        if (mOnResultListener != null) {
            mOnResultListener.onResult(value, errorCode);
            mOnResultListener = null;
        }
    }

    public void OnHintSelected(String value, int errorCode) {
        if (mOnResultListener != null) {
            mOnResultListener.onResult(value, errorCode);
            mOnResultListener = null;
        }
    }
}
