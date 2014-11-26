package hu.denield.chatly.data;

import android.location.Location;

import java.io.Serializable;

public class MessageData implements Serializable {
    private long time;
    private String username;
    private String message;
    private Location location;

    public MessageData(long time, String username, String message, Location location) {
        this.time = time;
        this.username = username;
        this.message = message;
        this.location = location;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
