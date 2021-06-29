package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ChooseVehicleActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton busChooseButton,bikeChooseButton,carChooseButton;
    NavigationView naview;
    TextView username,rating;
    List<String > name;
    private RequestQueue requestQueue;
    private Context context;
    ReentrantLock lock;

    boolean alreadyPending;
    boolean checked;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_vehicle_choose);
        busChooseButton=findViewById(R.id.busChooseButton);
        bikeChooseButton=findViewById(R.id.bikeChooseButton);
        carChooseButton=findViewById(R.id.carChooseButton);

        lock = new ReentrantLock();

        naview = findViewById(R.id.nav_view);
        View headerView = naview.getHeaderView(0);
        username = headerView.findViewById(R.id.nav_header_title);
        rating = headerView.findViewById(R.id.nav_header_subtitle);

        alreadyPending = false;
        checked = false;


        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        name = new ArrayList<>();

        naview.setEnabled(true);

        GetMethodForName();


        //username.setText(name.get(0));

        busChooseButton.setOnClickListener(this);
        carChooseButton.setOnClickListener(this);
        bikeChooseButton.setOnClickListener(this);

        checkIfAlreadyPending();

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

                carBikeButtonFunction("bike");

                break;

            case R.id.carChooseButton:

                carBikeButtonFunction("car");

                break;
        }
    }

    synchronized void carBikeButtonFunction(String type)
    {
        lock.lock();

        Intent intent = null;

        if(!checked)
            return;

        if(!alreadyPending)
        {
            intent=new Intent(getApplicationContext(),CarBikeSearchActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }

        else
        {
            intent=new Intent(getApplicationContext(),WaitingActivity.class);
            startActivity(intent);
        }

        lock.unlock();
    }

    synchronized public void checkIfAlreadyPending()
    {
        lock.lock();

        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json";

        CustomPriorityRequest jsonObjectRequest = new CustomPriorityRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                //type.clear();
                //try
                {
                    JSONArray array = response.names();

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            boolean pending = jsonObject.getBoolean("pending");
                            if(!pending)
                                continue;

                            String user = jsonObject.getString("userEmail");

                            if(Info.currentEmail.equalsIgnoreCase(user))
                            {
                                alreadyPending = true;
                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    checked = true;

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("error: " , error.getMessage());
            }
        });

        jsonObjectRequest.setPriority(Request.Priority.HIGH);
        requestQueue.add(jsonObjectRequest);


        lock.unlock();
    }

    public void GetMethodForName()
    {
        lock.lock();

        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/users.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                            String key = array.getString(i);

                            String email = response.getJSONObject(key).getString("email");
                            if(email.equalsIgnoreCase(Info.currentEmail))
                            {
                                String n = response.getJSONObject(key).getString("name");
                                //username.setText(n);
                                Log.i("name", n);
                                username.setText(n);

                                break;
                            }

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

        lock.unlock();
    }
}