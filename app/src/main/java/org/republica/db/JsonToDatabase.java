package org.republica.db;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.republica.api.FossasiaUrls;
import org.republica.model.FossasiaEvent;
import org.republica.model.Speaker;
import org.republica.model.Sponsor;
import org.republica.model.Venue;
import org.republica.utils.StringUtils;
import org.republica.utils.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        fetchTracks(FossasiaUrls.TRACKS_URL);
        startTrackUrlFetch(FossasiaUrls.VERSION_TRACK_URL);
        SponsorUrl(FossasiaUrls.SPONSOR_URL);

    }

    private void SponsorUrl(final String sponsorUrl) {
        RequestQueue queue = VolleySingleton.getReqQueue(context);

        //Request string reponse from the url

        StringRequest stringRequest = new StringRequest(sponsorUrl, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray jsonArray1 = removePaddingFromString(response);
                String name;
                String img;
                String url;
                Sponsor temp;

                for (int i=0;i<jsonArray1.length();i++){

                    try{
                        name = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(0).getString("v");
                        img = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(1).getString("v");
                        url = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(2).getString("v");

                        temp = new Sponsor((i+1),name,img,url);
                        String ab = temp.generatesql();
                        queries.add(ab);
                        Log.d(TAG,ab);
                    }
                    catch ( JSONException e){

                       // Log.e(TAG, "JSON error: " + e.getMessage() + "\nResponse: " + response);

                    }
                }
            }
        }
                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
              //  Log.d(TAG, "VOLLEY ERROR :" + error.getMessage());

            }
        }

        );
        queue.add(stringRequest);
    }

    private void startTrackUrlFetch(String url) {


        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
                //Log.d(TAG, jsonArray.toString());
                String name;
                String url;
                String venue;
                String address;
                String howToReach;
                String link;
                String room;

                String mapLocation;
                String version;
                String forceTrack;
                Venue temp;


                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        name = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("f");
                        venue = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(2)
                                .getString("v");
                        room = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(3)
                                .getString("v");
                        link = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(4)
                                .getString("v");
                        address = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(6)
                                .getString("v");
                        howToReach = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(7)
                                .getString("v");

                        version = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(8)
                                .getString("v");

                        mapLocation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(5)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES (%d, '%s', '%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_TRACK_VENUE, i, name, venue, mapLocation);
                        queries.add(query);

                        temp = new Venue(name, venue, mapLocation, room, link, address, howToReach);
                        //Generate query
                        queries.add(temp.generateSql());

                       // Log.d(TAG, name);

                        fetchData(FossasiaUrls.PART_URL + url, venue, name, (i + 50) * 100);


                    } catch (JSONException e) {
                      //  Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
                count--;
                checkStatus();

            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                count--;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        count++;

    }


    private void fetchData(String url, final String venue, final String forceTrack, final int id) {

        final RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
               // Log.d(TAG, jsonArray.toString());

                String firstName;
                String lastName;
                String time;
                String date;
                String organization;
                String email;
                String blog;
                String twitter;
                String typeOfProposal;
                String topicName;
                String field;
                String day;
                String proposalAbstract;
                String description;
                String url;
                String fullName;
                String linkedIn;
                String moderator;


                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        firstName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.FIRST_NAME)
                                .getString("v");
                        lastName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LAST_NAME)
                                .getString("v");
                        time = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TIME)
                                .getString("f");
                        date = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DATE)
                                .getString("v");
                        organization = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ORGANIZATION)
                                .getString("v");
                        email = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.EMAIL)
                                .getString("v");
                        blog = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.BLOG)
                                .getString("v");
                        twitter = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TWITTER)
                                .getString("v");
                        typeOfProposal = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TYPE_OF_PROPOSAL)
                                .getString("v");
                        topicName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TOPIC_NAME)
                                .getString("v");
                        field = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TRACK)
                                .getString("v");
                        proposalAbstract = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ABSTRACT)
                                .getString("v");
                        description = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DESCRIPTION)
                                .getString("v");
                        url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.URL)
                                .getString("v");
                        linkedIn = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LINKEDIN)
                                .getString("v");
                        moderator = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.MODERATOR)
                                .getString("v");
                        String logData = "First Name: %s\nLast Name: %s\nDate: %s\nTime: %s\nOrganization: %s\nEmail: %s\nBlog: %s\nTwitter: %s\nType Of Proposal: %s\nTopic Name:%s\nTrack: %s\nAbstarct: %s\nDescription: %s\nURL: %s";
                      //  logData = String.format(logData, firstName, lastName, date, time, organization, email, blog, twitter, typeOfProposal, topicName, field, proposalAbstract, description, url);
//                        Log.d(TAG, logData);
                        int id2 = id + i;
                        if (date.equals("") || firstName.equals("") || time.equals("") || topicName.equals("")) {
                            continue;
                        }
                        String[] dayDate = date.split(" ");
                        day = dayDate[0];
                        date = dayDate[1] + " " + dayDate[2];
                        FossasiaEvent temp = new FossasiaEvent(id2, topicName, field, date, day, time, proposalAbstract, description, venue, forceTrack, moderator);


                        fullName = firstName + " " + lastName;
                        Speaker tempSpeaker = new Speaker(id2, fullName, "", linkedIn, twitter, organization, url, 0);
                        queries.add(tempSpeaker.generateSqlQuery());
                        queries.add(temp.generateSqlQuery());
                        String query = "INSERT INTO %s VALUES ('%s', %d, '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, fullName, id2, StringUtils.replaceUnicode(topicName));
//                        Log.d(TAG, query);
                        queries.add(query);


                    } catch (JSONException e) {
                     //   Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }

                count--;
                checkStatus();

            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                count--;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        count++;

    }


    private void fetchTracks(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
              //  Log.d(TAG, jsonArray.toString());
                String trackName;
                String trackInformation;

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        trackName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        trackInformation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES (%d, '%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_TRACK, i, StringUtils.replaceUnicode(trackName), StringUtils.replaceUnicode(trackInformation));
                       // Log.d(TAG, query);
                        queries.add(query);
                    } catch (JSONException e) {
                      //  Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
                tracks = true;
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                tracks = true;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void fetchSpeakerEventRelation(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
               // Log.d(TAG, jsonArray.toString());
                String speaker;
                String event;

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        speaker = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        event = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES ('%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, speaker, event);
//                        Log.d(TAG, query);
                        queries.add(query);
                    } catch (JSONException e) {
                  //      Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
//                speakerEventRelation = true;
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                speakerEventRelation = true;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void fetchKeySpeakers(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
               // Log.d(TAG, jsonArray.toString());
                String name;
                String designation;
                String profilePicUrl;
                String information;
                String twitterHandle;
                String linkedInUrl;
                int isKeySpeaker;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        name = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        designation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        information = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(2)
                                .getString("v");
                        twitterHandle = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(3)
                                .getString("v");
                        linkedInUrl = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(4)
                                .getString("v");
                        profilePicUrl = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(5)
                                .getString("v");
                        isKeySpeaker = (int) jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(6)
                                .getLong("v");
                        Speaker temp = new Speaker(i + 1, name, information, linkedInUrl, twitterHandle, designation, profilePicUrl, isKeySpeaker);
//                        Log.d(TAG, temp.generateSqlQuery());
                        queries.add(temp.generateSqlQuery());
                    } catch (JSONException e) {
               //         Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse: " + response);
                    }

                }
//                keySpeakerLoaded = true;
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                keySpeakerLoaded = true;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void checkStatus() {
        if (tracks && count == 0) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            //Temporary clearing database for testing only
            dbManager.clearDatabase();
            dbManager.performInsertQueries(queries);

            //Implement callbacks
            if (mCallback != null) {
                mCallback.onDataLoaded();
            }
        }
    }

    private JSONArray removePaddingFromString(String response) {
        response = response.replaceAll("\"v\":null", "\"v\":\"\"");
        response = response.replaceAll("null", "{\"v\": \"\"}");
        response = response.substring(response.indexOf("(") + 1, response.length() - 2);
        try {
            JSONObject jObj = new JSONObject(response);
            jObj = jObj.getJSONObject("table");
            JSONArray jArray = jObj.getJSONArray("rows");
//            Log.d(TAG, jArray.toString());
            return jArray;
        } catch (JSONException e) {
           // Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);

        }

        return null;

    }

    public static interface JsonToDatabaseCallback {
        public void onDataLoaded();
    }


}
