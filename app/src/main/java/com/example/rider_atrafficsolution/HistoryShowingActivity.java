package com.example.rider_atrafficsolution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryShowingActivity extends AppCompatActivity {

    ListView historyListview;
    RequestQueue requestQueue;
    Context context;

    ArrayList<String> historyArrayList;

    ArrayAdapter<String> arrayAdapter;

    String source,dest,driverName,type;

    double userRating,driverRating;

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
                           //     driverRating=4;



                                String history="Journey From "+source+" to "+dest+ "\n"+
                                        "Fare:"+fare+" TK\n"+
                                        "Driver "+driverName+" rated you "+userRating+"\n"+
                                        "You rated "+driverName+" "+driverRating;

                                Log.i("history",history);



                                historyArrayList.add(history);

                                arrayAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,historyArrayList);
                                historyListview.setAdapter(arrayAdapter);



//
//                                History history=new History();
//                                history.setSource(source);
//                                history.setDest(dest);
//                                history.setDriverName(dest);



                                //break;
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_showing);

        historyListview=findViewById(R.id.histroy_listview);
        historyArrayList=new ArrayList<>();
     //   historyArrayList.add("bal");
        context = getBaseContext();

        requestQueue = Volley.newRequestQueue(context);

        GetHistory();


        arrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,historyArrayList);
        historyListview.setAdapter(arrayAdapter);






    }
}