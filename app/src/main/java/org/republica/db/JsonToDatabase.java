package org.republica.db;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.republica.api.RepublicaUrls;
import org.republica.model.FossasiaEvent;
import org.republica.model.Speaker;
import org.republica.utils.StringUtils;
import org.republica.utils.VolleySingleton;

import java.util.ArrayList;

/**
 * Created by Abhishek on 17/02/15.
 */
public class JsonToDatabase {

    private final static String TAG = "JSON_TO_DATABASE";

    private Context context;
    private boolean tracks;
    private ArrayList<String> queries;
    private JsonToDatabaseCallback mCallback;
    private int count;

    public JsonToDatabase(Context context) {
        count = 0;
        this.context = context;
        queries = new ArrayList<String>();
        tracks = false;


    }

    public void setOnJsonToDatabaseCallback(JsonToDatabaseCallback callback) {
        this.mCallback = callback;
    }


    public void startDataDownload() {
        parseEvents(RepublicaUrls.SESSIONS_URL);

    }

    public void parseEvents(final String url) {
        RequestQueue queue = VolleySingleton.getReqQueue(context);

        //Request string reponse from the url

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                ArrayList<String> queries = new ArrayList<>();

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
                    JSONObject jObjMain = new JSONObject(response);
                    JSONArray jsonArray = jObjMain.getJSONArray("sessions");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            id = i;
                            title = jsonObject.getString("title");
                            abstractText = jsonObject.getString("abstract");
                            subTitle = jsonObject.getString("type");

                            JSONObject dayObject = jsonObject.getJSONObject("day");
                            date = dayObject.getString("label_en");
                            startTime = "1:00 AM";
                            description = jsonObject.getString("description");
                            JSONObject venueObject = jsonObject.getJSONObject("location");
                            venue = venueObject.getString("label_en");
                            track = jsonObject.getJSONObject("track").getString("label_en");
                            moderator = "";
                            FossasiaEvent event = new FossasiaEvent(i, title, subTitle, date, "", startTime, abstractText, description, venue, track, moderator);

                            String query = "INSERT INTO %s VALUES ('%s', '%s', '%s');";

                            JSONArray jArray = jsonObject.getJSONArray("speakers");
                            for (int j = 0; j < jArray.length(); j++) {
                                JSONObject jObj = jArray.getJSONObject(j);
                                String fullName = jObj.getString("name");
                                String speakerId = jObj.getString("id");
                                String speakerQuery = String.format(query, DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, fullName, speakerId, StringUtils.replaceUnicode(title));
                                queries.add(speakerQuery);

                            }

                            queries.add(event.generateSqlQuery());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    jsonArray = jObjMain.getJSONArray("speakers");

                    String name;
                    String information;
                    String linkedInUrl;
                    String twitterHandle;
                    String designation;
                    String profilePicUrl;
                    String organization;
                    boolean isKeySpeaker;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            id = i;
                            name = jsonObject.getString("name");
                            information = jsonObject.getString("biography");
                            designation = jsonObject.getString("position");
                            organization = jsonObject.getString("organization");

                            if (organization != null && !organization.equals("")) {
                                designation += ", " + organization;
                            }
                            profilePicUrl = jsonObject.getString("photo");
                            Speaker speaker = new Speaker(i, name, information, "", "", designation, profilePicUrl, 0);
                            queries.add(speaker.generateSqlQuery());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    jsonArray = jObjMain.getJSONArray("tracks");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String trackName = jsonArray.getJSONObject(i).getString("label_en");

                        String query = "INSERT INTO %s VALUES (%d, '%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_TRACK, i, StringUtils.replaceUnicode(trackName), "");
                        queries.add(query);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                DatabaseManager dbManager = DatabaseManager.getInstance();
                //Temporary clearing database for testing only
                dbManager.clearDatabase();
                dbManager.performInsertQueries(queries);

                if(mCallback != null) {
                    mCallback.onDataLoaded();
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

    public static interface JsonToDatabaseCallback {
        public void onDataLoaded();
    }


}
