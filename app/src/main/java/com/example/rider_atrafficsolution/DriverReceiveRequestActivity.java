package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.Hex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DriverReceiveRequestActivity extends AppCompatActivity
{
    ListView requestsListView;
    List<String> requests;
    Context context;
    RequestQueue requestQueue;
    ArrayAdapter<String > adapter;

    List<Double> latlong;
    List<String> type;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_receive_request);

        requestsListView = findViewById(R.id.requestsListView);

        requests = new ArrayList<>();

        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        type = new ArrayList<>();
        latlong = new ArrayList<>();

        update();

        Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 10000);
                Log.i("driver request timer", "updated after 10 seconds");
                update();

            }
        };
        handler.postDelayed(r, 0000);


        adapter = new ArrayAdapter< >(this, android.R.layout.simple_list_item_1, requests);
        requestsListView.setAdapter(adapter);

        for(int i=0;i<requestsListView.getChildCount();i++)
        {
            requestsListView.getChildAt(i).setBackgroundColor(0);
        }

        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(requests.contains("No pending requests right now"))
                    return;

                Intent intent = new Intent(getApplicationContext(), DriverLocationUpdate.class);
                startActivity(intent);
            }
        });

    }

    public void update()
    {
        GetDriverLocation();
        GetRequests();
    }

    public void GetDriverLocation()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Driver.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                latlong.clear();
                type.clear();
                //try
                {
                    JSONArray array = response.names();

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            String id = jsonObject.getString("driverID");

                            if(id.equalsIgnoreCase(Info.driverID))
                            {
                                latlong.add(jsonObject.getDouble("lat"));
                                latlong.add(jsonObject.getDouble("long"));
                                type.add(jsonObject.getString("type"));
                                //type.add(t);
                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(requests.isEmpty())
                    {
                        requests.add("No pending requests right now");
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

    public void GetRequests()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                requests.clear();
                //try
                {
                    JSONArray array = response.names();
                    Util util = new Util();

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            boolean pending = jsonObject.getBoolean("pending");
                            if(!pending)
                                continue;

                            String vehicleType = jsonObject.getString("type");

                            if(!vehicleType.equalsIgnoreCase(type.get(0)))
                                continue;

                            double sourceLat = jsonObject.getDouble("sourceLat");
                            double sourceLong = jsonObject.getDouble("sourceLong");
//                            double destLat = jsonObject.getDouble("destLat");
//                            double destLong = jsonObject.getDouble("destLong");

                            double dist = util.getDistanceFromLatLonInKm(sourceLat, sourceLong, latlong.get(0), latlong.get(1));

                            Log.i("distance ", String.valueOf(dist));

                            if(dist < 3.0)
                            {
                                String source = jsonObject.getString("source");
                                String dest = jsonObject.getString("dest");

                                requests.add("From: " + source + "\nTo: " + dest);

                                //Log.i("request", requests.get(0));
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(requests.isEmpty())
                    {
                        requests.add("No pending requests right now");
                    }

                    adapter = new ArrayAdapter< String>(context, android.R.layout.simple_list_item_1, requests);
                    requestsListView.setAdapter(adapter);

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