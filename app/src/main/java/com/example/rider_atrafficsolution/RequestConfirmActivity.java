package com.example.rider_atrafficsolution;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RequestConfirmActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double sourceLat, sourceLong;

    double destLat,destLong;

    String source, dest;

    String type;

    double estimatedFare;

    TextView estimatedFareTextView;
    Button sendRequestButton;

    Context context;
    RequestQueue requestQueue;
    private double duration;
    private String keyForRequest;
    private ArrayList<LatLng> intermediate;
    private boolean retrievedIntermediate;
    private Polyline polyline1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_confirm);
        estimatedFareTextView = findViewById(R.id.estimatedFareTextView);
        intermediate = new ArrayList<>();
        retrievedIntermediate = false;

        Intent intent = this.getIntent();

        sourceLat = intent.getDoubleExtra("sourceLat", 1);
        sourceLong = intent.getDoubleExtra("sourceLong", 1);
        destLat = intent.getDoubleExtra("destLat", 1);
        destLong = intent.getDoubleExtra("destLong", 1);
        source = intent.getStringExtra("source");
        dest = intent.getStringExtra("dest");
        type = intent.getStringExtra("type");

        estimatedFare = intent.getDoubleExtra("fare", 1);
        estimatedFare = Math.round(estimatedFare);
        System.out.println("estimated fare " + estimatedFare);

        duration = intent.getDoubleExtra("duration", 1);
        duration = Math.round(duration);

        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);
        sendRequestButton = findViewById(R.id.carrequestconfirmbutton);

        GetIntermediateLocations();

        Handler h = new Handler();
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                if(retrievedIntermediate)
                {
                    update();
                    return;
                }
                h.postDelayed(this, 2000);
            }
        };
        h.postDelayed(r, 0);



        sendRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendRequest(Info.currentEmail, sourceLat, sourceLong, destLat, destLong, source, dest, estimatedFare, true);

                //GetKeyForRequest();

                Intent intent = new Intent(getApplicationContext(), WaitingActivity.class);

                intent.putExtra("sourceLat", sourceLat);
                intent.putExtra("sourceLong", sourceLong);
                intent.putExtra("destLat", destLat);
                intent.putExtra("destLong", destLong);
                intent.putExtra("type", type);
                intent.putExtra("key", keyForRequest);
                intent.putExtra("classID", "confirmRequest");

                startActivity(intent);
            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.requestConfirmMap);
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
    }


    public void showLocation(LatLng latLng,String comment)
    {

        mMap.addMarker(new MarkerOptions().position(latLng).title(comment));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
//        if(comment.equalsIgnoreCase("Car"))
//        {
//            mMap.addMarker(new MarkerOptions().position(latLng).title("Car Location")
//                    // below line is use to add custom marker on our map.
//                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.car)));                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 12.0f));
//        }
//
//        else {
//            mMap.addMarker(new MarkerOptions().position(latLng).title(comment));
//        }

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


        LatLng source = new LatLng(sourceLat, sourceLong);
        LatLng dest = new LatLng(destLat, destLong);

        estimatedFareTextView.setText("Estimated Fare "+String.valueOf(estimatedFare) + " TK\n" + "Estimated duration " + duration +" Mins");

        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(intermediate));

        showLocation(source,"source");
        showLocation(dest,"destination");
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



    public void sendRequest(String email, double sourceLat, double sourceLong, double destLat, double destLong, String source, String dest, double fare, boolean pending)
    {
        try
        {
            keyForRequest = String.valueOf(System.currentTimeMillis());

            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request/" + keyForRequest + ".json";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userEmail", email);
            jsonBody.put("sourceLat", sourceLat);
            jsonBody.put("sourceLong", sourceLong);
            jsonBody.put("destLat", destLat);
            jsonBody.put("destLong", destLong);
            jsonBody.put("source", source);
            jsonBody.put("dest", dest);
            jsonBody.put("pending", pending);
            jsonBody.put("type", type);
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
    }

    synchronized public void GetIntermediateLocations()
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
}