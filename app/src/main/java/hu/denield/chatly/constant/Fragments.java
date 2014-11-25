package hu.denield.chatly.constant;

public class Fragments {
    public static enum FragmentName {
        SETTINGS, ABOUT
    }

    public static FragmentName fromInteger(int x) {
        switch(x) {
            case 0:
                return FragmentName.SETTINGS;
            case 1:
                return FragmentName.ABOUT;
        }
        return null;
    }
}
