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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class BusDriverLocationUpdate extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    LocationManager locationManager;
    LocationListener locationListener;

    int availableSeats;
    int busID;
    int busNo;
    double lat;
    double longt;
    private RequestQueue requestQueue;
    Context context;
    private String keyForLocation;
    private ArrayList<LatLng> route;

    boolean [] retrievedIntermediate;

    Polyline[] polyline;
    ArrayList<ArrayList<LatLng>> intermediate;
    private boolean gotRoute;

    private AlertDialog.Builder builder;

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
        setContentView(R.layout.activity_bus_driver_location_update);

        busID = Integer.parseInt(Info.driverID);

        keyForLocation = "empty";

        builder = new AlertDialog.Builder(this);

        builder.setMessage("Stop The Bus")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        context = getBaseContext();
        requestQueue = Volley.newRequestQueue(context);

        route = new ArrayList<>();

        gotRoute = false;

        GetCurrentLocation();

        GetRoute();




        Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(gotRoute)
                {
                    retrievedIntermediate = new boolean[route.size()-1];
                    intermediate = new ArrayList<>();
                    polyline = new Polyline[route.size()-1];

                    for (int i=0;i<route.size()-1;i++)
                    {
                        intermediate.add(new ArrayList<LatLng>());
                    }

                    Arrays.fill(retrievedIntermediate, false);

                    for(int i=0;i<route.size()-1;i++)
                    {
                        GetIntermediateLocations(i);
                    }

                    return;
                }

                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(runnable, 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void update()
    {
        if(polyline == null)
            return;

        for(int i=0;i<polyline.length;i++)
        {
            if(polyline[i] == null)
                return;
        }

        //System.out.println("intermediate" + intermediate);

        for(int i=0;i<polyline.length;i++)
        {
            polyline[i] = mMap.addPolyline(new PolylineOptions()
                    .clickable(true).color(Color.RED)
                    .addAll(intermediate.get(i)));
        }

    }


    public void showLocation(LatLng latLng,String comment)
    {
        if(comment.equalsIgnoreCase("Bus"))
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Bus")
                    // below line is use to add custom marker on our map.
                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.bus)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
        }
        else
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title(comment));               //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
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

        Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 2000);

                for(int i=0;i<route.size()-1;i++)
                {
                    if (!retrievedIntermediate[i])
                        break;

                    for(int j=0;j<polyline.length;j++)
                    {
                        polyline[j] = mMap.addPolyline(new PolylineOptions()
                                .clickable(true).color(Color.RED)
                                .addAll(new ArrayList<LatLng>()));
                    }

                    for(LatLng l : route)
                    {
                        showLocation(l, "stoppage");
                    }


                    update();

                }
                //update();

            }
        };
        handler.postDelayed(r, 0000);

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                if(!keyForLocation.equalsIgnoreCase("empty"))
                {
                    lat = location.getLatitude();
                    longt = location.getLongitude();

                    for(int j=0;j<route.size()-1;j++)
                    {
                        LatLng ll = route.get(j);

                        double dist = new Util().getDistanceFromLatLonInKm(lat, longt, ll.latitude, ll.longitude);

                        if(dist < 0.5)
                        {
                            AlertDialog alert = builder.create();
                            //Setting the title manually
                            alert.setTitle("Bus Stoppage Nearby");
                            alert.show();
                            //create alert box here
                        }

                    }


                    updateDriverLocation();
                }

                mMap.clear();
                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                showLocation(latLng,"Bus");


            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10,locationListener);

        }
    }


    synchronized void GetCurrentLocation()
    {
        CustomPriorityRequest jsonObjectRequest = new CustomPriorityRequest(com.android.volley.Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/BusTable.json", null, new Response.Listener<JSONObject>() {
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

                            int busIdFromJson = response.getJSONObject(key).getInt("busID");

                            if(busID == busIdFromJson)
                            {
                                availableSeats = response.getJSONObject(key).getInt("availableSeats");
                                busNo = response.getJSONObject(key).getInt("busNo");
                                lat = response.getJSONObject(key).getDouble("lat");
                                longt = response.getJSONObject(key).getDouble("long");

                                keyForLocation = key;

                                showLocation(new LatLng(lat, longt), "Bus");

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

        jsonObjectRequest.setPriority(Request.Priority.NORMAL);
        requestQueue.add(jsonObjectRequest);
    }

    synchronized public void updateDriverLocation()
    {
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/BusTable/" + keyForLocation + ".json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("availableSeats", availableSeats);
            jsonBody.put("busID", busID);
            jsonBody.put("busNo", busNo);
            jsonBody.put("lat", lat);
            jsonBody.put("long", longt);


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

    }







    synchronized public void GetIntermediateLocations(int i)
    {
        //lock.lock();

        double lat1 = route.get(i).latitude;
        double lon1 = route.get(i).longitude;
        double lat2 = route.get(i+1).latitude;
        double lon2 = route.get(i+1).longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, TestMapsActivity.getMapsApiDirectionsUrl(lat1, lon1, lat2, lon2), null, new Response.Listener<JSONObject>() {
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

                            ArrayList<LatLng> temp = new ArrayList<>(latLngs);

                            intermediate.get(i).addAll(temp);

                            System.out.println(latLngs);
                        }
                    }

                    retrievedIntermediate[i] = true;

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




    public void GetRoute()
    {
        CustomPriorityRequest jsonObjectRequest = new CustomPriorityRequest(com.android.volley.Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/CoOrdinates.json", null, new Response.Listener<JSONObject>() {
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

                            double lt = response.getJSONObject(key).getDouble("lat");
                            double ln = response.getJSONObject(key).getDouble("long");
                            String name = response.getJSONObject(key).getString("location");


                            route.add(new LatLng(lt, ln));
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                gotRoute = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("error: " , error.getMessage());
            }
        });

        jsonObjectRequest.setPriority(Request.Priority.NORMAL);
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

        // after generating our bitmap we are returning our bit       map.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}