package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {


    Button LogInButton, SignUpButton;
    EditText signupEmailEditText, signupPasswordEditText, signupNameEditText, signupPhoneNumberEditText, signupConfirmPasswordEditText;
    String email,name,password,confirm_password,phone_number;


    private FirebaseAuth mAuth;

    boolean isInputCorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.setTitle("Sign Up");
        getSupportActionBar().hide();
        LogInButton =findViewById(R.id.signupLogInButton);
        SignUpButton =findViewById(R.id.signupSignUpButton);
        signupEmailEditText =findViewById(R.id.signUpEmail);
        signupPasswordEditText =findViewById(R.id.signUpPassword);
        signupConfirmPasswordEditText =findViewById(R.id.signUpConfirmPassword);
        signupNameEditText =findViewById(R.id.signUpName);
        signupPhoneNumberEditText =findViewById(R.id.signUpPhoneNo);

        signupPhoneNumberEditText.setText("+88");
        Selection.setSelection(signupPhoneNumberEditText.getText(), signupPhoneNumberEditText.getText().length());
        mAuth = FirebaseAuth.getInstance();

        isInputCorrect=true;


        signupPhoneNumberEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+88")){
                    signupPhoneNumberEditText.setText("+88");
                    Selection.setSelection(signupPhoneNumberEditText.getText(), signupPhoneNumberEditText.getText().length());

                }

            }
        });


        LogInButton.setOnClickListener(this);
        SignUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.signupLogInButton:
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.signupSignUpButton:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        email= signupEmailEditText.getText().toString().trim();
        name= signupNameEditText.getText().toString().trim();
        password= signupPasswordEditText.getText().toString().trim();
        phone_number=signupPhoneNumberEditText.getText().toString().trim();
        confirm_password=signupConfirmPasswordEditText.getText().toString().trim();



        if(email.isEmpty())
        {
            signupEmailEditText.setError("Enter an email address");
            signupEmailEditText.requestFocus();
            isInputCorrect=false;
        }
        if(password.isEmpty())
        {
            signupPasswordEditText.setError("Enter Password");
            signupPasswordEditText.requestFocus();
            isInputCorrect=false;
        }

        if(confirm_password.isEmpty())
        {
            signupConfirmPasswordEditText.setError("Enter Password");
            signupConfirmPasswordEditText.requestFocus();
            isInputCorrect=false;


        }

        if(name.isEmpty())
        {
            signupNameEditText.setError("Enter your name");
            signupNameEditText.requestFocus();
            isInputCorrect=false;
        }
        if(password.length()<6)
        {
            signupPasswordEditText.setError("Password must be at least 6 characters long");
            signupPasswordEditText.requestFocus();
            isInputCorrect=false;
        }
        if(phone_number.equalsIgnoreCase("+88")||phone_number.length()!=14)
        {
            signupPhoneNumberEditText.setError("Enter your 11 digit phone number");
            signupPhoneNumberEditText.requestFocus();
            isInputCorrect=false;
        }

        if(phone_number.charAt(5)=='1'||phone_number.charAt(5)=='2'||
                phone_number.charAt(5)=='0'||phone_number.charAt(5)=='4')
        {
            signupPhoneNumberEditText.setError("Enter a valid phone number");
            signupPhoneNumberEditText.requestFocus();
            isInputCorrect=false;

        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            signupEmailEditText.setError("Enter a valid email address");
            signupEmailEditText.requestFocus();
            isInputCorrect=false;
        }


        if(isInputCorrect)
            verifyPhoneNumber();




    }

    private void verifyPhoneNumber()
    {
        Intent intent=new Intent(getApplicationContext(),PhoneAuthActivity.class);
        intent.putExtra("PhoneNumber",phone_number);
        startActivity(intent);



    }



}
