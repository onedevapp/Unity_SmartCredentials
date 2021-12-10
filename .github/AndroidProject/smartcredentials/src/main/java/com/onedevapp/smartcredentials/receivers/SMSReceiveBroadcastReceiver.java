package com.onedevapp.smartcredentials.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.onedevapp.smartcredentials.listeners.OTPReceiveListener;

public class SMSReceiveBroadcastReceiver  extends BroadcastReceiver {

    private OTPReceiveListener otpListener;
    private String regexOTPPattern = "(\\d{4})";

    /**
     * @param otpListener Listener
     */
    public void setOTPListener(OTPReceiveListener otpListener) {
        this.otpListener = otpListener;
    }

    /**
     * @param otpRegex OTP Pattern
     */
    public void setOTPRegex(String otpRegex) {
        this.regexOTPPattern = otpRegex;
    }


    /**
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    //otpListener.onSuccess(messageIntent);
                    otpListener.onOTPReceived(message);

                    //This is the full message
                    /*String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);

                    Pattern pattern = Pattern.compile(regexOTPPattern);
                    Matcher matcher = pattern.matcher(message);
                    *//*<#> Your ExampleApp code is: 123ABC78 FA+9qCX9VSu*//*

                    //Extract the OTP code and send to the listener

                    if (otpListener != null) {
                        // Extract one-time code from the message and complete verification
                        String value = "";
                        if (matcher.find()) {
                            System.out.println(matcher.group(1));
                            value = matcher.group(1);
                        }
                        otpListener.onOTPReceived(value);
                    }*/
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    if (otpListener != null) {
                        otpListener.onOTPTimeOut();
                    }
                    break;

                case CommonStatusCodes.API_NOT_CONNECTED:

                    if (otpListener != null) {
                        otpListener.onOTPReceivedError("API NOT CONNECTED");
                    }

                    break;

                case CommonStatusCodes.NETWORK_ERROR:

                    if (otpListener != null) {
                        otpListener.onOTPReceivedError("NETWORK ERROR");
                    }

                    break;

                case CommonStatusCodes.ERROR:

                    if (otpListener != null) {
                        otpListener.onOTPReceivedError("SOME THING WENT WRONG");
                    }

                    break;

            }
        }
    }

}