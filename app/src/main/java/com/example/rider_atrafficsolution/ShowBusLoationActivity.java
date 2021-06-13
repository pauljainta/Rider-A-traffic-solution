package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowBusLoationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Context context;

    RequestQueue requestQueue;

    double minDistLat;
    double minDistLong;

    double fromLat;
    double fromLong;
    double toLat;
    double toLong;
    double fare;

    int busId;
    private Util util;
    private AlertDialog.Builder sourceAlertBuilder;
    private AlertDialog.Builder destinationAlertBuilder;

    Boolean isBusAlertShown;
    Boolean isDestinationAlertShown;

    LatLng startCounter,endCounter,busLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bus_loation);

        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        minDistLat = getIntent().getDoubleExtra("minDistLat", 1);
        minDistLong = getIntent().getDoubleExtra("minDistLong", 1);

        fromLat = getIntent().getDoubleExtra("fromLat", 1);
        fromLong = getIntent().getDoubleExtra("fromLong", 1);
        toLat = getIntent().getDoubleExtra("toLat", 1);
        toLong = getIntent().getDoubleExtra("toLong", 1);
        fare = getIntent().getDoubleExtra("fare", 1);

        Log.i("map", String.valueOf(fromLat));
        Log.i("map", String.valueOf(fromLong));
        Log.i("map", String.valueOf(toLat));
        Log.i("map", String.valueOf(toLong));

        busId = getIntent().getIntExtra("busId", 1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



//        Log.i("v", String.valueOf(minDistLat));
//        Log.i("v", String.valueOf(minDistLong));
        Log.i("id", String.valueOf(busId));

        isBusAlertShown=false;
        isDestinationAlertShown = false;
        util=new Util();

        sourceAlertBuilder = new AlertDialog.Builder(this);

        sourceAlertBuilder.setMessage("Get On The Bus")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mMap.clear();
                        showLocation(startCounter,"Start");
                        showLocation(endCounter,"End");
                        showLocation(new LatLng(minDistLat, minDistLong), "Bus");


                    }});


        destinationAlertBuilder = new AlertDialog.Builder(this);

        destinationAlertBuilder.setMessage("You have reached the destination")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent(getApplicationContext(), BusJourneyCompleteActivity.class);
                        intent.putExtra("fare", fare);
                        startActivity(intent);
                    }
                });


    }


    public void showLocation(LatLng latLng,String comment)
    {

        if(comment.equalsIgnoreCase("Bus"))
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Bus Location")
                    // below line is use to add custom marker on our map.
                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.bus)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
        }

        else {
            mMap.addMarker(new MarkerOptions().position(latLng).title(comment));
        }

    }


    public void updateUI()
    {
        Log.i("bus map","bus update");
        mMap.clear();
        showLocation(startCounter,"Start");
        showLocation(endCounter,"End");
        showLocation(new LatLng(minDistLat, minDistLong), "Bus");

        Log.i("distance",util.getDistanceFromLatLonInKm(startCounter.latitude,startCounter.longitude,busLocation.latitude,busLocation.longitude)+"");

        if(util.getDistanceFromLatLonInKm(startCounter.latitude,startCounter.longitude, minDistLat, minDistLong)<=0.5 && !isBusAlertShown)
        {

            isBusAlertShown=true;
            Log.i("src","bus arrived");
            AlertDialog alert = sourceAlertBuilder.create();
            alert.setTitle("Bus has Arrived");
            alert.show();
        }

        if(util.getDistanceFromLatLonInKm(endCounter.latitude, endCounter.longitude, minDistLat, minDistLong)<=0.5 && !isDestinationAlertShown && isBusAlertShown)
        {

            isDestinationAlertShown=true;
            Log.i("dest","chole asche");
            AlertDialog alert = destinationAlertBuilder.create();
            alert.setTitle("Bus has reached");
            alert.show();
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



         startCounter = new LatLng(fromLat, fromLong);
         endCounter = new LatLng(toLat, toLong);
         busLocation=new LatLng(minDistLat,minDistLong);

        showLocation(startCounter,"Start");
        showLocation(endCounter,"End");
        showLocation(busLocation,"Bus");

        Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 5000);
                Log.i("timer", "updated after 30 seconds");
                GetCurrentLocation();
                updateUI();

            }
        };
        handler.postDelayed(r, 0000);





    }

    public void GetCurrentLocation()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/BusTable.json", null, new Response.Listener<JSONObject>() {
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

                            int id = response.getJSONObject(key).getInt("busID");

                            if(id == busId)
                            {
                                minDistLat = response.getJSONObject(key).getDouble("lat");
                                minDistLong = response.getJSONObject(key).getDouble("long");

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
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);
        int height = 100;
        int width = 100;
        bitmap=Bitmap.createScaledBitmap(bitmap, width, height, false);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}