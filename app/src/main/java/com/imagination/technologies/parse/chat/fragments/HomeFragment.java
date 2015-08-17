package com.imagination.technologies.parse.chat.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.imagination.technologies.parse.chat.R;
import com.imagination.technologies.parse.chat.adapters.ChatListAdapter;
import com.imagination.technologies.parse.chat.beans.Message;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getName();
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    private static final int REFRESH_TIMEOUT = 200;
    private static String sUserId;

    private EditText etMessage;
    private Button btSend;
    private ListView lvChat;
    private ChatListAdapter mAdapter;

    // Create a handler which can run code periodically
    private Handler handler = new Handler();
    private ArrayList<Message> mMessages = new ArrayList<>();

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        // User login
        if (ParseUser.getCurrentUser() != null) {
            // start with existing user
            startWithCurrentUser();
        } else {
            // If not logged in, login as a new anonymous user
            login();
        }

        // Run the runnable object defined every REFRESH_TIMEOUT ms
        handler.postDelayed(runnable, REFRESH_TIMEOUT);
    }

    // Defines a runnable which is run every REFRESH_TIMEOUT ms
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            //handler.postDelayed(this, REFRESH_TIMEOUT);
        }
    };

    // Get the userId from the cached currentUser object
    private void startWithCurrentUser() {
        sUserId = ParseUser.getCurrentUser().getObjectId();
        setupMessagePosting();
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    private void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "Anonymous login failed: " + e.toString());
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }

    // Setup button event handler which posts the entered message to Parse
    private void setupMessagePosting() {
        // This View
        View view = getView();

        // Find the text field and button
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        btSend = (Button) view.findViewById(R.id.btSend);
        lvChat = (ListView) view.findViewById(R.id.lvChat);
        mAdapter = new ChatListAdapter(getActivity(), sUserId, mMessages);
        lvChat.setAdapter(mAdapter);
        lvChat.setEmptyView(view.findViewById(R.id.empty));

        // When send button is clicked, create message object on Parse
        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String data = etMessage.getText().toString();
                Message message = new Message();
                message.setUserId(sUserId);
                message.setBody(data);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        refreshMessages();
                    }
                });
                etMessage.setText("");
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    public void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);

        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        query.orderByAscending("createdAt");

        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {
                if (!isAdded()) return;

                if (e == null) {
                    mMessages.clear();
                    mAdapter.addAll(messages);
                } else {
                    Log.d(TAG, "error: " + e.getMessage());
                }
            }
        });
    }
}
