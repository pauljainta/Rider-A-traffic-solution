package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.concurrent.locks.ReentrantLock;

public class UserSideJourneyCompleteActivity extends AppCompatActivity
{
    TextView fareShowTextView;
    RatingBar userRatingBar;
    TextView userRatingTextView;
    Button userSideContinueButton;

    double user_rating_driver;
    double driver_rating_user;

    private ReentrantLock lock;
    private RequestQueue requestQueue;

    boolean checkedHistory;

    String email;
    String source;
    String dest;
    String driverID;
    String driverName;
    String finishTime;
    String startTime;
    String userName;

    double fare;

    String key;
    private Timestamp t_latest;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_side_journey_complete);

        checkedHistory = false;

        fareShowTextView = findViewById(R.id.userFareShowTextView);
        userRatingBar = findViewById(R.id.userRatingBar);
        userRatingTextView = findViewById(R.id.userRatingTextView);
        userSideContinueButton = findViewById(R.id.userSideContinueButtonToHome);

        user_rating_driver = 5;

        Context context = getBaseContext();
        requestQueue = Volley.newRequestQueue(context);
        lock = new ReentrantLock();

        fare = getIntent().getDoubleExtra("fare", 1);
        email = getIntent().getStringExtra("email");

        fareShowTextView.setText("Your Have Paid TK " + fare);

        GetHistoryInfo();

        userRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                user_rating_driver = rating;
                userRatingTextView.setText("Rate Your Driver : " + rating);
            }
        });

        userSideContinueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!checkedHistory)
                    return;

                updateHistory();

                Intent intent = new Intent(getApplicationContext(), ChooseVehicleActivity.class);
                startActivity(intent);
            }
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(checkedHistory)
                {
                    System.out.println(key);
                    return;
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 0);

    }



    synchronized void GetHistoryInfo()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/History.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                //try
                {
                    JSONArray array = response.names();

                    t_latest = Timestamp.valueOf("2010-01-01 10:10:10");

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String k = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(k);

                            String userMail = jsonObject.getString("userEmail");

                            if(email.equalsIgnoreCase(userMail))
                            {
                                String finish = jsonObject.getString("finishTime");

                                Timestamp ts = Timestamp.valueOf(finish);

                                if(ts.compareTo(t_latest) > 0)
                                {
                                    t_latest = ts;
                                    key = k;

                                    driverID = jsonObject.getString("driverID");
                                    driverName = jsonObject.getString("driverName");
                                    userName = jsonObject.getString("userName");
                                    driver_rating_user = jsonObject.getDouble("driver_rating_user");
                                    source = jsonObject.getString("source");
                                    dest = jsonObject.getString("dest");
                                    startTime = jsonObject.getString("startTime");
                                    finishTime = finish;
                                }

                                System.out.println(ts);
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        catch (IllegalArgumentException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    checkedHistory = true;

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

    synchronized void updateHistory()
    {
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/History/" + key + ".json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("driverID", driverID);
            jsonBody.put("driverName", driverName);
            jsonBody.put("userEmail", email);
            jsonBody.put("userName", userName);
            jsonBody.put("source", source);
            jsonBody.put("dest", dest);
            jsonBody.put("fare", fare);
            jsonBody.put("startTime", startTime);
            jsonBody.put("finishTime", finishTime);
            jsonBody.put("driver_rating_user", driver_rating_user);
            jsonBody.put("user_rating_driver", user_rating_driver);

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


}