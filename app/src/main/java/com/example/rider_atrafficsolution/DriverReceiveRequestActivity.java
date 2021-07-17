package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DriverReceiveRequestActivity extends AppCompatActivity
{
    ListView requestsListView;
    List<String> requests;
    Context context;
    RequestQueue requestQueue;
    ArrayAdapter<String > adapter;
    ReentrantLock lock;
    List<LatLng> sourceList;
    List<LatLng> destList;
    List<String > keys;
    List<Double> fares;

    boolean busy;
    boolean checked = false;
    boolean checked1 = false;

    double accepted_sourceLat;
    double accepted_sourceLong;
    double accepted_destLat;
    double accepted_destLong;
    String accepted_key;
    int code;

    Handler handler;
    Runnable r;


    List<Double> latlong;
    //List<String> type;
    String type;
    private double fare;
    private ArrayList<Integer> codes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_receive_request);

        requestsListView = findViewById(R.id.requestsListView);

        requests = new ArrayList<>();

        context = getBaseContext();

        lock = new ReentrantLock();

        requestQueue = Volley.newRequestQueue(context);

        latlong = new ArrayList<>();

        sourceList = new ArrayList<>();
        destList = new ArrayList<>();
        keys = new ArrayList<>();
        fares = new ArrayList<>();
        codes = new ArrayList<>();

        update();

        busy = false;


        handler =new Handler();

        r = new Runnable()
        {
            public void run()
            {
                handler.postDelayed(this, 5000);
                Log.i("driver request timer", "updated after 5 seconds");
                update();

            }
        };
        handler.postDelayed(r, 0000);

        adapter = new ArrayAdapter< >(this, android.R.layout.simple_list_item_1, requests);
        requestsListView.setAdapter(adapter);

        for(int i=0;i<requestsListView.getChildCount();i++)
        {
            requestsListView.getChildAt(i).setBackgroundColor(0);
        }

        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(requests.contains("No pending requests right now"))
                    return;

                Intent intent = new Intent(getApplicationContext(), DriverLocationUpdate.class);

                intent.putExtra("sourceLat", sourceList.get(position).latitude);
                intent.putExtra("sourceLong", sourceList.get(position).longitude);
                intent.putExtra("destLat", destList.get(position).latitude);
                intent.putExtra("destLong", destList.get(position).longitude);
                intent.putExtra("driverLat", latlong.get(0));
                intent.putExtra("driverLong", latlong.get(1));
                intent.putExtra("type", type);
                intent.putExtra("key", keys.get(position));
                intent.putExtra("uniqueCode", codes.get(position));
                intent.putExtra("driverID", Info.driverID);
                intent.putExtra("fare", fares.get(position));
                Log.i("info.id = ", Info.driverID);

                intent.putExtra("classid","driver");

                handler.removeCallbacks(r);

                startActivity(intent);
            }
        });

    }

    public void update()
    {
        GetDriverLocation();

        if(!latlong.isEmpty())
            GetRequests();

        if(!checked)
            return;

        if(busy)
        {
            Log.i("busy", "busy");
            GetAlreadyAcceptedRequest();
        }

        if(busy && !checked1)
            return;

        if(busy && checked1)
        {
            Intent intent = new Intent(getApplicationContext(), DriverLocationUpdate.class);

            intent.putExtra("sourceLat", accepted_sourceLat);
            intent.putExtra("sourceLong", accepted_sourceLong);
            intent.putExtra("destLat", accepted_destLat);
            intent.putExtra("destLong", accepted_destLong);
            intent.putExtra("driverLat", latlong.get(0));
            intent.putExtra("driverLong", latlong.get(1));
            intent.putExtra("type", type);
            intent.putExtra("key", accepted_key);
            intent.putExtra("fare", fare);
            intent.putExtra("uniqueCode", code);
            intent.putExtra("driverID", Info.driverID);
            Log.i("info.id = ", Info.driverID);

            intent.putExtra("classid","driver2");

            handler.removeCallbacks(r);

            startActivity(intent);
        }

        if(requests.isEmpty())
            requests.add("No pending requests right now");

        adapter = new ArrayAdapter< String>(context, android.R.layout.simple_list_item_1, requests);
        requestsListView.setAdapter(adapter);
    }


    synchronized public void GetAlreadyAcceptedRequest()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {

                //try
                {
                    JSONArray array = response.names();
                    Util util = new Util();

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            String acceptor = jsonObject.getString("accepted_by");

                            if(acceptor.equalsIgnoreCase(Info.driverID))
                            {
                                accepted_sourceLat = jsonObject.getDouble("sourceLat");
                                accepted_sourceLong = jsonObject.getDouble("sourceLong");
                                accepted_destLat = jsonObject.getDouble("destLat");
                                accepted_destLong = jsonObject.getDouble("destLong");
                                accepted_key = key;
                                fare = jsonObject.getDouble("fare");
                                code = jsonObject.getInt("uniqueCode");

                                break;
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(requests.isEmpty())
                    {
                        requests.add("No pending requests right now");
                    }


                    checked1 = true;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("error: " , error.getMessage());
            }
        });

        //jsonObjectRequest.setPriority(Request.Priority.HIGH);
        requestQueue.add(jsonObjectRequest);

        lock.unlock();
    }

    synchronized public void GetDriverLocation()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Driver.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                latlong.clear();

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

                            String id = jsonObject.getString("driverID");
                            busy = jsonObject.getBoolean("busy");



                            if(id.equalsIgnoreCase(Info.driverID))
                            {
                                latlong.add(jsonObject.getDouble("lat"));
                                latlong.add(jsonObject.getDouble("long"));
                                //type.add(jsonObject.getString("type"));
                                type = jsonObject.getString("type");
                                //type.add(t);
                                break;
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

        //jsonObjectRequest.setPriority(Request.Priority.IMMEDIATE);
        requestQueue.add(jsonObjectRequest);

        lock.unlock();
    }

    synchronized public void GetRequests()
    {
        lock.lock();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/Request.json", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                requests.clear();
                sourceList.clear();;
                destList.clear();
                keys.clear();
                codes.clear();

                //try
                {
                    JSONArray array = response.names();
                    Util util = new Util();

                    for(int i=0;i<array.length();i++)
                    {
                        try
                        {
                            String key = array.getString(i);

                            JSONObject jsonObject = response.getJSONObject(key);

                            boolean pending = jsonObject.getBoolean("pending");
                            if(!pending)
                                continue;

                            String vehicleType = jsonObject.getString("type");

                            //if(!vehicleType.equalsIgnoreCase(type.get(0)))
                            if(!vehicleType.equalsIgnoreCase(type))
                                continue;

                            double sourceLat = jsonObject.getDouble("sourceLat");
                            double sourceLong = jsonObject.getDouble("sourceLong");

                            double destLat = jsonObject.getDouble("destLat");
                            double destLong = jsonObject.getDouble("destLong");

//                            double destLat = jsonObject.getDouble("destLat");
//                            double destLong = jsonObject.getDouble("destLong");


//                            while (true)
//                            {
//                                lock.lock();
//
//                                if(!latlong.isEmpty())
//                                    break;
//
//                                lock.unlock();
//                            }

                            double dist = util.getDistanceFromLatLonInKm(sourceLat, sourceLong, latlong.get(0), latlong.get(1));

                            Log.i("distance ", String.valueOf(dist));

                            if(dist < 3.0)
                            {
                                String source = jsonObject.getString("source");
                                String dest = jsonObject.getString("dest");
                                double fare = jsonObject.getDouble("fare");
                                int uniqueCode = jsonObject.getInt("uniqueCode");

                                requests.add("From: " + source + "\nTo: " + dest);
                                sourceList.add(new LatLng(sourceLat, sourceLong));
                                destList.add(new LatLng(destLat, destLong));
                                keys.add(key);
                                fares.add(fare);
                                codes.add(uniqueCode);

                                //Log.i("request", requests.get(0));
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(requests.isEmpty())
                    {
                        requests.add("No pending requests right now");
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

        //jsonObjectRequest.setPriority(Request.Priority.HIGH);
        requestQueue.add(jsonObjectRequest);

        lock.unlock();
    }

    public void GetDistance(double lat1, double lon1, double lat2, double lon2)
    {
        lock.lock();

        String API_Key = "AIzaSyC-9ghJoVuhdfodTVZ3JnpDbgx38-0PtGk";

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+lat1+","+lon1+"&destinations="+lat2+","+lon2+"&key="  + API_Key;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    JSONArray dist = (JSONArray) response.get("rows");
                    JSONObject obj2 = (JSONObject) dist.get(0);
                    JSONArray disting = (JSONArray) obj2.get("elements");
                    JSONObject obj3 = (JSONObject) disting.get(0);
                    JSONObject obj4 =(JSONObject) obj3.get("distance");
                    JSONObject obj5 = (JSONObject) obj3.get("duration");
                    Log.i("distance", obj4.get("text").toString());
                    System.out.println(obj4.get("text"));
                    System.out.println(obj5.get("text"));
                    System.out.println(obj4.get("text"));
                    System.out.println(obj5.get("text"));
                    //Log.i("dist", String.valueOf(dist));

                }catch (Exception e)
                {
                    e.printStackTrace();
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