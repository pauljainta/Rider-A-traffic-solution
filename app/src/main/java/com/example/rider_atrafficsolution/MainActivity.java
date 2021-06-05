package com.example.rider_atrafficsolution;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity  {

    Switch userTypeSwitch;
    Button getStartedButton;
    public static boolean isuserTypeSwitchChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Rider A Traffic Solution");
        getSupportActionBar().hide();
        userTypeSwitch=findViewById(R.id.userTypeSwitch);
        getStartedButton=findViewById(R.id.Get_Started_Button);
        Log.i("start","Start");
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isuserTypeSwitchChecked=userTypeSwitch.isChecked();
                //finish();
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);

            }
        });


        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/users.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                //try
                {
                    JSONArray array = response.names();
                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            Log.i("names", response.getJSONObject(array.getString(i)).getString("email"));
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("error: " , error.getMessage());
            }
        });

        requestQueue.add(jsonObjectRequest);






    }



}