package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class PhoneAuthActivity extends AppCompatActivity {

    EditText phoneNumberOTP;
    Button resendOTPButton,verifyOTPButton;
    String phone_number;

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


    }
}