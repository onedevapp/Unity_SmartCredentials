package com.onedevapp.smartcredentials.listeners;

public interface OnCredentialsStatusListener {
    /**
     * credentials status
     * @param isSuccess value of credentials user id
     */
    void onCredentialsStatus(boolean isSuccess);
}
