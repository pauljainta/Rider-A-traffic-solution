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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.ReentrantLock;

public class DriverLocationUpdate extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;

    double sourceLat, sourceLong;

    double destLat,destLong;

    double driverLat,driverLong;

    int driverID;
    String name;
    String userName;

    String keyForDriverID;
    boolean busy;
    boolean accepted;

    String type;

    String source, dest;

    String driverMail;
    String email;

    boolean startedChecked;
    boolean started;
    boolean finished;

    String startTime;
    String finishTime;

    boolean thisWindowDone;

    // double estimatedFare;

   // TextView estimatedFareTextView;
    Button acceptRequestButton,rejectRequestButton, startRideButton, finishRideButton, proceedButton;
    Button ridingWithButton;

    Context context;
    RequestQueue requestQueue;

    LocationManager locationManager;
    LocationListener locationListener;
    private ReentrantLock lock;
    private String keyForRequest;
    private double fare;
    private String userEmail;

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
        setContentView(R.layout.activity_driver_location_update);
        //estimatedFareTextView = findViewById(R.id.estimatedFareTextView);

        thisWindowDone = false;

        busy = false;

        Intent intent = this.getIntent();

        sourceLat = intent.getDoubleExtra("sourceLat", 1);
        sourceLong = intent.getDoubleExtra("sourceLong", 1);
        destLat = intent.getDoubleExtra("destLat", 1);
        destLong = intent.getDoubleExtra("destLong", 1);
        driverLat = intent.getDoubleExtra("driverLat", 1);
        driverLong = intent.getDoubleExtra("driverLong", 1);
        type = intent.getStringExtra("type");
        keyForRequest = intent.getStringExtra("key");
        driverID = Integer.parseInt(intent.getStringExtra("driverID"));

        accepted = false;

        started = false;
        finished = false;
        startedChecked = false;

        acceptRequestButton = findViewById(R.id.driver_request_accept_button);
        rejectRequestButton = findViewById(R.id.driver_request_reject_button);
        startRideButton = findViewById(R.id.startRideButton);
        finishRideButton = findViewById(R.id.finishRideButton);
        proceedButton = findViewById(R.id.proceedToPaymentButton);
        ridingWithButton = findViewById(R.id.ridingWith);

        startRideButton.setVisibility(View.GONE);
        startRideButton.setEnabled(false);

        finishRideButton.setVisibility(View.GONE);
        finishRideButton.setEnabled(false);

        proceedButton.setVisibility(View.GONE);
        proceedButton.setEnabled(false);

        ridingWithButton.setVisibility(View.GONE);
        ridingWithButton.setEnabled(false);

        if (intent.getStringExtra("classid").equalsIgnoreCase("driver2"))
        {
            acceptRequestButton.setVisibility(View.GONE);
            rejectRequestButton.setVisibility(View.GONE);

            //startRideButton.setVisibility(View.VISIBLE);
            accepted = true;
        }

        if (intent.getStringExtra("classid").equalsIgnoreCase("driver"))
        {
            startRideButton.setVisibility(View.GONE);
            startRideButton.setEnabled(false);

            finishRideButton.setVisibility(View.GONE);
            finishRideButton.setEnabled(false);
        }


        context = getBaseContext();
        requestQueue = Volley.newRequestQueue(context);

        lock = new ReentrantLock();

        GetKeyForLocationUpdate();

        GetRequestInfo();

        Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(email != null)
                {
                    GetUserName();
                    return;
                }
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(runnable, 0);
      //  requestQueue = Volley.newRequestQueue(context);


        //driverMail=Info.driverID;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverLocationFragment);
        mapFragment.getMapAsync(this);


        acceptRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(keyForDriverID == null)
                    return;

                Log.i("accept", "clicked");

                accepted = true;
                updateRequestStatus(false, false);
                acceptRequestButton.setVisibility(View.GONE);
                rejectRequestButton.setVisibility(View.GONE);
                //acceptRequestButton.setEnabled(false);
                //rejectRequestButton.setEnabled(false);

                busy = true;
                updateDriverLocation(true);

                //startRideButton.setVisibility(View.VISIBLE);
            }
        });

        rejectRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("reject", "clicked");
                Intent intent1 = new Intent(getApplicationContext(), DriverReceiveRequestActivity.class);

                thisWindowDone = true;
                locationManager.removeUpdates(locationListener);
                locationListener = null;

                startActivity(intent1);
            }
        });



        startRideButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis()));

                updateRequestStatus(true, false);
                started = true;
                startRideButton.setVisibility(View.GONE);
                Log.i("start", "ride started");

                ridingWithButton.setVisibility(View.VISIBLE);
            }
        });


        finishRideButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finishTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis()));

                System.out.println(fare);

                fare = Math.round(CalculateFareClass.CalculateFare(fare));
                System.out.println("last fare " + fare);

                updateRequestStatus(true, true);
                finished = true;

                ridingWithButton.setVisibility(View.GONE);
                finishRideButton.setVisibility(View.GONE);
                finishRideButton.setEnabled(false);

                proceedButton.setVisibility(View.VISIBLE);
                proceedButton.setEnabled(true);
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent1 = new Intent(getApplicationContext(), DriverCollectCashActivity.class);

                intent1.putExtra("destLat", destLat);
                intent1.putExtra("destLong", destLong);
                intent1.putExtra("sourceLat", sourceLat);
                intent1.putExtra("sourceLong", sourceLong);
                intent1.putExtra("driverLat", driverLat);
                intent1.putExtra("driverLong", driverLong);
                intent1.putExtra("dest", dest);
                intent1.putExtra("source", source);
                intent1.putExtra("type", type);
                intent1.putExtra("fare", fare);
                intent1.putExtra("key", keyForRequest);
                intent1.putExtra("key2", keyForDriverID);
                intent1.putExtra("driverName", name);
                intent1.putExtra("userEmail", userEmail);
                intent1.putExtra("userName", userName);
                intent1.putExtra("startTime", startTime);
                intent1.putExtra("finishTime", finishTime);

                thisWindowDone = true;

                locationManager.removeUpdates(locationListener);
                locationListener = null;

                startActivity(intent1);
            }
        });
    }






    public void showLocation(LatLng latLng,String comment)
    {
        if(comment.equalsIgnoreCase("Driver"))
        {
            if(type.equalsIgnoreCase("car"))
            {
                mMap.addMarker(new MarkerOptions().position(latLng).title("car")
                        // below line is use to add custom marker on our map.
                        .icon(BitmapFromVector(getApplicationContext(), R.drawable.car)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
            }

            if(type.equalsIgnoreCase("bike"))
            {
                mMap.addMarker(new MarkerOptions().position(latLng).title("bike")
                        // below line is use to add custom marker on our map.
                        .icon(BitmapFromVector(getApplicationContext(), R.drawable.bike)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
            }
        }
        else
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title(comment));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
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

        // Add a marker in Sydney and move the camera
        Intent intent = this.getIntent();


        LatLng source = new LatLng(sourceLat, sourceLong);
        LatLng dest = new LatLng(destLat, destLong);

      //  estimatedFareTextView.setText("Estimated Fare "+String.valueOf(estimatedFare) + " TK");

        showLocation(source,"source");
        showLocation(dest,"destination");

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener()
        {
            @Override
            public void onLocationChanged(@NonNull Location location)
            {
                if(keyForDriverID == null || thisWindowDone)
                    return;

                driverLat=location.getLatitude();
                driverLong=location.getLongitude();
                LatLng driverLatLng=new LatLng(driverLat,driverLong);
                mMap.clear();

                if(startedChecked && accepted)
                {
                    Util util = new Util();

                    if(!started)
                    {
                        double dist = util.getDistanceFromLatLonInKm(driverLat, driverLong, sourceLat, sourceLong);

                        if(dist < 0.5)
                        {
                            startRideButton.setVisibility(View.VISIBLE);
                            startRideButton.setEnabled(true);
                        }

                        else
                        {
                            startRideButton.setVisibility(View.GONE);
                            startRideButton.setEnabled(false);
                        }

                    }

                    else
                    {
                        startRideButton.setVisibility(View.GONE);
                        startRideButton.setEnabled(false);

                        if(!finished)
                        {
                            double dist = util.getDistanceFromLatLonInKm(driverLat, driverLong, destLat, destLong);

                            if(dist < 0.5)
                            {
                                finishRideButton.setVisibility(View.VISIBLE);
                                finishRideButton.setEnabled(true);
                            }

                        }

                        else
                        {
                            finishRideButton.setVisibility(View.GONE);
                            finishRideButton.setEnabled(false);
                        }
                    }
                }

                showLocation(source,"source");
                showLocation(dest,"destination");
                showLocation(driverLatLng,"Driver");

                if(!busy)
                    updateDriverLocation(busy);
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

    synchronized public void GetKeyForLocationUpdate()
    {

        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Driver.json", null, new Response.Listener<JSONObject>() {
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

                            int id = jsonObject.getInt("driverID");

                            if(id == driverID)
                            {
                                keyForDriverID = key;
                                //busy = jsonObject.getBoolean("busy");
                                name = jsonObject.getString("name");
                                busy = jsonObject.getBoolean("busy");

                                //updateMessage();

                                //type.add(t);
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



    public void GetUserName()
    {
        lock.lock();

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
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            userEmail = jsonObject.getString("email");

                            if(email.equalsIgnoreCase(userEmail))
                            {
                                userName = jsonObject.getString("name");

                                ridingWithButton.setText("You are sharing ride with " +  userName);

                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }


                    startedChecked = true;

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


    synchronized public void GetRequestInfo()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json", null, new Response.Listener<JSONObject>() {
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

                            if(key.equalsIgnoreCase(keyForRequest))
                            {
                                JSONObject jsonObject = response.getJSONObject(key);

                                source = jsonObject.getString("source");
                                dest = jsonObject.getString("dest");
                                email = jsonObject.getString("userEmail");
                                started = jsonObject.getBoolean("started");
                                finished = jsonObject.getBoolean("finished");
                                fare = jsonObject.getDouble("fare");

                                break;
                            }



                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }


                    startedChecked = true;

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

    synchronized public void updateRequestStatus(boolean started, boolean finished)
    {
        lock.lock();
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request/" + keyForRequest + ".json";
            JSONObject jsonBody = new JSONObject();


            jsonBody.put("destLat", destLat);
            jsonBody.put("destLong", destLong);
            jsonBody.put("sourceLat", sourceLat);
            jsonBody.put("sourceLong", sourceLong);
            jsonBody.put("userEmail", email);
            jsonBody.put("pending", false);
            jsonBody.put("accepted_by", Info.driverID);
            jsonBody.put("dest", dest);
            jsonBody.put("source", source);
            jsonBody.put("type", type);
            jsonBody.put("started", started);
            jsonBody.put("finished", finished);
            jsonBody.put("done", false);
            jsonBody.put("fare", fare);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError
                {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        lock.unlock();
    }




    synchronized public void updateDriverLocation(boolean busy)
    {
        lock.lock();
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Driver/" + keyForDriverID + ".json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("lat", driverLat);
            jsonBody.put("long", driverLong);
            jsonBody.put("driverID", driverID);
            jsonBody.put("type", type);
            jsonBody.put("busy", busy);
            jsonBody.put("name", name);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError
                {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        lock.unlock();
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