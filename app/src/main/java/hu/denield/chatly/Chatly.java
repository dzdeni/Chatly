package hu.denield.chatly;

import android.app.Application;
import android.location.Location;
import android.net.TrafficStats;

public class Chatly extends Application {

    private String username;
    private String password;
    private Location location;

    private long downloadedAtStart;
    private long uploadedAtStart;

    @Override
    public void onCreate() {
        super.onCreate();

        TrafficStats stats = new TrafficStats();
        downloadedAtStart = stats.getUidRxBytes(getApplicationInfo().uid);
        uploadedAtStart = stats.getUidTxBytes(getApplicationInfo().uid);
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

    public long getUploadedAtStart() {
        return uploadedAtStart;
    }

    public long getDownloadedAtStart() {
        return downloadedAtStart;
    }
}
