package hu.denield.chatly.data;

import android.location.Location;

import java.io.Serializable;

/**
 * Messages stored as an object of this class.
 * After creation, there is no chance to modify
 * its data.
 */
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
}
