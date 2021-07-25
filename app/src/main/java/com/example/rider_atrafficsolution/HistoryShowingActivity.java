package com.example.rider_atrafficsolution;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class HistoryShowingActivity extends AppCompatActivity {

    ListView historyListview;
    RequestQueue requestQueue;
    Context context;

    ArrayList<String> historyArrayList;

    ArrayList<History> histories;

    ArrayAdapter<String> arrayAdapter;

    String source,dest,driverName,type;

    double userRating,driverRating;
    String startTime,finishTime;

    double fare;


    public void GetHistory()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/History.json", null, new Response.Listener<JSONObject>() {
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

                            String userEMail = response.getJSONObject(key).getString("userEmail");

                            Log.i("cmail",Info.currentEmail);

                            if(userEMail.equalsIgnoreCase(Info.currentEmail))
                            {
                                //minDistLat = response.getJSONObject(key).getDouble("lat");
                               // minDistLong = response.getJSONObject(key).getDouble("long");

                                source=response.getJSONObject(key).getString("source");
                                dest=response.getJSONObject(key).getString("dest");
                                driverName=response.getJSONObject(key).getString("driverName");

                              //  type=response.getJSONObject(key).getString("type");

                                fare=response.getJSONObject(key).getDouble("fare");

                                userRating=response.getJSONObject(key).getDouble("driver_rating_user");

                                driverRating=response.getJSONObject(key).getDouble("user_rating_driver");
                                startTime=response.getJSONObject(key).getString("startTime");
                                finishTime=response.getJSONObject(key).getString("finishTime");

                                type=response.getJSONObject(key).getString("type");
                                Log.i("starttime",startTime+"bal");

                                History history=new History();
                                history.setSource(source);
                                history.setDest(dest);
                                history.setDriverName(driverName);
                                history.setFare(fare);
                                history.setUserRating(userRating);
                                history.setDriverRating(driverRating);
                                history.setType(type);

                                if(startTime!=null && finishTime!=null)
                                {
                                    history.setStartTime(Timestamp.valueOf(startTime));
                                    history.setFinishTime(Timestamp.valueOf(finishTime));
                                }


                                histories.add(history);

                                histories.sort(new TimestampSorter());


//                                String history2=startTime+" "+finishTime+" Journey From "+source+" to "+dest+ "\n"+
//                                        "Fare:"+fare+" TK\n"+
//                                        "Driver "+driverName+" rated you "+userRating+"\n"+
//                                        "You rated "+driverName+" "+driverRating;

//                                Log.i("history",history.toString());
//
//
                                historyArrayList.clear();


                                for(History h:histories)
                                {
                                    Log.i("h",h.toString());
                                    historyArrayList.add(h.toString());

                                }


                                //historyArrayList.add(history.toString());

                                arrayAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,historyArrayList);
                                historyListview.setAdapter(arrayAdapter);



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

       // histories.sort(new TimestampSorter());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_showing);

        histories=new ArrayList<History>();

        historyListview=findViewById(R.id.histroy_listview);
        historyArrayList=new ArrayList<>();
     //   historyArrayList.add("bal");
        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

//        arrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,historyArrayList);
//        historyListview.setAdapter(arrayAdapter);

        GetHistory();

       //AddHistory();



        arrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,historyArrayList);
        historyListview.setAdapter(arrayAdapter);


        historyListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),TripDetailsActivity.class);
                History history=histories.get(position);
                intent.putExtra("source",history.getSource());
                intent.putExtra("dest",history.getDest());
                intent.putExtra("fare",history.getFare());
                intent.putExtra("driverName",history.getDriverName());
                intent.putExtra("passengerName",history.getPassengerName());
                intent.putExtra("startTime",history.getStartTime().toString());
                intent.putExtra("finishTime",history.getFinishTime().toString());
                intent.putExtra("userRating",history.getUserRating());
                intent.putExtra("driverRating",history.getDriverRating());
                intent.putExtra("type",history.getType());

                startActivity(intent);



            }
        });








    }

//    private void AddHistory() {
//
//      //  histories.sort(new TimestampSorter());
//
//        for(History history:histories)
//        {
//            Log.i("h",history.toString());
//            arrayAdapter.add(history.toString());
////            historyArrayList.add(history.toString());
//
//        }
////        arrayAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,historyArrayList);
////        historyListview.setAdapter(arrayAdapter);
//    }
}