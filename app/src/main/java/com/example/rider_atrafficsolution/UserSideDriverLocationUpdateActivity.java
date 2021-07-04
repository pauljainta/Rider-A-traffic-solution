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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UserSideDriverLocationUpdateActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double sourceLat, sourceLong;

    double destLat,destLong;

    double driverLat,driverLong;

    int driverID;
    String name;

    String keyForDriverID;
    boolean busy;
    String type;

    String source, dest;

    String driverMail;
    String email;

    Handler handler;
    Runnable runnable;

    String key;



    // double estimatedFare;

    // TextView estimatedFareTextView;
   // Button acceptRequestButton,rejectRequestButton;

    TextView omuk_driver_accept_korse_textview;


    Context context;
    RequestQueue requestQueue;

    LocationManager locationManager;
    LocationListener locationListener;
    private ReentrantLock lock;
    private String keyForRequest;
    private boolean started;
    private boolean finished;
    private double fare;
    private boolean done;

    boolean checked = false;
    private boolean retrievedIntermediate;
    private boolean retrievedIntermediate2;
    private ArrayList<LatLng> intermediate;
    private Polyline polyline1;
    private ArrayList<LatLng> intermediateBetnSrcDrvr;
    private Polyline polyline2;
    private Polygon polygon;

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
        setContentView(R.layout.activity_user_side_driver_location_update);
        //estimatedFareTextView = findViewById(R.id.estimatedFareTextView);
        omuk_driver_accept_korse_textview=findViewById(R.id.omuk_driver_accept_korse_textview);

        started = false;
        finished = false;
        done = false;

        retrievedIntermediate = false;
        retrievedIntermediate2 = false;
        intermediate = new ArrayList<>();
        intermediateBetnSrcDrvr = new ArrayList<>();

        Intent intent = this.getIntent();

        sourceLat = intent.getDoubleExtra("sourceLat", 1);
        sourceLong = intent.getDoubleExtra("sourceLong", 1);
        destLat = intent.getDoubleExtra("destLat", 1);
        destLong = intent.getDoubleExtra("destLong", 1);
        driverLat = intent.getDoubleExtra("driverLat", 1);
        driverLong = intent.getDoubleExtra("driverLong", 1);
        type = intent.getStringExtra("type");
        keyForRequest = intent.getStringExtra("key");
        driverID = intent.getIntExtra("driverID",1);

        //omuk_driver_accept_korse_textview.setText("Omuk Driver Accept korse");


       // acceptRequestButton = findViewById(R.id.driver_request_accept_button);
      //  rejectRequestButton = findViewById(R.id.driver_request_reject_button);




        context = getBaseContext();
        requestQueue = Volley.newRequestQueue(context);

        lock = new ReentrantLock();

        GetKeyForLocationUpdate();

        GetRequestInfo();

        GetIntermediateLocationsBetnSrcDst();
        GetIntermediateLocationsBetnSrcDriver();

        Handler h = new Handler();
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                if(retrievedIntermediate && retrievedIntermediate2)
                {
                    update();
                    return;
                }
                h.postDelayed(this, 2000);
            }
        };
        h.postDelayed(r, 0);



        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(done)
                {
                    //handler.removeCallbacks(runnable);

                    Intent intent1 = new Intent(getApplicationContext(), UserSideJourneyCompleteActivity.class);
                    intent1.putExtra("fare", fare);
                    intent1.putExtra("email", email);
                    startActivity(intent1);

                    return;
                }

                handler.postDelayed(this, 3000);

                GetKeyForLocationUpdate();

                GetRequestInfo();

            }
        };
        handler.postDelayed(runnable, 0);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userSideDriverLocationFragment);
        mapFragment.getMapAsync(this);
    }


    private void update()
    {
        if(polyline1 == null)
            return;

        //System.out.println("intermediate" + intermediate);
        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true).color(Color.RED)
                .addAll(intermediate));

        if(polyline2 == null)
            return;

        //System.out.println("intermediate" + intermediate);
        polyline2 = mMap.addPolyline(new PolylineOptions()
                .clickable(true).color(Color.BLUE)
                .addAll(intermediateBetnSrcDrvr));
    }

    public void showLocation(LatLng latLng,String comment)
    {
        //mMap.clear();

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

//        sourceLat = intent.getDoubleExtra("sourceLat", 1);
//        sourceLong = intent.getDoubleExtra("sourceLong", 1);
//        destLat = intent.getDoubleExtra("destLat", 1);
//        destLong = intent.getDoubleExtra("destLong", 1);
//        source = intent.getStringExtra("source");
//        dest = intent.getStringExtra("dest");
//        type = intent.getStringExtra("type");

        // estimatedFare = intent.getDoubleExtra("fare", 1);
        //estimatedFare = (double) Math.round(estimatedFare * 100) / 100;

//        sourceLat= 23.738;
//        sourceLong= 90.4;
//
//        destLat=23.7561067;
//        destLong=90.38719609999998;

        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(intermediate));

        polyline2 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(intermediateBetnSrcDrvr));

        LatLng source = new LatLng(sourceLat, sourceLong);
        LatLng dest = new LatLng(destLat, destLong);

        //  estimatedFareTextView.setText("Estimated Fare "+String.valueOf(estimatedFare) + " TK");

        showLocation(source,"source");
        showLocation(dest,"destination");

        //GetKeyForLocationUpdate();

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location)
            {

                //driverLat=location.getLatitude();
                //driverLong=location.getLongitude();

                //GetKeyForLocationUpdate();

                //updateDriverLocation();


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
                    Util util = new Util();

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

                                driverLat = jsonObject.getDouble("lat");
                                driverLong = jsonObject.getDouble("long");

                                double dist = util.getDistanceFromLatLonInKm(driverLat, driverLong, sourceLat, sourceLong);

                                if(!started)
                                {
                                    if(dist < 0.5)
                                    {
                                        omuk_driver_accept_korse_textview.setText(name + " is here");
                                    }

                                    else
                                    {
                                        omuk_driver_accept_korse_textview.setText(name + " is on his way");
                                    }
                                }

                                mMap.clear();

                                LatLng driverLatLng=new LatLng(driverLat,driverLong);
                                showLocation(driverLatLng,"Driver");
                                showLocation(new LatLng(sourceLat, sourceLong),"destination");
                                showLocation(new LatLng(destLat, destLong),"source");

                                if(retrievedIntermediate && retrievedIntermediate2)
                                    update();

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

                                if(started)
                                {
                                    omuk_driver_accept_korse_textview.setText("Your ride is started");
                                }

                                if(finished)
                                {
                                    fare = jsonObject.getDouble("fare");
                                    omuk_driver_accept_korse_textview.setText("Your ride has ended\n" + "Pay TK " + fare);
                                    done = jsonObject.getBoolean("done");

                                    System.out.println("done = " + done);

                                }

                                System.out.println("s "+ source);
                                System.out.println("d " + dest);
                                System.out.println("user " + email);

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



    synchronized public void GetIntermediateLocationsBetnSrcDst()
    {
        //lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, TestMapsActivity.getMapsApiDirectionsUrl(sourceLat, sourceLong, destLat, destLong), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {

                try {
                    //Tranform the string into a json object

                    JSONArray legs = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                    for (int j = 0; j < legs.length(); j++)
                    {
                        JSONObject leg = legs.getJSONObject(j);


                        int distance = leg.getJSONObject("distance").getInt("value");


                        JSONArray steps = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(j).getJSONArray("steps");
                        for (int k = 0; k < steps.length(); k++)
                        {
                            JSONObject step = steps.getJSONObject(k);
                            String polyline = step.getJSONObject("polyline").getString("points");

                            List<LatLng> latLngs = TestMapsActivity.decodePoly(polyline);

                            intermediate.addAll(latLngs);

                            System.out.println(latLngs);
                        }
                    }

                    retrievedIntermediate = true;

                } catch (JSONException e) {

                    System.out.println("exception from distance matrix");
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


    synchronized public void GetIntermediateLocationsBetnSrcDriver()
    {
        //lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, TestMapsActivity.getMapsApiDirectionsUrl(sourceLat, sourceLong, driverLat, driverLong), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {

                try {
                    //Tranform the string into a json object

                    JSONArray legs = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                    for (int j = 0; j < legs.length(); j++)
                    {
                        JSONObject leg = legs.getJSONObject(j);


                        int distance = leg.getJSONObject("distance").getInt("value");


                        JSONArray steps = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(j).getJSONArray("steps");
                        for (int k = 0; k < steps.length(); k++)
                        {
                            JSONObject step = steps.getJSONObject(k);
                            String polyline = step.getJSONObject("polyline").getString("points");

                            List<LatLng> latLngs = TestMapsActivity.decodePoly(polyline);

                            intermediateBetnSrcDrvr.addAll(latLngs);

                            System.out.println(latLngs);
                        }
                    }

                    retrievedIntermediate2 = true;

                } catch (JSONException e) {

                    System.out.println("exception from distance matrix");
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