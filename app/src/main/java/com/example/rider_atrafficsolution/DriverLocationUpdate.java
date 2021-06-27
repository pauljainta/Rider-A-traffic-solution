package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

public class DriverLocationUpdate extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double sourceLat, sourceLong;

    double destLat,destLong;

    double driverLat,driverLong;

    String source, dest;

    String driverMail;

    String type;

   // double estimatedFare;

   // TextView estimatedFareTextView;
    Button acceptRequestButton,rejectRequestButton;

    Context context;
    RequestQueue requestQueue;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_confirm);
        //estimatedFareTextView = findViewById(R.id.estimatedFareTextView);

        context = getBaseContext();

      //  requestQueue = Volley.newRequestQueue(context);
        acceptRequestButton = findViewById(R.id.driver_request_accept_button);
        rejectRequestButton=findViewById(R.id.driver_request_reject_button);

        driverMail=Info.driverID;





        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.car_request_confirm);
        mapFragment.getMapAsync(this);
    }



    public void showLocation(LatLng latLng,String comment)
    {

        mMap.addMarker(new MarkerOptions().position(latLng).title(comment));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));

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

        // Add a marker in Sydney and move the camera
        Intent intent = this.getIntent();

//        sourceLat = intent.getDoubleExtra("sourceLat", 1);
//        sourceLong = intent.getDoubleExtra("sourceLong", 1);
//        destLat = intent.getDoubleExtra("destLat", 1);
//        destLong = intent.getDoubleExtra("destLong", 1);
//        source = intent.getStringExtra("source");
//        dest = intent.getStringExtra("dest");
//        type = intent.getStringExtra("type");

       // estimatedFare = intent.getDoubleExtra("fare", 1);
        //estimatedFare = (double) Math.round(estimatedFare * 100) / 100;

        sourceLat= 23.738;
        sourceLong= 90.4;

        destLat=23.7561067;
        destLong=90.38719609999998;

        LatLng source = new LatLng(sourceLat, sourceLong);
        LatLng dest = new LatLng(destLat, destLong);

      //  estimatedFareTextView.setText("Estimated Fare "+String.valueOf(estimatedFare) + " TK");

        showLocation(source,"source");
        showLocation(dest,"destination");

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                driverLat=location.getLatitude();
                driverLong=location.getLongitude();
                LatLng driverLatLng=new LatLng(driverLat,driverLong);
                showLocation(driverLatLng,"Driver");


            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        }

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

        // after generating our bitmap we are returning our bit       map.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



//    public void sendRequest(String email, double sourceLat, double sourceLong, double destLat, double destLong, String source, String dest, boolean pending)
//    {
//        try
//        {
//            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json";
//            JSONObject jsonBody = new JSONObject();
//            jsonBody.put("userEmail", email);
//            jsonBody.put("sourceLat", sourceLat);
//            jsonBody.put("sourceLong", sourceLong);
//            jsonBody.put("destLat", destLat);
//            jsonBody.put("destLong", destLong);
//            jsonBody.put("source", source);
//            jsonBody.put("dest", dest);
//            jsonBody.put("pending", pending);
//            jsonBody.put("type", type);
//
//
//            final String requestBody = jsonBody.toString();
//
//            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.i("VOLLEY", response);
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.e("VOLLEY", error.toString());
//                }
//            }) {
//                @Override
//                public String getBodyContentType() {
//                    return "application/json; charset=utf-8";
//                }
//
//                @Override
//                public byte[] getBody() throws AuthFailureError
//                {
//                    try {
//                        return requestBody == null ? null : requestBody.getBytes("utf-8");
//                    } catch (UnsupportedEncodingException uee) {
//                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
//                        return null;
//                    }
//                }
//
//                @Override
//                protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                    String responseString = "";
//                    if (response != null) {
//                        responseString = String.valueOf(response.statusCode);
//                        // can get more details such as response.headers
//                    }
//                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//                }
//            };
//
//            requestQueue.add(stringRequest);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}