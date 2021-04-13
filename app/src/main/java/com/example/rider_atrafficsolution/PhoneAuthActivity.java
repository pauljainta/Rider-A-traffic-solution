package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {

    EditText phoneNumberOTP;
    Button resendOTPButton,verifyOTPButton;
    String phone_number;
    private FirebaseAuth mAuth;
    String verificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        this.setTitle("Phone Number OTP Verifier");
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        resendOTPButton=findViewById(R.id.resendOTPButton);
        verifyOTPButton=findViewById(R.id.otpVerifyButton);
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        Intent intent=getIntent();
        phone_number=intent.getStringExtra("PhoneNumber");
        phoneNumberOTP.setHint("Enter OTP sent to "+phone_number);
        mAuth=FirebaseAuth.getInstance();

        initiateOTP();



    }

    private void initiateOTP() {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone_number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(PhoneAuthActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationid, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                               super.onCodeSent(verificationID, forceResendingToken);
                                verificationID=verificationid;

                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));


                    } else {

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.otpVerifyButton:
                PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(verificationID,phoneNumberOTP.getText().toString().trim());
                signInWithPhoneAuthCredential(phoneAuthCredential);
                break;
        }
    }
}