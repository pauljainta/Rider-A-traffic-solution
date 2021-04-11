package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    Button LogIn,SignUp;
    EditText Email,Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Log In");
        LogIn=findViewById(R.id.logInButton);
        SignUp=findViewById(R.id.signUp);
        Email=findViewById(R.id.logInEmail);
        Password=findViewById(R.id.logInPassword);

    }
}