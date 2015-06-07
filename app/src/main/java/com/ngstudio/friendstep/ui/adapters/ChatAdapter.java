package com.ngstudio.friendstep.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;
import com.ngstudio.friendstep.model.entity.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ChatAdapter extends BaseArrayAdapter<Message> implements StickyListHeadersAdapter {

    private LayoutInflater inflater;
    public ChatAdapter(Context context, int style, List<Message> list) {
        super(context, style, list);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatHolder holder;
        Log.d("SCROLL","Position = "+position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chat, parent, false);
            holder = holderInitialise(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ChatHolder)convertView.getTag();
        }

        Message message = getItem(position);

        if (position != getCount()-1) {
               Message nextMessage = getItem(position+1);
                if ( nextMessage.getSenderId().equals(message.getSenderId()) &&
                        resetTime(nextMessage.getMessagetime()) - resetTime(message.getMessagetime()) < DAY_IN_MILLIS )
                    holder.view.setVisibility(View.GONE);
                 else
                    holder.view.setVisibility(View.VISIBLE);
        } else {
            holder.view.setVisibility(View.GONE);
        }

        int iconSize = (int) getContext().getResources().getDimension(R.dimen.size_item_imageview);
        if (position == 0) {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setLayoutParams(new RelativeLayout.LayoutParams(iconSize,iconSize));
        } else  {
            Message previousMessage = getItem(position-1);

            if ( !previousMessage.getSenderId().equals(message.getSenderId()) ||
                        resetTime(message.getMessagetime()) - resetTime(previousMessage.getMessagetime()) >= DAY_IN_MILLIS ) {
                holder.ivIcon.setVisibility(View.VISIBLE);
                holder.ivIcon.setLayoutParams(new RelativeLayout.LayoutParams(iconSize,iconSize));

            } else {
                holder.ivIcon.setVisibility(View.INVISIBLE);
                holder.ivIcon.setLayoutParams(new RelativeLayout.LayoutParams(iconSize,0));
            }
        }

        //WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(message.getSenderId()),holder.ivIcon);
        Log.d("SENDER_NAME", "Name = "+message.getSendername());
        WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(message.getSendername()), holder.ivIcon);
        holder.tvMessage.setText( message.isMine() || TextUtils.isEmpty(message.getReceivemessage()) ? message.getMessage() : message.getReceivemessage());
        holder.tvTime.setText(formatTime(message.getMessagetime()));
        return convertView;
    }

    private String formatTime(long unixTime) {
        Date date = new Date(unixTime*1000);
        return new SimpleDateFormat("HH:mm").format(date);
    }

    private ChatHolder holderInitialise(View view) {
        ChatHolder holder = new ChatHolder();
        holder.ivIcon = (ImageView) view.findViewById(R.id.ivContactsPhoto);
        holder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        holder.tvTime = (TextView) view.findViewById(R.id.tvTime);
        holder.view = view.findViewById(R.id.chatDivider);
        return holder;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ChatHeaderHolder holder;
        if (convertView == null) {
            holder = new ChatHeaderHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.tvHeader = (TextView) convertView.findViewById(R.id.tvHeaderChat);
            convertView.setTag(holder);
        } else {
            holder = (ChatHeaderHolder) convertView.getTag();
        }

        Message message = getItem(position);
        holder.tvHeader.setText(formatDate(message.getMessagetime()));
        return convertView;
    }

    @Override
    public Message getItem(int position) {
        return super.getItem(getCount() - position - 1);
    }

    private String formatDate(long unixTime) {
        long obtainedDate = resetTime(unixTime);
        long todayDate = resetTime(Calendar.getInstance().getTimeInMillis());
        long timeDuration = todayDate - obtainedDate;
        Date date = new Date(obtainedDate);

        if(timeDuration == DAY_IN_MILLIS) {
            return getContext().getString(R.string.time_yesterday);
        } else if (timeDuration < DAY_IN_MILLIS) {
            return getContext().getString(R.string.time_today);
        } else {
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
    }

    private final int DAY_IN_MILLIS = 24*3600*1000;

    @Override
    public long getHeaderId(int position) {
        Message message = getItem(position);
        return resetTime(message.getMessagetime());
    }

    private long resetTime(long unixTime) {
        unixTime = unixTime*1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    static class ChatHeaderHolder {
        TextView tvHeader;
    }

    static class ChatHolder {
        ImageView ivIcon;
        TextView tvMessage;
        TextView tvTime;
        View view;
    }
}

