package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TestMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener
{

    private GoogleMap mMap;

    RequestQueue requestQueue;
    Context context;
    ReentrantLock lock;
    List<LatLng> intermediate;
    Polyline polyline1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_maps);
        context = getBaseContext();
        requestQueue = Volley.newRequestQueue(context);
        lock = new ReentrantLock();
        intermediate = new ArrayList<>();



        GetDriverLocation();

        Handler handler =new Handler();
        final Runnable r = new Runnable()
        {
            public void run()
            {
                if(!intermediate.isEmpty())
                {
                    update();
                    return;
                }

                handler.postDelayed(this, 5000);
                Log.i("map timer", "updated after 5 seconds");
                update();


            }
        };
        handler.postDelayed(r, 0000);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    synchronized public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(intermediate));

        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.7561, 90.3872), 12));

        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);
    }

    public void update()
    {
        if(polyline1 == null)
            return;

        System.out.println("intermediate" + intermediate);
        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(intermediate));
    }


    private String getMapsApiDirectionsUrl()
    {


        String origin = "origin=" + "23.7561" + "," + "90.3872";
        //String waypoints = "waypoints=optimize:true|" + BROOKLYN_BRIDGE.latitude + "," + BROOKLYN_BRIDGE.longitude + "|";
        String destination = "destination=" + "23.8223" + "," + "90.3654";

        String sensor = "sensor=false";
        String params = origin + "&"  + "&"  + destination + "&" + sensor + "&key=" + "AIzaSyC-9ghJoVuhdfodTVZ3JnpDbgx38-0PtGk";
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    synchronized public void GetDriverLocation()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getMapsApiDirectionsUrl(), null, new Response.Listener<JSONObject>() {
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

                            List<LatLng> latLngs = decodePoly(polyline);

                            intermediate.addAll(latLngs);

                            System.out.println(latLngs);
                        }
                    }


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

        lock.unlock();
    }


    @Override
    public void onPolygonClick(@NonNull Polygon polygon)
    {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline)
    {

    }
}