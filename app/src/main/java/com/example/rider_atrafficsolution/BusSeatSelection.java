package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class BusSeatSelection extends AppCompatActivity
{
    Spinner busfromSpinner,bustoSpinner,whichbuSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_seat_selection);

        busfromSpinner=findViewById(R.id.busfFromSpinner);
        bustoSpinner=findViewById(R.id.busToSpinner);
        whichbuSpinner=findViewById(R.id.whichBusSpinner);




        List<String> locations = new ArrayList<String>();
        locations.add("Motijheel");
        locations.add("Gulshan");
        locations.add("Azimpur");
        locations.add("Gulistan");
        locations.add("Science Lab");
        locations.add("New Market");


        List<String> buses = new ArrayList<String>();
        buses.add("Bus no-1");
        buses.add("Bus no-2");
        buses.add("Bus no-3");
        buses.add("Bus no-4");
        buses.add("Bus no-5");
        buses.add("Bus no-6");

        // Creating adapter for spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);

        ArrayAdapter<String> busAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buses);


        // Drop down layout style - list view with radio button
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        busAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        // attaching data adapter to spinner
        busfromSpinner.setAdapter(locationAdapter);
        bustoSpinner.setAdapter(locationAdapter);
        whichbuSpinner.setAdapter(busAdapter);
    }
}