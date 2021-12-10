package com.onedevapp.smartcredentials.listeners;

public interface OnResultListener {


    /**
     * On any mobile number or email address selected from hint request
     * @param message selected value of credentials for hint or value of either OTP or error details for OTP
     * @param code type of an error for hint or status of OTP 0 - OTP received, 1 - Timeout, -1 - error for OTP
     */
    void onResult(String message, int code);
}
