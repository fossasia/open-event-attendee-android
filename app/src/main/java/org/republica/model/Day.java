package org.republica.model;

/**
 * Created by Abhishek on 08/03/15.
 */
public class Day {

    private int position;
    private String date;

    public Day(int position, String date) {
        this.position = position;
        this.date = date;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
