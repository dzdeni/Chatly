package hu.denield.chatly.data;

import java.io.Serializable;

public class MessageData implements Serializable {
    private long time;
    private String username;
    private String message;

    public MessageData(long time, String username, String message) {
        this.time = time;
        this.username = username;
        this.message = message;
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
}
