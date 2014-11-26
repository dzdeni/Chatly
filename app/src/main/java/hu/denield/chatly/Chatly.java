package hu.denield.chatly;

import android.app.Application;
import android.location.Location;

public class Chatly extends Application {

    private String username;
    private String password;
    private Location location;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
