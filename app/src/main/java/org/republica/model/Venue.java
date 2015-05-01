package org.republica.model;

import org.republica.db.DatabaseHelper;
import org.republica.utils.StringUtils;

/**
 * Created by Abhishek on 12/03/15.
 */
public class Venue {

    private String track;
    private String venue;
    private String map;
    private String room;
    private String link;
    private String address;
    private String howToReach;

    public Venue(String track, String venue, String map, String room, String link, String address, String howToReach) {
        this.track = track;
        this.venue = venue;
        this.map = map;
        this.room = room;
        this.link = link;
        this.address = address;
        this.howToReach = howToReach;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHowToReach() {
        return howToReach;
    }

    public void setHowToReach(String howToReach) {
        this.howToReach = howToReach;
    }

    public String generateSql() {

        //tract TEXT, venue TEXT, map TEXT, room TEXT, link TEXT, address TEXT, how_to_reach TEXT
        String query = "INSERT INTO %s VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')";
        query = String.format(query, DatabaseHelper.TABLE_NAME_VENUE, StringUtils.replaceUnicode(track), StringUtils.replaceUnicode(venue), StringUtils.replaceUnicode(map), StringUtils.replaceUnicode(room), StringUtils.replaceUnicode(link), StringUtils.replaceUnicode(address), StringUtils.replaceUnicode(howToReach));
        return query;
    }
}
