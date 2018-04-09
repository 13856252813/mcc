package com.txt.library.model;

import android.location.Location;
import android.net.Uri;

/**
 * Created by pc on 2017/3/1.
 */

public class ChatSend {
    private int id;

    private String text;

    private String OtherText;

    private Uri mediaPath;

    private Location location;

    private int type;

    private long timeTamp;

    public void setTimeTamp(long timeTamp) {
        this.timeTamp = timeTamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimeTamp() {
        return timeTamp;
    }

    public String getText() {
        return text;
    }

    public String getOtherText() {
        return OtherText;
    }

    public Uri getMediaPath() {
        return mediaPath;
    }

    public Location getLocation() {
        return location;
    }

    public int getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOtherText(String otherText) {
        OtherText = otherText;
    }

    public void setMediaPath(Uri mediaPath) {
        this.mediaPath = mediaPath;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setType(int type) {
        this.type = type;
    }
}
