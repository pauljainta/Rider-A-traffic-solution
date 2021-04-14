package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {

    EditText phoneNumberOTP;
    Button resendOTPButton,verifyOTPButton;
    TextView phoneVerifiedTextview;
    String phone_number,email;
    private FirebaseAuth mAuth;
    String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        this.setTitle("Phone Number OTP Verifier");
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        resendOTPButton=findViewById(R.id.resendOTPButton);
        verifyOTPButton=findViewById(R.id.otpVerifyButton);
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        phoneVerifiedTextview =findViewById(R.id.phoneVerifiedTextview);
        Intent intent=getIntent();
        phone_number=intent.getStringExtra("PhoneNumber");
        email=intent.getStringExtra("email");
        phoneNumberOTP.setHint("Enter OTP sent to "+phone_number);
        mAuth=FirebaseAuth.getInstance();

        verifyOTPButton.setOnClickListener(this);
        resendOTPButton.setOnClickListener(this);
        resendOTPButton.setEnabled(false);

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
                                Toast.makeText(getApplicationContext(),"Verification Failed",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationid, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                               super.onCodeSent(verificationid, forceResendingToken);
                                mVerificationID =verificationid;
                                mResendToken=forceResendingToken;
                                resendOTPButton.setVisibility(View.VISIBLE);

                                countDownTimer=new CountDownTimer(60000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendOTPButton.setText("RESEND OTP In "+millisUntilFinished/1000 +" seconds");
                                    }

                                    @Override
                                    public void onFinish() {

                                        resendOTPButton.setEnabled(true);
                                        resendOTPButton.setText("RESEND OTP NOW ");
                                    }
                                }.start();

                            }

                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String verificationID) {
                                super.onCodeAutoRetrievalTimeOut(verificationID);


                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Verified","Verified");
                        phoneNumberOTP.setVisibility(View.GONE);
                        resendOTPButton.setVisibility(View.GONE);
                        verifyOTPButton.setVisibility(View.GONE);
                        phoneVerifiedTextview.setText("Phone Number Verification Successful.CLick on the link sent to "+email+" to verify.");
                        phoneVerifiedTextview.setVisibility(View.VISIBLE);


                    } else {

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            Toast.makeText(getApplicationContext(),"Verification Failed+"+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.otpVerifyButton:
                Log.d("Bal","Bal");
                PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationID,phoneNumberOTP.getText().toString().trim());
                signInWithPhoneAuthCredential(phoneAuthCredential);
                break;
            case R.id.resendOTPButton:
                resendVerificationCode(phone_number,mResendToken);
                break;
        }
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(getApplicationContext(),"Verification Failed",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);
                                mVerificationID =verificationId;
                                mResendToken=forceResendingToken;
                                countDownTimer=new CountDownTimer(60000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendOTPButton.setText("RESEND OTP In "+millisUntilFinished/1000 +" seconds");
                                    }

                                    @Override
                                    public void onFinish() {

                                        resendOTPButton.setEnabled(true);
                                        resendOTPButton.setText("RESEND OTP NOW ");
                                    }
                                }.start();

                            }
                        })          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}