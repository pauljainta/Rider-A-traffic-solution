package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {


    EditText phoneNumberOTP;
    Button resendOTPButton,verifyOTPButton;
 //   TextView phoneVerifiedTextview;
    String name,phone_number,email,password;
    private FirebaseAuth mAuth;
    String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private AlertDialog.Builder builder;


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
      //  phoneVerifiedTextview =findViewById(R.id.phoneVerifiedTextview);
        Intent intent=getIntent();
        phone_number=intent.getStringExtra("PhoneNumber");
        email=intent.getStringExtra("email");
        name=intent.getStringExtra("name");
        password =intent.getStringExtra("password");
        phoneNumberOTP.setHint("Enter OTP sent to "+phone_number);
        mAuth=FirebaseAuth.getInstance();
       // databaseReference= FirebaseDatabase.getInstance().getReference("users");

        verifyOTPButton.setOnClickListener(this);
        resendOTPButton.setOnClickListener(this);
        resendOTPButton.setEnabled(false);

        //alert

        builder = new AlertDialog.Builder(this);

        builder.setMessage("Click Ok to Continue")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                SaveUserData();
                            }
                        });
                        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                    }
                });

        initiateOTP();

    }

    private void SaveUserData()
    {
        Passenger passenger=new Passenger(name,email,phone_number);

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

                        AlertDialog alert = builder.create();
                        //Setting the title manually
                        alert.setTitle("PHONE NUMBER VERIFIED");
                        alert.show();



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
              //  Log.d("Bal","Bal");
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