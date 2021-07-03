package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

public class DriverCollectCashActivity extends AppCompatActivity
{
    double sourceLat, sourceLong;

    double destLat,destLong;

    double driverLat,driverLong;

    String type;

    String source, dest;

    boolean flag;

    double fare, driver_rating_user;

    String driverName, userName, userEmail;

    String startTime, finishTime;

    Context context;
    RequestQueue requestQueue;
    ReentrantLock lock;
    private String keyForRequest;

    Button collectCashButton;
    TextView driverFareShow;
    RatingBar driverRatingBar;
    Button submitButton;
    TextView driverRatingTextView;
    String keyForDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();

        flag = false;

        source = intent.getStringExtra("source");
        dest = intent.getStringExtra("dest");

        sourceLat = intent.getDoubleExtra("sourceLat", 1);
        sourceLong = intent.getDoubleExtra("sourceLong", 1);
        destLat = intent.getDoubleExtra("destLat", 1);
        destLong = intent.getDoubleExtra("destLong", 1);
        driverLat = intent.getDoubleExtra("driverLat", 1);
        driverLong = intent.getDoubleExtra("driverLong", 1);
        type = intent.getStringExtra("type");
        fare = intent.getDoubleExtra("fare", 1);
        keyForRequest = intent.getStringExtra("key");
        keyForDriverID = intent.getStringExtra("key2");
        driverName = intent.getStringExtra("driverName");
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        startTime = intent.getStringExtra("startTime");
        finishTime = intent.getStringExtra("finishTime");

        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        lock = new ReentrantLock();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_collect_cash);

        driverFareShow = findViewById(R.id.driverFareShowTextView);
        collectCashButton = findViewById(R.id.collectCashButton);
        driverRatingBar = findViewById(R.id.driverRatingBar);
        submitButton = (Button) findViewById(R.id.continueButtonToHome);
        driverRatingTextView = findViewById(R.id.driverRatingTextView);

        driverRatingBar.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        driverRatingTextView.setVisibility(View.GONE);

        driver_rating_user = 5.0;


        driverFareShow.setText("YOU ARE TO BE PAID TK " + fare);

        collectCashButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                updateRequestStatus();

                updateDriverLocation();

                addHistory();

                driverRatingBar.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                driverRatingTextView.setVisibility(View.VISIBLE);
            }
        });


        driverRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                System.out.println("rating " + rating);
                driver_rating_user = rating;
                driverRatingTextView.setText("Rate Your Passenger : " + rating);
            }

        });


        // perform click event on button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent intent1 = new Intent(getApplicationContext(), DriverInitialSetLocationActivity.class);
                startActivity(intent1);
            }
        });
    }


     public void updateRequestStatus()
    {
        //lock.lock();
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request/" + keyForRequest + ".json";
            JSONObject jsonBody = new JSONObject();


            jsonBody.put("destLat", destLat);
            jsonBody.put("destLong", destLong);
            jsonBody.put("sourceLat", sourceLat);
            jsonBody.put("sourceLong", sourceLong);
            jsonBody.put("userEmail", userEmail);
            jsonBody.put("pending", false);
            jsonBody.put("accepted_by", Info.driverID);
            jsonBody.put("dest", dest);
            jsonBody.put("source", source);
            jsonBody.put("type", type);
            jsonBody.put("started", true);
            jsonBody.put("finished", true);
            jsonBody.put("done", true);
            jsonBody.put("fare", fare);
            jsonBody.put("type", type);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);

                    flag = true;
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

        //lock.unlock();
    }


     public void addHistory()
    {
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/History.json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("driverID", Info.driverID);
            jsonBody.put("driverName", driverName);
            jsonBody.put("userEmail", userEmail);
            jsonBody.put("userName", userName);
            jsonBody.put("source", source);
            jsonBody.put("dest", dest);
            jsonBody.put("fare", fare);
            jsonBody.put("startTime", startTime);
            jsonBody.put("finishTime", finishTime);
            jsonBody.put("driver_rating_user", driver_rating_user);



            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
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


     public void updateDriverLocation()
    {
        //lock.lock();
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Driver/" + keyForDriverID + ".json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("lat", driverLat);
            jsonBody.put("long", driverLong);
            jsonBody.put("driverID", Info.driverID);
            jsonBody.put("type", type);
            jsonBody.put("busy", false);
            jsonBody.put("name", driverName);

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

        //lock.unlock();
    }
}