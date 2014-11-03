package hu.denield.chatly.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageDataManager {

    public static final int NUMBER_OF_USERS = 50;

    /**
     * Singleton stuff
     */
    private static MessageDataManager instance;

    public static MessageDataManager getInstance() {
        if(instance == null) {
            synchronized(MessageDataManager.class) {
                if(instance == null) {
                    instance = new MessageDataManager();
                }
            }
        }
        return instance;
    }

    private List<MessageData> messages;

    private MessageDataManager() {
        messages = new ArrayList<MessageData>();

        //Fill the user list with random stuff
        Random r = new Random();
        String[] sureNames = new String[] {"Laszlo", "Beata", "Istvan", "Rodrigez", "Emilio", "Un", "Szasa"};
        String[] lastNames = new String[] {"Kim Jong", "Kovacs", "Kiss", "Szurke", "Lakatos"};
        for (int i = 0 ; i<NUMBER_OF_USERS ; i++) {
            messages.add(new MessageData(1241231, "Teszt", sureNames[r.nextInt(sureNames.length)] + " " + lastNames[r.nextInt(lastNames.length)]));
        }
    }

    public List<MessageData> getUsers() {
        return messages;
    }
}
