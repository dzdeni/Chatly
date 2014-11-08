package hu.denield.chatly.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hu.denield.chatly.R;
import hu.denield.chatly.data.MessageData;
import hu.denield.chatly.util.Time;

public class MessageListAdapter extends BaseAdapter {
    private List<MessageData> messages;
    private Context context;

    public MessageListAdapter(Context context, List<MessageData> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    static class ViewHolder {
        TextView timeTextView;
        TextView messageTextView;
        TextView usernameTextView;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            // inflate the layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.messages, parent, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.chat_time);
            viewHolder.messageTextView = (TextView) convertView.findViewById(R.id.chat_message);
            viewHolder.usernameTextView = (TextView) convertView.findViewById(R.id.chat_username);

            // store the holder with the view.
            convertView.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MessageData currentMessage = messages.get(i);

        viewHolder.timeTextView.setText(Time.getTime(currentMessage.getTime()));
        viewHolder.messageTextView.setText(currentMessage.getMessage());
        viewHolder.usernameTextView.setText(currentMessage.getUsername());

        return convertView;
    }
}
