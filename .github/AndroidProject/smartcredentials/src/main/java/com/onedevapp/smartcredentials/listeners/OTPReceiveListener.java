package com.onedevapp.smartcredentials.listeners;

import android.content.Intent;

public interface OTPReceiveListener
{

    void onSuccess(Intent intent);

    void onOTPReceived(String otp);

    void onOTPTimeOut();

    void onOTPReceivedError(String error);
}
