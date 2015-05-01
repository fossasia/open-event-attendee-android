package org.republica.model;

import org.republica.db.DatabaseHelper;

/**
 * Created by Abhishek on 17/02/15.
 */
public class KeySpeaker {

    private int id;
    private String name;
    private String information;
    private String linkedInUrl;
    private String twitterHandle;
    private String designation;
    private String profilePicUrl;

    public KeySpeaker(int id, String name, String information, String linkedInUrl, String twitterHandle, String designation, String profilePicUrl) {
        this.id = id;
        this.name = name;
        this.information = information;
        this.linkedInUrl = linkedInUrl;
        this.twitterHandle = twitterHandle;
        this.designation = designation;
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String generateSqlQuery() {
        String query = String.format("INSERT INTO %s VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s');", DatabaseHelper.TABLE_NAME_KEY_SPEAKERS, id, name, designation, information, twitterHandle, linkedInUrl, profilePicUrl);
        return query;
    }
}
