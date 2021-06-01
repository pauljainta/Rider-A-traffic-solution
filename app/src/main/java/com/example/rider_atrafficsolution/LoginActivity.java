package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button LogIn,SignUp;
    EditText Email,Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Log In");
        getSupportActionBar().hide();
        LogIn=findViewById(R.id.loginLogInButton);
        SignUp=findViewById(R.id.loginSignUpButton);
        Email=findViewById(R.id.logInEmail);
        Password=findViewById(R.id.logInPassword);

        LogIn.setOnClickListener(this);
        SignUp.setOnClickListener(this);
        

    }

//    public void LoginUser()
//    {
//
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.loginLogInButton:
                //LoginUser();
              //  Log.d("b","Dhkse2");
                break;

            case R.id.loginSignUpButton:
                Log.d("b","Dhkse");
                Intent intent=new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }
}