package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ChooseVehicleActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton busChooseButton,bikeChooseButton,carChooseButton;
    NavigationView naview;
    TextView username,rating;
    List<String > name;
    private RequestQueue requestQueue,requestQueue2;
    private Context context;
    ReentrantLock lock;

    Double sourceLat;
    Double sourceLong;
    Double destLat;
    Double destLong;

    boolean alreadyPending;
    boolean checked;
    private String type;
    private String keyForRequest;

    double totalrating,totalratingcount;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_vehicle_choose);
        busChooseButton=findViewById(R.id.busChooseButton);
        bikeChooseButton=findViewById(R.id.bikeChooseButton);
        carChooseButton=findViewById(R.id.carChooseButton);



        lock = new ReentrantLock();

        naview = findViewById(R.id.nav_view);
        View headerView = naview.getHeaderView(0);
        username = headerView.findViewById(R.id.nav_header_title);
        rating = headerView.findViewById(R.id.nav_header_subtitle);

        alreadyPending = false;
        checked = false;


        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);
        requestQueue2=Volley.newRequestQueue(context);

        name = new ArrayList<>();

        setAverageRating();

        naview.setEnabled(true);

        GetMethodForName();


        //username.setText(name.get(0));

        busChooseButton.setOnClickListener(this);
        carChooseButton.setOnClickListener(this);
        bikeChooseButton.setOnClickListener(this);

        checkIfAlreadyPending();

        naview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.nav_history:
                        Intent intent=new Intent(getApplicationContext(),HistoryShowingActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_contact_us:

                        MakeCall(false);

                        break;

                    case R.id.nav_emergency_call:
                        MakeCall(true);

                        break;

                    case R.id.nav_promo_code:
                        Intent intent2=new Intent(getApplicationContext(),PromoCodeActivity.class);
                        startActivity(intent2);

                    case R.id.nav_logout:
                        break;

                }
                return true;
            }
        });

    }

    private void MakeCall(Boolean thana) {
        if(thana==false)
        {
            String phone="+8801550072160";
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        }
        else
        {
            String phone="999";
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);


        }


    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId())
        {
            case R.id.busChooseButton:

                if(checked )
                {
                    if(alreadyPending)
                        carBikeButtonFunction();

                    else
                    {
                        intent=new Intent(getApplicationContext(),BusSeatSelection.class);
                        startActivity(intent);
                    }

                }

                break;

            case R.id.bikeChooseButton:

                if(checked && !alreadyPending)
                    type = "bike";

                carBikeButtonFunction();

                break;

            case R.id.carChooseButton:

                if(checked && !alreadyPending)
                    type = "car";

                carBikeButtonFunction();

                break;
        }
    }

    synchronized void carBikeButtonFunction()
    {
        lock.lock();

        Intent intent = null;

        if(!checked)
            return;

        if(!alreadyPending)
        {
            intent=new Intent(getApplicationContext(),CarBikeSearchActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }

        else
        {
            intent=new Intent(getApplicationContext(),WaitingActivity.class);

            intent.putExtra("sourceLat", sourceLat);
            intent.putExtra("sourceLong", sourceLong);
            intent.putExtra("destLat", destLat);
            intent.putExtra("destLong", destLong);
            intent.putExtra("type", type);
            intent.putExtra("key", keyForRequest);
            intent.putExtra("classID", "chooseVehicle");

            startActivity(intent);
        }

        lock.unlock();
    }

    synchronized public void checkIfAlreadyPending()
    {
        lock.lock();

        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json";

        CustomPriorityRequest jsonObjectRequest = new CustomPriorityRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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

                            boolean pending = jsonObject.getBoolean("pending");

                            if(pending)
                            {
                                String user = jsonObject.getString("userEmail");

                                if(Info.currentEmail.equalsIgnoreCase(user))
                                {
                                    sourceLat = jsonObject.getDouble("sourceLat");
                                    sourceLong = jsonObject.getDouble("sourceLong");
                                    destLat = jsonObject.getDouble("destLat");
                                    destLong = jsonObject.getDouble("destLong");
                                    type = jsonObject.getString("type");
                                    keyForRequest = key;

                                    alreadyPending = true;
                                    break;
                                }
                            }

                            else
                            {
                                boolean done = jsonObject.getBoolean("done");

                                if(!done)
                                {
                                    String user = jsonObject.getString("userEmail");

                                    if(Info.currentEmail.equalsIgnoreCase(user))
                                    {
                                        sourceLat = jsonObject.getDouble("sourceLat");
                                        sourceLong = jsonObject.getDouble("sourceLong");
                                        destLat = jsonObject.getDouble("destLat");
                                        destLong = jsonObject.getDouble("destLong");
                                        type = jsonObject.getString("type");
                                        keyForRequest = key;

                                        alreadyPending = true;
                                        break;
                                    }
                                }
                            }





                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    checked = true;

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("error: " , error.getMessage());
            }
        });

        jsonObjectRequest.setPriority(Request.Priority.HIGH);
        requestQueue.add(jsonObjectRequest);


        lock.unlock();
    }

    public void setAverageRating()
    {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/History.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {

                {
                    JSONArray array = response.names();


                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            String userEMail = response.getJSONObject(key).getString("userEmail");

                            Log.i("cmail",Info.currentEmail);

                            if(userEMail.equalsIgnoreCase(Info.currentEmail))
                            {
                                    totalrating+=response.getJSONObject(key).getDouble("driver_rating_user");
                                    Log.i("tot",totalrating+"");
                                    totalratingcount++;
                                    Log.i("totc",totalratingcount+"");
                                    double avg=totalrating/totalratingcount;
                                    avg=Math.round(avg*100.0)/100.0;
                                    rating.setText(avg+"*");
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


        requestQueue2.add(jsonObjectRequest);

    }

    public void GetMethodForName()
    {
        lock.lock();

        String url = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/users.json";
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

                            String email = response.getJSONObject(key).getString("email");
                            if(email.equalsIgnoreCase(Info.currentEmail))
                            {
                                String n = response.getJSONObject(key).getString("name");
                                //username.setText(n);
                                Log.i("name", n);
                                username.setText(n);
                               // setAverageRating();
                               // rating.setText(""+(totalrating/totalratingcount)+"*");

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
}