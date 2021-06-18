package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ChooseVehicleActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton busChooseButton,bikeChooseButton,carChooseButton;
    NavigationView naview;
    TextView username,rating;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_vehicle_choose);
        busChooseButton=findViewById(R.id.busChooseButton);
        bikeChooseButton=findViewById(R.id.bikeChooseButton);
        carChooseButton=findViewById(R.id.carChooseButton);
        naview = findViewById(R.id.nav_view);
        username=findViewById(R.id.nav_header_title);
        rating=findViewById(R.id.nav_header_subtitle);
        busChooseButton.setOnClickListener(this);
        carChooseButton.setOnClickListener(this);
        bikeChooseButton.setOnClickListener(this);



        naview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.nav_history:
                        break;

                    case R.id.nav_help:
                        break;

                    case R.id.nav_logout:
                        break;

                }
                return true;
            }
        });

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