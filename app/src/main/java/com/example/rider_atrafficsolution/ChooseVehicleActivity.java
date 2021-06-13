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
        Intent intent = null;

        switch (v.getId())
        {
            case R.id.busChooseButton:
                intent=new Intent(getApplicationContext(),BusSeatSelection.class);
                startActivity(intent);
                break;

            case R.id.bikeChooseButton:
                intent=new Intent(getApplicationContext(),CarBikeSearchActivity.class);
                startActivity(intent);
                break;

            case R.id.carChooseButton:
                intent=new Intent(getApplicationContext(),RequestConfirmActivity.class);
                startActivity(intent);
                break;
        }
    }
}