package hu.denield.chatly;

import android.app.Application;

public class Chatly extends Application {

    private String username;
    private String password;

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
}
