package com.onedevapp.smartcredentials.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.onedevapp.smartcredentials.AuthManager;
import com.onedevapp.smartcredentials.R;
import com.onedevapp.smartcredentials.listeners.OTPReceiveListener;
import com.onedevapp.smartcredentials.receivers.SMSReceiveBroadcastReceiver;
import com.onedevapp.smartcredentials.utilities.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthHeadlessActivity extends FragmentActivity implements OTPReceiveListener {

    CredentialsClient mCredentialsApiClient;
    CredentialRequest mCredentialRequest;
    SMSReceiveBroadcastReceiver smsReceiver;
    String regexOTPPattern;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headless_fragmentactivity);

        // Instantiate client for interacting with the credentials API. For this demo
        // application we forcibly enable the SmartLock save dialog, which is sometimes
        // disabled when it would conflict with the Android autofill API.
        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        mCredentialsApiClient = Credentials.getClient(this, options);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        setIntent(intent);
    }

    void handleIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        if (intent.getExtras().containsKey("regexOTP")) {
            regexOTPPattern = intent.getExtras().getString("regexOTP");
        } else {
            regexOTPPattern = Constants.regexOTPPattern;
        }
        if (intent.getExtras().containsKey("type")) {
            switch (intent.getExtras().getInt("type", 0)) {
                case 0:
                    requestHintMobileNo();
                    break;
                case 1:
                    requestHintEmailAddress(intent.getExtras().getStringArray("accountTypes"));
                    break;
                case 2:
                    startListening();
                    break;
                case 3:
                    requestStoredCredential(intent.getExtras().getStringArray("accountTypes"));
                    break;
                case 4:
                    saveToStoredCredential(
                            intent.getExtras().getString("email"),
                            intent.getExtras().getString("password"),
                            intent.getExtras().getString("accountType"),
                            intent.getExtras().getString("displayName"),
                            intent.getExtras().getString("profilePicUrl"));
                    break;
                case 5:
                    removeOTPListener();
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    void requestHintMobileNo() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();
        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), Constants.REQUEST_HINT_MOBILE_NO, null, 0, 0, 0, new Bundle());
        } catch (IntentSender.SendIntentException e) {
            Constants.WriteLog(e.getMessage());
            invokeHintResultNRemove("", Constants.AUTH_INTERNAL_ERROR);
        }
    }

    void requestHintEmailAddress(String[] accountTypes) {
        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setIdTokenRequested(false)
                .setEmailAddressIdentifierSupported(true)
                .setAccountTypes(accountTypes)
                .build();
        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), Constants.REQUEST_HINT_EMAIL_ID, null, 0, 0, 0, new Bundle());
        } catch (IntentSender.SendIntentException e) {
            Constants.WriteLog(e.getMessage());
            invokeHintResultNRemove("", Constants.AUTH_INTERNAL_ERROR);
        }
    }

    void startListening() {
        try {
            smsReceiver = new SMSReceiveBroadcastReceiver();
            smsReceiver.setOTPListener(this);

            SmsRetrieverClient client = SmsRetriever.getClient(this);
            client.startSmsUserConsent(null);
            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(aVoid -> {
                // API successfully started

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
                registerReceiver(smsReceiver, intentFilter);
                //registerReceiver(smsVerificationReceiver, intentFilter);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(smsVerificationReceiver, SmsRetriever.SEND_PERMISSION, intentFilter);
                }else{
                    registerReceiver(smsVerificationReceiver, intentFilter);
                }*/
            });
            task.addOnFailureListener(e -> {
                // Fail to start API
                unregisterReceiver(smsReceiver);
                //unregisterReceiver(smsVerificationReceiver);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveToStoredCredential(String emailOrMobile, String password, String accountType, String displayName, String profilePicUrl) {
        Credential credential = null;
        if (accountType.isEmpty()) {
            credential = new Credential.Builder(emailOrMobile)
                    .setPassword(password)  // Important: only store passwords in this field.
                    // Android autofill uses this value to complete
                    // sign-in forms, so repurposing this field will
                    // likely cause errors.
                    .build();
        } else if (accountType.equalsIgnoreCase(IdentityProviders.GOOGLE)) {
            credential = new Credential.Builder(emailOrMobile)
                    .setAccountType(IdentityProviders.GOOGLE)
                    .setName(displayName)
                    .setProfilePictureUri(profilePicUrl.isEmpty() ? null : Uri.parse(profilePicUrl))
                    .build();
        } else {
            //https://developers.google.com/identity/sms-retriever/request#optional_save_the_phone_number_with_smart_lock_for_passwords
            credential = new Credential.Builder(emailOrMobile)
                    .setAccountType(accountType)
                    .build();
        }

        mCredentialsApiClient.save(credential).addOnCompleteListener(
                new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Constants.WriteLog("Saved To Stored Credential: OK");
                            invokeResultNRemove(true);
                            return;
                        }

                        Exception e = task.getException();
                        if (e instanceof ResolvableApiException) {
                            // Try to resolve the save request. This will prompt the user if
                            // the credential is new.
                            ResolvableApiException rae = (ResolvableApiException) e;
                            try {
                                rae.startResolutionForResult(AuthHeadlessActivity.this, Constants.REQUEST_SAVE_CREDENTIALS);
                            } catch (IntentSender.SendIntentException exception) {
                                // Could not resolve the request
                                Constants.WriteLog("Failed to send resolution::"+ exception);
                                invokeResultNRemove(false);
                            }
                        } else {
                            // Request has no resolution
                            Constants.WriteLog("Save failed");
                            invokeResultNRemove(false);
                        }
                    }
                });
    }


    void requestStoredCredential(String[] accountTypes) {
        mCredentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(accountTypes)
                .build();

        mCredentialsApiClient.request(mCredentialRequest).addOnCompleteListener(
                new OnCompleteListener<CredentialRequestResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<CredentialRequestResponse> task) {

                        if (task.isSuccessful()) {
                            // See "Handle successful credential requests"
                            Credential credential = task.getResult().getCredential();
                            invokeCredentialsResultNRemove(credential, Constants.AUTH_HINTS_AVAILABLE);
                            return;
                        }

                        // See "Handle unsuccessful and incomplete credential requests"
                        // ...
                        Exception e = task.getException();
                        if (e instanceof ResolvableApiException) {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one. This requires showing UI to
                            // resolve the read request.
                            ResolvableApiException rae = (ResolvableApiException) e;
                            try {
                                rae.startResolutionForResult(AuthHeadlessActivity.this, Constants.REQUEST_READ_CREDENTIALS);
                            } catch (IntentSender.SendIntentException ex) {
                                Constants.WriteLog("Failed to send resolution::" + ex);
                                invokeCredentialsResultNRemove(null, Constants.AUTH_NO_HINTS_AVAILABLE);
                            }
                        } else if (e instanceof ApiException) {
                            // The user must create an account or sign in manually.
                            Constants.WriteLog("Unsuccessful credential request::" + e);

                            ApiException ae = (ApiException) e;
                            int code = ae.getStatusCode();
                            // ...
                            invokeCredentialsResultNRemove(null, Constants.AUTH_NO_HINTS_AVAILABLE);
                        }
                    }
                });
    }

    @Override
    public void onOTPReceived(String otpValue) {
        invokeOTPResultNRemove(parseOneTimeCode(otpValue), Constants.SMS_RECEIVER_OPT_RECEIVED);
    }

    @Override
    public void onSuccess(Intent intent) {
        startActivityForResult(intent, Constants.REQUEST_SMS_CONSENT);
    }

    @Override
    public void onOTPTimeOut() {
        invokeOTPResultNRemove("", Constants.SMS_RECEIVER_TIMEOUT);
    }

    @Override
    public void onOTPReceivedError(String error) {
        invokeOTPResultNRemove(error, Constants.SMS_RECEIVER_ERROR);
    }

    /**
     * Handle the request result when user switch back from Settings.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super, because overridden method will make the handler null, and we don't want that.
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AuthManager", "AuthManager onActivityResult called::requestCode::" + requestCode + "::resultCode::" + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_HINT_EMAIL_ID || requestCode == Constants.REQUEST_HINT_MOBILE_NO) {
                // Obtain the phone number from the result
                Credential credentials = data.getParcelableExtra(Credential.EXTRA_KEY);
                String value = credentials.getId(); //get the selected phone number
                invokeHintResultNRemove(value, Constants.AUTH_HINTS_AVAILABLE);
            } else if (requestCode == Constants.REQUEST_SMS_CONSENT) {
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                invokeOTPResultNRemove(parseOneTimeCode(message), Constants.SMS_RECEIVER_OPT_RECEIVED);
            } else if (requestCode == Constants.REQUEST_SAVE_CREDENTIALS) {
                invokeResultNRemove(true);
            } else if (requestCode == Constants.REQUEST_READ_CREDENTIALS) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                invokeCredentialsResultNRemove(credential, Constants.AUTH_HINTS_AVAILABLE);
            }
        } else if (resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            invokeHintResultNRemove("", Constants.AUTH_NO_HINTS_AVAILABLE);
        } else {
            if (requestCode == Constants.REQUEST_HINT_EMAIL_ID || requestCode == Constants.REQUEST_HINT_MOBILE_NO) {
                invokeHintResultNRemove("", Constants.AUTH_NO_HINTS_AVAILABLE);
            } else if (requestCode == Constants.REQUEST_SMS_CONSENT) {
                invokeOTPResultNRemove("", Constants.SMS_RECEIVER_ERROR);
            } else if (requestCode == Constants.REQUEST_SAVE_CREDENTIALS) {
                invokeResultNRemove(false);
            } else if (requestCode == Constants.REQUEST_READ_CREDENTIALS) {
                invokeCredentialsResultNRemove(null, Constants.AUTH_NO_HINTS_AVAILABLE);
            }
        }
    }

    String parseOneTimeCode(String message) {
        String value = "";
        if(message == null || message.isEmpty()) return value;

        try{
            //This is the full message
            Pattern pattern = Pattern.compile(regexOTPPattern);
            Matcher matcher = pattern.matcher(message);
            /*Your ExampleApp code is: 123ABC78 FA+9qCX9VSu*/

            //Extract the OTP code and send to the listener
            // Extract one-time code from the message and complete verification
            if (matcher.find()) {
                System.out.println(matcher.group(1));
                value = matcher.group(1);
            }
        }catch (Exception ignored){}

        return value;
    }

    void invokeResultNRemove(boolean status) {
        Constants.WriteLog("OnCredentialsStatus::" + status);
        AuthManager.getInstance().OnCredentialsStatus(status);
        finish();
    }

    void invokeHintResultNRemove(String value, int errorCode) {
        Constants.WriteLog("OnHintSelected::errorCode::" + errorCode + "::value::" + value);
        AuthManager.getInstance().OnHintSelected(value, errorCode);
        finish();
    }

    void invokeOTPResultNRemove(String value, int errorCode) {
        Constants.WriteLog("OnOTPRetrieved::errorCode::" + errorCode + "::value::" + value);
        AuthManager.getInstance().OnOTPRetrieved(value, errorCode);
        finish();
    }

    void invokeCredentialsResultNRemove(Credential credential, int errorCode) {
        if(credential != null)
            Constants.WriteLog("OnCredentialsRetrieved::errorCode::" + errorCode + "::userId::" + credential.getId() + "::password::" + credential.getPassword() + "::accountType::" + credential.getAccountType());
        else
            Constants.WriteLog("OnCredentialsRetrieved::errorCode::" + errorCode + "::userId:: ::password:: ::accountType::");
        AuthManager.getInstance().OnCredentialsRetrieved(credential, errorCode);
        finish();
    }

    void removeOTPListener() {
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            removeOTPListener();
        } catch (Exception e) {
            // already unregistered
        }
    }
}
