package org.republica.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.republica.db.DatabaseHelper;
import org.republica.utils.StringUtils;

import java.util.ArrayList;


/**
 * Created by Abhishek on 20/02/15.
 */
public class FossasiaEvent implements Parcelable {

    public static final Parcelable.Creator<FossasiaEvent> CREATOR = new Parcelable.Creator<FossasiaEvent>() {
        public FossasiaEvent createFromParcel(Parcel in) {
            return new FossasiaEvent(in);
        }

        public FossasiaEvent[] newArray(int size) {
            return new FossasiaEvent[size];
        }
    };

    private long id;
    private String title;
    private String subTitle;
    private ArrayList<String> keyNoteList;
    //TODO: Do validation on date and convert to date object;
    private String date;
    //TODO: Get day from Date object
    private String day;
    //TODO: Convert time to date object, will be later used to save as reminder
    private String startTime;
    private String abstractText;
    private String description;
    private String venue;
    private String track;
    private String moderator;

    public FossasiaEvent(int id, String title, String subTitle, ArrayList<String> keyNoteList, String date, String day, String startTime, String abstractText, String description, String venue, String track, String moderator) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.keyNoteList = keyNoteList;
        this.date = date;
        this.day = day;
        this.startTime = startTime;
        this.abstractText = abstractText;
        this.description = description;
        this.venue = venue;
        this.track = track;
        this.moderator = moderator;
    }

    public FossasiaEvent(int id, String title, String subTitle, String date, String day, String startTime, String abstractText, String description, String venue, String track, String moderator) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.date = date;
        this.day = day;
        this.startTime = startTime;
        this.abstractText = abstractText;
        this.description = description;
        this.venue = venue;
        this.keyNoteList = new ArrayList<String>();
        this.track = track;
        this.moderator = moderator;

    }

    public FossasiaEvent(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.subTitle = in.readString();
        this.date = in.readString();
        this.day = in.readString();
        this.startTime = in.readString();
        this.abstractText = in.readString();
        this.description = in.readString();
        this.venue = in.readString();
        this.track = in.readString();
        this.moderator = in.readString();
        this.keyNoteList = in.readArrayList(String.class.getClassLoader());

    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public ArrayList<String> getKeyNoteList() {
        return keyNoteList;
    }

    public void setKeyNoteList(ArrayList<String> keyNoteList) {
        this.keyNoteList = keyNoteList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getPersonSummary() {
        // TODO: Make a comma separated list of all the speakers from KeyNoteList
        if (keyNoteList.size() > 0) {
            String speakers = "";
            for (String speaker : keyNoteList) {
                speakers += speaker + ", ";
            }

            return speakers.substring(0, speakers.length() - 2);
        } else {
            return "";
        }

    }

    public String generateSqlQuery() {

        String query = String.format("INSERT INTO %s VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", DatabaseHelper.TABLE_NAME_SCHEDULE, id, StringUtils.replaceUnicode(title), StringUtils.replaceUnicode(subTitle), StringUtils.replaceUnicode(date), StringUtils.replaceUnicode(day), StringUtils.replaceUnicode(startTime), StringUtils.replaceUnicode(abstractText), StringUtils.replaceUnicode(description), StringUtils.replaceUnicode(venue), StringUtils.replaceUnicode(track), StringUtils.replaceUnicode(moderator));

        Log.d("this", query);
        return query;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(subTitle);
        out.writeString(date);
        out.writeString(day);
        out.writeString(startTime);
        out.writeString(abstractText);
        out.writeString(description);
        out.writeString(venue);
        out.writeString(track);
        out.writeString(moderator);
        out.writeList(keyNoteList);

    }
}
