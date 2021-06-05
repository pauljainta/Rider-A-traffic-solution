package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class ChooseVehicleActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton busChooseButton,bikeChooseButton,carChooseButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_vehicle);
        busChooseButton=findViewById(R.id.busChooseButton);
        bikeChooseButton=findViewById(R.id.bikeChooseButton);
        carChooseButton=findViewById(R.id.carChooseButton);
        busChooseButton.setOnClickListener(this);
        carChooseButton.setOnClickListener(this);
        bikeChooseButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.busChooseButton:
                Intent intent=new Intent(getApplicationContext(),BusSeatSelection.class);
                startActivity(intent);
                break;

            case R.id.bikeChooseButton:
                break;

            case R.id.carChooseButton:
                break;
        }
    }
}