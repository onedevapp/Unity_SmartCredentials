package com.onedevapp.smartcredentials.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.onedevapp.smartcredentials.utilities.Constants;
import com.unity3d.player.UnityPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiveBroadcastReceiver  extends BroadcastReceiver {

    private String regexOTPPattern = ":\\s([0-9]{4})";

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
            Constants.WriteLog("status.getStatusCode()::"+status.getStatusCode());
            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    //This is the full message
                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    if(message == null || message.isEmpty()){
                        UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveError", String.valueOf(Constants.SMS_RECEIVER_ERROR));
                    }else{
                        Constants.WriteLog(message);
                        //String input = "Your OTP code is : 1234\r\n" + "\r\n" + "FA+9qCX9VSu";
                        /*String value = "";
                        Pattern regex = Pattern.compile(regexOTPPattern);
                        Matcher m = regex.matcher(message);
                        try{
                            if (m.find()) {
                                System.out.println(m.group(1));
                                value = m.group(1);
                            }
                        }catch(Exception e){
                            Constants.WriteLog(e.toString());
                        }*/

                        UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveSuccess", message);
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveError", String.valueOf(Constants.SMS_RECEIVER_TIMEOUT));
                    break;

                case CommonStatusCodes.API_NOT_CONNECTED:

                    UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveError", String.valueOf(Constants.SMS_RECEIVER_API_NOT_CONNECTED));
                    break;

                case CommonStatusCodes.NETWORK_ERROR:

                    UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveError", String.valueOf(Constants.SMS_RECEIVER_NETWORK_ERROR));

                    break;

                case CommonStatusCodes.ERROR:

                    UnityPlayer.UnitySendMessage("SmartCredentialManager", "OnOtpReceiveError", String.valueOf(Constants.SMS_RECEIVER_ERROR));

                    break;

            }
        }
    }

}