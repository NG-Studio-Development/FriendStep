package com.ngstudio.friendstep.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.entity.Message;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.ChatActivity;
import com.ngstudio.friendstep.ui.adapters.ChatAdapter;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.MessagesHelpers;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ChatFragment extends BaseFragment<ChatActivity> implements NotificationManager.Client {

    private final String ERROR_NO_MESSAGE = "No messages have been sent yet!";

    ChatAdapter adapter;
    private String senderName;
    private ContactStep currentContact;
    StickyListHeadersListView stickyList;
    EditText messageText;
    ImageButton sendMessage;
    InputMethodManager imm;

    public static ChatFragment instance(Bundle args) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager.registerClient(this);

        if(getArguments() != null) {
            currentContact = (ContactStep) getArguments().getSerializable(WhereAreYouAppConstants.KEY_CONTACT);
            senderName = currentContact.getName();
        } else { throw new Error("EMPTY CONTACT!"); }

        setHasOptionsMenu(true);
        getHostActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_chat;
    }

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

        messageText = (EditText) view.findViewById(R.id.etWriteMessage);
        sendMessage = (ImageButton) view.findViewById(R.id.ibSendMessage);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        stickyList = (StickyListHeadersListView) view.findViewById(R.id.list);

        if(adapter != null)
            adapter.clear();
        adapter = new ChatAdapter(getActivity(), R.layout.item_chat, MessagesHelpers.getInstance().loadMessages(String.valueOf(currentContact.getId())));

        if ( MessagesHelpers.getInstance().size(/*currentContact.getMobilenumber()*/) == 0 )
            queryGetMessages(WhereAreYouApplication.getInstance().getUserId(), currentContact.getId());
        else
            postList(adapter.getCount() - 1);

        stickyList.setAreHeadersSticky(false);
        stickyList.setAdapter(adapter);
        sendMessage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( CommonUtils.isConnected(getActivity()) )
                    sendMessage();
                else
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        final long idUser = WhereAreYouApplication.getInstance().getUserId();
        final long idFriend = currentContact.getId();
        String message = CommonUtils.getText(messageText);

        if(TextUtils.isEmpty(message))
            return;

        MessagesHelpers.getInstance().queryMessagesSend(idUser,idFriend,message,new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                queryGetMessages(idUser, idFriend);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
        messageText.setText(null);
    }

    public void queryGetMessages(long idUser, long idFriend /*String mobile*/) {

        MessagesHelpers.getInstance().queryMessagesFromServer(idUser, idFriend, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_MESSAGES, result);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if (obj == null)
            return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_MESSAGES) {

            String result = (String) obj;
            fillList(result);

        } else if(what == WhereAreYouAppConstants.NOTIFICATION_MESSAGE_INCOMING) {
            Intent msgIntent = (Intent) obj;
            Message message = getMessageFromIntent(msgIntent);

            long senderId = Long.valueOf(message.getSenderId());
            MessagesHelpers.getInstance().queryMessagesFromServer(WhereAreYouApplication.getInstance().getUserId(), senderId, new BaseResponseCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    //NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_MESSAGES, result);
                    fillList(result);
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void fillList(String jsonString) {

        try {

            Gson gson = new Gson();
            List<Message> messageList = gson.fromJson(jsonString, new TypeToken<List<Message>>() {
            }.getType());

            MessagesHelpers.getInstance().saveMessages(messageList);
            MessagesHelpers.getInstance().putMessages(messageList);

            adapter.notifyDataSetChanged();
            postList(adapter.getCount() - 1);

        } catch (Exception e) {
            e.printStackTrace();
            jsonString = jsonString.contains(ERROR_NO_MESSAGE) ? ERROR_NO_MESSAGE : getString(R.string.toast_unknown_error);
            Toast.makeText(getActivity(), jsonString, Toast.LENGTH_SHORT).show();
        }
    }

    public void postList(final int selectPosition) {
        stickyList.post(new Runnable() {
            @Override
            public void run() {
                stickyList.requestFocusFromTouch();
                stickyList.setSelection(selectPosition);
                stickyList.requestFocus();
            }
        });
    }

    private Message getMessageFromIntent(Intent intent) {
        String messageText = intent.getStringExtra(WhereAreYouAppConstants.SERVER_KEY_MESSAGE);
        String fromId = intent.getStringExtra(WhereAreYouAppConstants.SERVER_KEY_FROM_ID);

        if(messageText == null || fromId == null /*|| messTime == null*/)
            return null;

        return new Message(null, messageText, null, fromId, null, -1 /*, Long.getLong(messTime)*/ );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager.unregisterClient(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("ACTION_BAR_CLICK", "Is clicked");

        switch (item.getItemId()) {
            case android.R.id.home:
                getHostActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
