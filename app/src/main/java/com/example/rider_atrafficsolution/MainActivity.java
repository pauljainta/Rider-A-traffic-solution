package com.example.rider_atrafficsolution;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  {

    Switch userTypeSwitch;
    Button getStartedButton;
    public static boolean isuserTypeSwitchChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Rider A Traffic Solution");
        getSupportActionBar().hide();
        userTypeSwitch=findViewById(R.id.userTypeSwitch);
        getStartedButton=findViewById(R.id.Get_Started_Button);
        Log.i("start","Start");
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isuserTypeSwitchChecked=userTypeSwitch.isChecked();
                //finish();
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);

            }
        });


        Info.currentEmail = "aaatowsif16@gmail.com";

        Intent intent=new Intent(getApplicationContext(),ShowBusLoationActivity.class);
        startActivity(intent);





    }



}