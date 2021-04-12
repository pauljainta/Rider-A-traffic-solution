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

    Pattern phone_number_pattern;//useless
    Button LogInButton, SignUpButton;
    EditText signupEmailEditText, signupPasswordEditText, signupNameEditText, signupPhoneNumberEditText, signupConfirmPasswordEditText;
    String email,name,password,confirm_password,phone_number;

    String mVerificationId;

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
        phone_number_pattern=Pattern.compile("(\\+8801)[356789]\\d{6}");

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
       Matcher phone_number_matcher=phone_number_pattern.matcher(phone_number);


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

        if(phone_number.charAt(5)!='3'||phone_number.charAt(5)!='5'||
                phone_number.charAt(5)!='6'||phone_number.charAt(5)!='7'||phone_number.charAt(5)!='8'||phone_number.charAt(5)!='9')
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

    private void verifyPhoneNumber() {

        Intent intent=new Intent(getApplicationContext(),PhoneAuthActivity.class);
        intent.putExtra("PhoneNumber",phone_number);
        startActivity(intent);

//        com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
//                mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            @Override
//            public void onVerificationCompleted(PhoneAuthCredential credential) {
//
//                signInWithPhoneAuthCredential(credential);
//            }


//            @Override
//            public void onVerificationFailed(FirebaseException e) {
//
//
//                if (e instanceof FirebaseAuthInvalidCredentialsException) {
//
//                } else if (e instanceof FirebaseTooManyRequestsException) {
//
//                }
//
//                // Show a message and update the UI
//            }
//
//            @Override
//            public void onCodeSent(@NonNull String verificationId,
//                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
//                mVerificationId = verificationId;
//              //  mResendToken = token;
//            }
//        };

//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phone_number)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // Activity (for callback binding)
//                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
//                        .build();

            // PhoneAuthProvider.verifyPhoneNumber(options);

//        }


//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                        //    Log.d(TAG, "signInWithCredential:success");
//
//                            FirebaseUser user = task.getResult().getUser();
//                            // Update UI
//                        } else {
//                            // Sign in failed, display a message and update the UI
//                     //       Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                // The verification code entered was invalid
//                            }
//                        }
//                    }
//                });
//
    }
}
