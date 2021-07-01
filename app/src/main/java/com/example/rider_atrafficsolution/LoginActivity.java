package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button LogIn,SignUp;
    EditText Email,Password;
    private FirebaseAuth mAuth;

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

        mAuth=FirebaseAuth.getInstance();



        

    }

    public void LoginUser()
    {
        Log.i("b","Dhkse2");

        String email=Email.getText().toString();
        String password=Password.getText().toString();

        if(MainActivity.isuserTypeSwitchChecked)
        {
//            Intent intent=new Intent(getApplicationContext(),DriverLocationUpdate.class);
            Intent intent=new Intent(getApplicationContext(),DriverInitialSetLocationActivity.class);
            //intent.putExtra("id",email);
            Info.driverID = email;

            startActivity(intent);
        }

        else
        {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user.isEmailVerified()) {
                            //  finish();
                            // Intent intent=new Intent(getApplicationContext(),Random.class);
                            //   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //  startActivity(intent);
                            Log.i("log in", "hoise");
                            Intent intent = new Intent(getApplicationContext(), ChooseVehicleActivity.class);
                            startActivity(intent);

                            Info.currentEmail = email;

                            Log.i("email", Info.currentEmail);
                        } else {
                            Log.i("log in", "hoinai");
                            Email.setError("Emaiil not verified");
                            // user.sendEmailVerification();
                            // Toast.makeText(getApplicationContext(), "Check Email for verification", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Toast.makeText(getApplicationContext(), "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.loginLogInButton:
                LoginUser();
                break;

            case R.id.loginSignUpButton:
                Log.i("b","Dhkse");
                Intent intent=new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }
}