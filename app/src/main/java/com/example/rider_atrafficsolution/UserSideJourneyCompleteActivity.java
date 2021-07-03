package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class UserSideJourneyCompleteActivity extends AppCompatActivity
{
    TextView fareShowTextView;
    RatingBar userRatingBar;
    TextView userRatingTextView;
    Button userSideContinueButton;

    double user_rating_driver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_side_journey_complete);

        fareShowTextView = findViewById(R.id.userFareShowTextView);
        userRatingBar = findViewById(R.id.userRatingBar);
        userRatingTextView = findViewById(R.id.userRatingTextView);
        userSideContinueButton = findViewById(R.id.userSideContinueButtonToHome);

        user_rating_driver = 5;

        double fare = getIntent().getDoubleExtra("fare", 1);

        fareShowTextView.setText("Your Have Paid TK " + fare);

        userRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                user_rating_driver = rating;
                userRatingTextView.setText("Rate Your Driver : " + rating);
            }
        });

        userSideContinueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

    }





}