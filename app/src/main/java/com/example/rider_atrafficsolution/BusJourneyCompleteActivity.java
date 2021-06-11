package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BusJourneyCompleteActivity extends AppCompatActivity
{
    TextView busFareView;
    Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_journey_complete);

        busFareView = findViewById(R.id.busFareTextView);
        continueButton = findViewById(R.id.continueButton);

        double fare = getIntent().getDoubleExtra("fare", 1);

        busFareView.setText("Your Fare was \n" + fare + "Taka");
    }

    public void gotoHome(View view)
    {
        Intent intent = new Intent(getApplicationContext(), ChooseVehicleActivity.class);
        startActivity(intent);
    }
}