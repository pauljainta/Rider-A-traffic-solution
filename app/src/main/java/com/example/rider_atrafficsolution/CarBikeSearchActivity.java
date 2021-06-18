package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class CarBikeSearchActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_bike_search);

        AutocompleteSupportFragment sourceFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.source_fragment);

        AutocompleteSupportFragment destinationFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_fragment);

        sourceFragment.setCountry("BD");
        destinationFragment.setCountry("BD");





        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCLCunrI2NjePZpCnEtLE0J6UQNfNN4Cg4", Locale.US);
        }

        sourceFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        destinationFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        sourceFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "source: " + place.getName() + ", " + place.getId());
            }



            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        destinationFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "dest: " + place.getName() + ", " + place.getId());
            }



            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
}