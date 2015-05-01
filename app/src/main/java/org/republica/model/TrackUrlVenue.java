package org.republica.model;

/**
 * Created by Abhishek on 06/03/15.
 */
public class TrackUrlVenue {

    private String url;
    private String version;
    private String venue;
    private String key;

    public TrackUrlVenue(String url, String version, String venue, String key) {
        this.url = url;
        this.version = version;
        this.venue = venue;
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
