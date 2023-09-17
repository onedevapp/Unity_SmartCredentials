package com.onedevapp.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.onedevapp.smartcredentials.AuthManager;
import com.onedevapp.smartcredentials.utilities.Constants;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    EditText inputMobileNumber, inputOtp, inputEmail, inputPassword;
    Button btnGetOtp, btnVerifyOtp, buttonSave, buttonDelete, buttonLoad, buttonHint;
    ConstraintLayout layoutInput, layoutVerify;
    AuthManager authManager;
    Credential savedCredential;

    String myAppIdentityProvider = "https://signin.example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        authManager = AuthManager.getInstance();
        authManager.setActivity(this);
        authManager.GetAppSignatures();
        Constants.enableLog = true;
        authManager.RequestMobileNoHint((message, statusCode) -> {
            if(statusCode == 0){
                inputMobileNumber.setText(message);
            }
        });
    }

    private void initViews() {
        inputMobileNumber = findViewById(R.id.editTextInputMobile);
        inputEmail = findViewById(R.id.editTextEmail);
        inputPassword = findViewById(R.id.editTextPassword);
        inputOtp = findViewById(R.id.editTextOTP);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonLoad = findViewById(R.id.buttonLoad);
        buttonHint = findViewById(R.id.buttonHint);
        btnGetOtp = findViewById(R.id.buttonGetOTP);
        btnVerifyOtp = findViewById(R.id.buttonVerify);

        layoutInput = findViewById(R.id.getOTPLayout);
        layoutVerify = findViewById(R.id.verifyOTPLayout);

        btnGetOtp.setOnClickListener(v -> {
            // Call server API for requesting OTP and when you got success start
            // SMS Listener for listing auto read message listener
            /*authManager.StartListening((message, statusCode) -> {
                if(statusCode == 0){
                    inputOtp.setText(message);
                }
            });*/
            layoutInput.setVisibility(View.GONE);
            layoutVerify.setVisibility(View.VISIBLE);
        });

        buttonSave.setOnClickListener(v -> {
            authManager.SaveToStoredCredentials(inputEmail.getText().toString(), inputPassword.getText().toString(), "", "", "", isSuccess -> {
                Toast.makeText(MainActivity.this, "Save credential - " + isSuccess, Toast.LENGTH_SHORT).show();
            });
        });

        btnVerifyOtp.setOnClickListener(v -> {
            authManager.SaveToStoredCredentials(inputMobileNumber.getText().toString(), "", myAppIdentityProvider, "", "", isSuccess -> {
                Toast.makeText(MainActivity.this, "Save credential - " + isSuccess, Toast.LENGTH_SHORT).show();
            });
        });

        buttonDelete.setOnClickListener(v -> {
            if(savedCredential == null) return;

            authManager.DeleteStoredCredentials(savedCredential, isSuccess -> {
                Toast.makeText(MainActivity.this, "Deleted credential - " + isSuccess, Toast.LENGTH_SHORT).show();
            });
        });

        buttonLoad.setOnClickListener(v -> {
            authManager.GetStoredCredentials(new String[]{""}, (credential, statusCode) -> {
                if(statusCode == 0){
                    savedCredential = credential;
                    inputEmail.setText(credential.getId());
                    inputPassword.setText(credential.getPassword());
                    buttonDelete.setEnabled(true);
                }else{
                    savedCredential = null;
                    buttonDelete.setEnabled(false);
                }
            });
        });//IdentityProviders.GOOGLE

        buttonHint.setOnClickListener(v -> {
            authManager.RequestEmailAddressHint(new String[]{IdentityProviders.GOOGLE}, (message, statusCode) -> {
                if(statusCode == 0){
                    inputEmail.setText(message);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authManager.StopListening();
    }
}