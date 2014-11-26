package hu.denield.chatly.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageDataManager {

    private static MessageDataManager instance;

    public static MessageDataManager getInstance() {
        if (instance == null) {
            synchronized(MessageDataManager.class) {
                if (instance == null) {
                    instance = new MessageDataManager();
                }
            }
        }
        return instance;
    }

    private List<MessageData> messages;

    private MessageDataManager() {
        messages = new ArrayList<MessageData>();
    }

    public static synchronized void add(MessageData message) {
        instance.messages.add(message);
    }

    public List<MessageData> getMessages() {
        return messages;
    }
}
