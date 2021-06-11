package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class DriverVehicleTypeSelection extends AppCompatActivity implements View.OnClickListener {

    Spinner driverVehicleTypeSpinner,driverVehicleNumberSpinner;
    Button driverNextButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_vehicle_type_selection);
        driverVehicleNumberSpinner=findViewById(R.id.driverVehicleNumberSpinner);
        driverVehicleTypeSpinner=findViewById(R.id.driverVehicleTypeSpinner);
        driverNextButton=findViewById(R.id.driverNextButton);

        driverNextButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.driverNextButton:
                Intent intent=new Intent(getApplicationContext(),DriverLocationUpdate.class);
                startActivity(intent);
        }
    }
}