package org.republica.db;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.republica.model.FossasiaEvent;
import org.republica.model.Sponsor;
import org.republica.utils.VolleySingleton;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by Abhishek on 03-05-2015.
 */
public class RepublicParser {
    
    private Context context;

    public RepublicParser(Context context) {
        this.context = context;
    }

    public void parseEvents(final String url) {
        RequestQueue queue = VolleySingleton.getReqQueue(context);

        //Request string reponse from the url

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                long id;
                String title;
                String subTitle;
                ArrayList<String> keyNoteList;
                //TODO: Do validation on date and convert to date object;
                String date;
                //TODO: Get day from Date object
                String day;
                //TODO: Convert time to date object, will be later used to save as reminder
                String startTime;
                String abstractText;
                String description;
                String venue;
                String track;
                String moderator;
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0; i<jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            id = i;
                            title = jsonObject.getString("title");
                            abstractText = jsonObject.getString("abstract");
                            subTitle = jsonObject.getString("type");
                            String tempDate = jsonObject.getString("begin");

                            JSONObject dayObject = jsonObject.getJSONObject("day");
                            date = dayObject.getString("date");
                            startTime = "1:00 AM";
                            description = jsonObject.getString("description");
                            JSONObject venueObject = jsonObject.getJSONObject("location");
                            venue = venueObject.getString("label_en");
                            track = jsonObject.getJSONObject("track").getString("label_en");
                            moderator = "";
                            FossasiaEvent event = new FossasiaEvent(i, title, subTitle, date, "", startTime, abstractText, description, venue, track, moderator);
                            Log.d(this.getClass().getCanonicalName(), event.generateSqlQuery());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }
                , new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //  Log.d(TAG, "VOLLEY ERROR :" + error.getMessage());

            }
        }

        );
        queue.add(stringRequest);
    }
}
