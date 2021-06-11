package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShowBusLoationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Context context;

    LocationManager locationManager;
    LocationListener locationListener;

    RequestQueue requestQueue;

    double minDistLat;
    double minDistLong;

    double fromLat;
    double fromLong;
    double toLat;
    double toLong;

    int busId;

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
//        {
//
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
//        }
//    }

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

      //  LatLng startCounter = null,endCounter=null;

//        Log.i("bal","bal");


//        fromLat = getIntent().getDoubleExtra("fromLat", 23.732972);
//        fromLong = getIntent().getDoubleExtra("fromLong", 23.732972);
//        toLat = getIntent().getDoubleExtra("toLat", 23.732972);
//        toLong = getIntent().getDoubleExtra("toLong", 23.732972);

//        Log.i("map", String.valueOf(BusSeatSelection.fromLat));
//        Log.i("map", String.valueOf(BusSeatSelection.fromLong));
//        Log.i("map", String.valueOf(BusSeatSelection.toLat));
//        Log.i("map", String.valueOf(BusSeatSelection.toLong));



//        LatLng startCounter = new LatLng(BusSeatSelection.fromLat, BusSeatSelection.fromLong);
//        LatLng endCounter = new LatLng(BusSeatSelection.toLat, BusSeatSelection.toLong);

        LatLng startCounter = new LatLng(fromLat, fromLong);
        LatLng endCounter = new LatLng(toLat, toLong);
        LatLng busLocation=new LatLng(minDistLat,minDistLong);

        showLocation(startCounter,"Start");
        showLocation(endCounter,"End");
        showLocation(busLocation,"Bus");


        Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 30000);
                Log.i("timer", "updated after 30 seconds");
                GetCurrentLocation();
                showLocation(new LatLng(minDistLat, minDistLong), "Bus");

            }
        };
        handler.postDelayed(r, 0000);

//        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//        locationListener=new LocationListener() {
//            @Override
//            public void onLocationChanged(@NonNull Location location) {
//
//                //    Log.i("Location:",location.toString());
//                mMap.clear();
//                showLocation(startCounter,"Start");
//                showLocation(endCounter,"End");
//                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
//                mMap.addMarker(new MarkerOptions().position(latLng).title("Bus Location")
//                        // below line is use to add custom marker on our map.
//                        .icon(BitmapFromVector(getApplicationContext(), R.drawable.bus)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
//
//
//            }
//        };
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
//        }
//        else
//        {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
//
//        }



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

                            int id = response.getJSONObject(key).getInt("busId");

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