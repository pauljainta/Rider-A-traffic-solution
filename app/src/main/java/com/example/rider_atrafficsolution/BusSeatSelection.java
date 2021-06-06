package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BusSeatSelection extends AppCompatActivity
{
    private static Context context;
    Spinner busfromSpinner,bustoSpinner,whichbuSpinner, seatCountSpinner;
    private String from;
    private String to;
    private double fare;
    List<String> buses;
    TextView fareShowTextView;

    ArrayAdapter<String> busAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_seat_selection);

        busfromSpinner=findViewById(R.id.busFromSpinner);
        bustoSpinner=findViewById(R.id.busToSpinner);
        whichbuSpinner=findViewById(R.id.whichBusSpinner);
        seatCountSpinner = findViewById(R.id.seatCountSpinner);
        fareShowTextView = findViewById(R.id.fareShowTextView);

        context = getBaseContext();


        List<String> locations = new ArrayList<String>();
        locations.add("Motijheel");
        locations.add("Shahbag");
        locations.add("Farmgate");
        locations.add("Malibag");
        locations.add("Mirpur");
        locations.add("Mohdpur");


        buses = new ArrayList<String>();
        from = "";
        to = "";

        List<String> counts = new ArrayList<>();
        counts.add("1");
        counts.add("2");
        counts.add("3");
        counts.add("4");

        // Creating adapter for spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);

        busAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buses);

        ArrayAdapter<String > countsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, counts);


        // Drop down layout style - list view with radio button
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        busAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        // attaching data adapter to spinner
        busfromSpinner.setAdapter(locationAdapter);
        bustoSpinner.setAdapter(locationAdapter);
        whichbuSpinner.setAdapter(busAdapter);
        seatCountSpinner.setAdapter(countsAdapter);


        busfromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                from = busfromSpinner.getSelectedItem().toString();
                GetMethodForBusRoute();
                GetMethodForFare();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        bustoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                to = bustoSpinner.getSelectedItem().toString();
                GetMethodForBusRoute();
                GetMethodForFare();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


    }



    public void confirm(View view)
    {
        Log.i("from", busfromSpinner.getSelectedItem().toString());
        Log.i("to", bustoSpinner.getSelectedItem().toString());

        from = busfromSpinner.getSelectedItem().toString();
        to = bustoSpinner.getSelectedItem().toString();

        GetMethodForBusRoute();
    }

    public void GetMethodForFare()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/fare.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                //try
                {
                    JSONArray array = response.names();

                    buses.clear();

                    for(int i=0;i<array.length();i++)
                    {
                        List<String> locations = new ArrayList<>();
                        try
                        {
                            String key = array.getString(i);

                            String location1 = response.getJSONObject(key).getString("location1");
                            String location2 = response.getJSONObject(key).getString("location2");

                            if((location1.equalsIgnoreCase(from) && location2.equalsIgnoreCase(to)) || (location2.equalsIgnoreCase(from) && location1.equalsIgnoreCase(to)) )
                            {
                                fare = Double.parseDouble(response.getJSONObject(key).getString("fare"));
                                Log.i("fare", String.valueOf(fare));
                                fareShowTextView.setText(String.valueOf(fare));
                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }


                    busAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, buses);

                    whichbuSpinner.setAdapter(busAdapter);
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

    public void GetMethodForBusRoute()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/BusRoute.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                //try
                {
                    JSONArray array = response.names();

                    buses.clear();

                    for(int i=0;i<array.length();i++)
                    {
                        List<String> locations = new ArrayList<>();
                        try
                        {
                            String key = array.getString(i);
                            locations.add(response.getJSONObject(key).getString("from"));
                            //Log.i("names", response.getJSONObject(array.getString(i)).getString("from"));
                            String[] splitted = response.getJSONObject(key).getString("intermediate").split(",");
                            for(String s : splitted)
                            {
                                //Log.i("route", s);
                                locations.add(s);
                            }
                            locations.add(response.getJSONObject(key).getString("to"));
//                            Log.i("names", array.getString(i));

                            if (locations.contains(from) && locations.contains(to))
                            {
                                String busnos = response.getJSONObject(key).getString("busNo");

                                String [] split = busnos.split(",");

                                for (String s : split)
                                {
                                    buses.add(s);
                                }
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    for (String b : buses)
                    {
                        Log.i("bus", b);
                    }

                    busAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, buses);

                    whichbuSpinner.setAdapter(busAdapter);
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