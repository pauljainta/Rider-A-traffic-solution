package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private ReentrantLock lock;
    private RequestQueue requestQueue;
    private Context context;
    private double dist;
    private double duration;
    private Handler handler;
    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_bike_search);


        estimatedFare = 0;

        dist = 0;

        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        lock = new ReentrantLock();

        sourceFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.source_fragment);

        destinationFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_fragment);

        sourceFragment.setCountry("BD");
        destinationFragment.setCountry("BD");


        type = getIntent().getStringExtra("type");

        if(type.equalsIgnoreCase("car"))
        {
            basicFare = 50;
            farePerKM = 30;
        }

        if(type.equalsIgnoreCase("bike"))
        {
            basicFare = 30;
            farePerKM = 15;
        }


        handler = new Handler();
        r = new Runnable()
        {
            @Override
            public void run()
            {
                showMapsActivity();
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(r, 0);

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

                //showMapsActivity();
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

                //showMapsActivity();
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
        if(!sourceSelected || !destSelected)
            return;

        GetDistance(sourceLat, sourceLong, destLat, destLong);

        if(dist == 0)
            return;

        System.out.println(dist/1000 + " KM");

        estimatedFare = dist * farePerKM;

        estimatedFare += basicFare;

        System.out.println(estimatedFare +" taka");

        //lock.unlock();

        Intent intent = new Intent(getApplicationContext(), RequestConfirmActivity.class);

        intent.putExtra("sourceLat", sourceLat);
        intent.putExtra("sourceLong", sourceLong);
        intent.putExtra("destLat", destLat);
        intent.putExtra("destLong", destLong);
        intent.putExtra("fare", estimatedFare);
        intent.putExtra("duration", duration);
        intent.putExtra("source", source);
        intent.putExtra("dest", dest);
        intent.putExtra("type", type);

        sourceSelected = false;
        destSelected = false;

        handler.removeCallbacks(r);

        startActivity(intent);

    }



    public void GetDistance(double lat1, double lon1, double lat2, double lon2)
    {
        //lock.lock();

        String API_Key = "AIzaSyC-9ghJoVuhdfodTVZ3JnpDbgx38-0PtGk";

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+lat1+","+lon1+"&destinations="+lat2+","+lon2+"&key="  + API_Key;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    lock.lock();

                    JSONArray jso = (JSONArray) response.get("rows");
                    JSONObject obj2 = (JSONObject) jso.get(0);
                    JSONArray disting = (JSONArray) obj2.get("elements");
                    JSONObject obj3 = (JSONObject) disting.get(0);
                    JSONObject obj4 =(JSONObject) obj3.get("distance");
                    JSONObject obj5 = (JSONObject) obj3.get("duration");
                    Log.i("distance", obj4.get("text").toString());

                    dist = obj4.getDouble("value");
                    dist /= 1000;

                    duration = obj5.getDouble("value");
                    duration /= 60;

                    System.out.println(obj4.get("text"));
                    System.out.println(obj5.get("text"));
                    System.out.println(dist);
                    System.out.println(duration);
                    //Log.i("dist", String.valueOf(dist));



                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    lock.unlock();
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

        //lock.unlock();
    }



}