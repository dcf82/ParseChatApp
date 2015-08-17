package com.imagination.technologies.parse.chat.controller;

import android.app.Application;
import android.util.Log;

import com.imagination.technologies.parse.chat.beans.Message;
import com.parse.Parse;
import com.parse.ParseObject;

public class ChatApplication extends Application {
    private static final String LOG = ChatApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        // Load Parse
        loadParse();
    }

    /**
     * Load the Parse Library using the keys with the project registered at:
     * https://www.parse.com
     */
    protected void loadParse() {
        try {
            // Parse Models
            ParseObject.registerSubclass(Message.class);

            // Parse Local Data Store
            Parse.enableLocalDatastore(this);

            // Enable Local Storage
            Parse.enableLocalDatastore(this);

            // Initialize Parse
            Parse.initialize(this);
        } catch(Exception e) {
            Log.i(LOG, "error while setting up Parse :: " + e);
        }
    }
}
