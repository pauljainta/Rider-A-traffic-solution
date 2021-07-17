package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.HashSet;

public class PromoCodeActivity extends AppCompatActivity
{
    Button applypromocodeButton;
    EditText applypromocodeEditText;
    private RequestQueue requestQueue;

    boolean alreadyApplied;
    private boolean checked;
    private String inputCode;
    private String key;

    String email;
    String validity;
    String promo;

    int count;
    int max_amount;
    int percentage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_code);

        checked = false;
        alreadyApplied = false;

        requestQueue = Volley.newRequestQueue(getBaseContext());

        GetDiscountInfo();


        applypromocodeButton=findViewById(R.id.promocodeButton);
        applypromocodeEditText=findViewById(R.id.promocodeEditText);

        applypromocodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!checked)
                    return;

                if(alreadyApplied)
                    return;

                inputCode = applypromocodeEditText.getText().toString();

                checkPromoCode();

                Handler handler = new Handler();
                Runnable runnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(key != null)
                        {
                            applyDiscount();
                            return;
                        }
                        handler.postDelayed(this, 2000);
                    }
                };
                handler.postDelayed(runnable, 0);

            }
        });

    }

    public void applyDiscount()
    {
        try
        {
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Discount/" + key + ".json";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("userEmail", email);
            jsonBody.put("promoCode", promo);
            jsonBody.put("validity", validity);
            jsonBody.put("count", count);
            jsonBody.put("max_amount", max_amount);
            jsonBody.put("percentage", percentage);
            jsonBody.put("applied", true);


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


    public void checkPromoCode()
    {
        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Discount.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                            String k = array.getString(i);

                            promo = response.getJSONObject(k).getString("promoCode");
                            email = response.getJSONObject(k).getString("userEmail");

                            if(email.equalsIgnoreCase(Info.currentEmail) && inputCode.equalsIgnoreCase(promo))
                            {
                                key = k;
                                validity = response.getJSONObject(k).getString("validity");
                                count = response.getJSONObject(k).getInt("count");
                                max_amount = response.getJSONObject(k).getInt("max_amount");
                                percentage = response.getJSONObject(k).getInt("percentage");

                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                checked = true;
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

    public void GetDiscountInfo()
    {
        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Discount.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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

                            String email = response.getJSONObject(key).getString("userEmail");
                            String validity = response.getJSONObject(key).getString("validity");
                            Timestamp ts = Timestamp.valueOf(validity);
                            boolean applied = response.getJSONObject(key).getBoolean("applied");
                            int count = response.getJSONObject(key).getInt("count");

                            if(email.equalsIgnoreCase(Info.currentEmail) && ts.compareTo(new Timestamp(System.currentTimeMillis()))>0 && applied && count>0)
                            {
                                alreadyApplied = true;

                                Toast.makeText(PromoCodeActivity.this, "Already promo applied", Toast.LENGTH_SHORT).show();

                                break;
                            }

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                checked = true;
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
}