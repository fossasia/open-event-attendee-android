package org.republica.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Link implements Parcelable {

    public static final Parcelable.Creator<Link> CREATOR = new Parcelable.Creator<Link>() {
        public Link createFromParcel(Parcel in) {
            return new Link(in);
        }

        public Link[] newArray(int size) {
            return new Link[size];
        }
    };
    private String url;
    private String description;

    public Link() {
    }

    private Link(Parcel in) {
        url = in.readString();
        description = in.readString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        Link other = (Link) obj;
        return url.equals(other.url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(url);
        out.writeString(description);
    }
}
