package edu.uncg.studdybuddy.studybuddy;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Anthony Ratliff on 4/4/2017.
 */

class ChatAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatRoomMessage> mMessageList;

    ChatAdapter(Context mContext, List<ChatRoomMessage> mMessageList) {
        this.mContext = mContext;
        this.mMessageList = mMessageList;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.message_left, null);
        TextView sender = (TextView) v.findViewById(R.id.txtSender);
        TextView time = (TextView) v.findViewById(R.id.txtDate);
        TextView message = (TextView) v.findViewById(R.id.txtMessage);

        // Set Text for TextView
        sender.setText(mMessageList.get(position).getSender());
        time.setText(mMessageList.get(position).getTime());
        message.setText(mMessageList.get(position).getMessage());

        // Save product id to tag
        v.setTag(position);

        return v;
    }
}
