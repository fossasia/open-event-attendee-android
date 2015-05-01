package org.republica.model;

import org.republica.db.DatabaseHelper;
import org.republica.utils.StringUtils;

/**
 * Created by manan on 25-03-2015.
 */
public class Sponsor {
    private int id;
    private String name;
    private String img;
    private String url;

    public Sponsor(int id, String name, String img, String url){
        this.id = id;
        this.name = name;
        this.img = img;
        this.url =url;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String generatesql(){
        String query = "INSERT INTO %s VALUES ('%d', '%s', '%s', '%s')";
        query = String.format(query, DatabaseHelper.TABLE_NAME_SPONSOR, id, StringUtils.replaceUnicode(name), StringUtils.replaceUnicode(img), StringUtils.replaceUnicode(url));
        return query;
    }
}
