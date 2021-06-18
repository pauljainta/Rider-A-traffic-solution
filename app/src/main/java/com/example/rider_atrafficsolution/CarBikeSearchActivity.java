package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class CarBikeSearchActivity extends AppCompatActivity
{
    double sourceLat;
    double sourceLong;
    double destLat;
    double destLong;

    double basicFare;
    double farePerKM;
    double estimatedFare;

    boolean sourceSelected;
    boolean destSelected;

    String source;
    String dest;
    String type;

    AutocompleteSupportFragment sourceFragment;
    AutocompleteSupportFragment destinationFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_bike_search);

        basicFare = 30;
        farePerKM = 30;
        estimatedFare = 0;



        sourceFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.source_fragment);

        destinationFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_fragment);

        sourceFragment.setCountry("BD");
        destinationFragment.setCountry("BD");


        type = getIntent().getStringExtra("type");


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCLCunrI2NjePZpCnEtLE0J6UQNfNN4Cg4", Locale.US);
        }

        sourceFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        destinationFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));



        sourceFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "source: " + place.getName() + ", " + place.getId());
                sourceSelected = true;

                sourceLat = place.getLatLng().latitude;
                sourceLong = place.getLatLng().longitude;
                source = place.getName();

                showMapsActivity();
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

                destSelected = true;

                destLat = place.getLatLng().latitude;
                destLong = place.getLatLng().longitude;
                dest = place.getName();

                showMapsActivity();
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void showMapsActivity()
    {
        if(sourceSelected && destSelected)
        {
            sourceSelected = false;
            destSelected = false;

            Util util = new Util();

            double dist = util.getDistanceFromLatLonInKm(sourceLat, sourceLong, destLat, destLong);

            estimatedFare = dist * farePerKM;

            estimatedFare += basicFare;

            Intent intent = new Intent(getApplicationContext(), RequestConfirmActivity.class);

            intent.putExtra("sourceLat", sourceLat);
            intent.putExtra("sourceLong", sourceLong);
            intent.putExtra("destLat", destLat);
            intent.putExtra("destLong", destLong);
            intent.putExtra("fare", estimatedFare);
            intent.putExtra("source", source);
            intent.putExtra("dest", dest);
            intent.putExtra("type", type);

            startActivity(intent);


        }
    }



}