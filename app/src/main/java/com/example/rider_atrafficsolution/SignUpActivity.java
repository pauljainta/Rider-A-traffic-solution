package com.example.rider_atrafficsolution;

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

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    Button LogIn,SignUp;
    EditText Email,Password,Name,Phone_No,Confirm_Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.setTitle("Sign Up");
        LogIn=findViewById(R.id.signupLogInButton);
        SignUp=findViewById(R.id.signupSignUpButton);
        Email=findViewById(R.id.signUpEmail);
        Password=findViewById(R.id.signUpPassword);
        Confirm_Password=findViewById(R.id.signUpConfirmPassword);
        Name=findViewById(R.id.signUpName);
        Phone_No=findViewById(R.id.signUpPhoneNo);

        Phone_No.setText("+88");
        Selection.setSelection(Phone_No.getText(), Phone_No.getText().length());


        Phone_No.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+88")){
                    Phone_No.setText("+88");
                    Selection.setSelection(Phone_No.getText(), Phone_No.getText().length());

                }

            }
        });



        LogIn.setOnClickListener(this);
        SignUp.setOnClickListener(this);
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


    }
}