package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    int busId;
    private Util util;
    private AlertDialog.Builder builder;

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

        busId = getIntent().getIntExtra("busId", 1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



//        Log.i("v", String.valueOf(minDistLat));
//        Log.i("v", String.valueOf(minDistLong));
        Log.i("id", String.valueOf(busId));

        util=new Util();
        builder = new AlertDialog.Builder(this);



//        Log.i("map", String.valueOf(BusSeatSelection.fromLat));
//        Log.i("map", String.valueOf(BusSeatSelection.fromLong));
//        Log.i("map", String.valueOf(BusSeatSelection.toLat));
//        Log.i("map", String.valueOf(BusSeatSelection.toLong));
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
        mMap.clear();
        showLocation(startCounter,"Start");
        showLocation(endCounter,"End");
        showLocation(new LatLng(minDistLat, minDistLong), "Bus");

        if(util.getDistanceFromLatLonInKm(startCounter.latitude,startCounter.longitude,busLocation.latitude,busLocation.longitude)<=0.3)
        {
            builder.setMessage("Bus has arrived")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mMap.clear();
                            showLocation(startCounter,"Start");
                            showLocation(endCounter,"End");
                            showLocation(new LatLng(minDistLat, minDistLong), "Bus");

                        }});
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
                handler.postDelayed(this, 30000);
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