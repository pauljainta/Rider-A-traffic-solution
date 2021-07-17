package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class PromoCodeActivity extends AppCompatActivity {
    Button applypromocodeButton;
    EditText applypromocodeEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_code);


        applypromocodeButton=findViewById(R.id.promocodeButton);
        applypromocodeEditText=findViewById(R.id.promocodeEditText);

    }
}