package hu.denield.chatly.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import hu.denield.chatly.data.MessageDataManager;

public class MessageFragmentPagerAdapter extends FragmentPagerAdapter {
    public MessageFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * In this method we should use some kind of mechanism, which can store the fragments instead of
     * creating new one every time.
     *
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        return null;
    }


    @Override
    public int getCount() {
        return MessageDataManager.getInstance().getMessages().size();
    }
}
