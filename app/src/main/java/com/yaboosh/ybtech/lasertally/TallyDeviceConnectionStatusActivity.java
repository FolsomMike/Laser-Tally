/******************************************************************************
 * Title: TallyDeviceConnectionStatusActivity.java
 * Author: Hunter Schoonover
 * Date: 10/09/14
 *
 * Purpose:
 *
 * This class is an activity used to display messages about the tally device
 * connection status to the user.
 * The device name and connection status are sent from the TallyDeviceService.
 *
 * The activity takes the form of an activity dialog.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceConnectionStatusActivity
//

public class TallyDeviceConnectionStatusActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    Handler timerHandler = new Handler();

    private static String tallyDeviceName;

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::TallyDeviceConnectionStatusActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public TallyDeviceConnectionStatusActivity()
    {

        layoutResID = R.layout.activity_tally_device_connection_status;

        LOG_TAG = "TallyDeviceConnectionStatusActivity";

        messenger = new Messenger(new IncomingHandler(this));

    }//end of TallyDeviceConnectionStatusActivity::TallyDeviceConnectionStatusActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

        serviceIntent = new Intent(this, TallyDeviceService.class);

    }//end of TallyDeviceConnectionStatusActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    //
    // All functions that must be done upon activity destruction should be
    // called here.
    //

    @Override
    protected void onDestroy() {

        activitiesLaunched.getAndDecrement();

        super.onDestroy();

    }//end of TallyDeviceConnectionStatusActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::onResume
    //
    // Automatically called upon activity resume.
    //
    // All functions that must be done upon activity resume should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of TallyDeviceConnectionStatusActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::handleEscapeKeyPressed
    //
    // This functions is overridden and left blank so that the user cannot use
    // the escape key to exit the activity.
    //

    @Override
    protected void handleEscapeKeyPressed() {

    }//end of TallyDeviceConnectionStatusActivity::handleEscapeKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::handleF1KeyPressed
    //
    // This functions is overridden and left blank so that the user cannot use
    // the F1 key inside the activity.
    //

    @Override
    protected void handleF1KeyPressed() {

    }//end of TallyDeviceConnectionStatusActivity::handleF1KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        try { unbindService(connection); } catch (Exception e) {}

        if (service == null) { return; }

        try {

            Message msg = Message.obtain(null, TallyDeviceService.MSG_UNREGISTER_MESSAGE_ACTIVITY);
            if (msg == null) { return; }
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of TallyDeviceConnectionStatusActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::connection
    //
    // Not really a function
    //
    // Creates a new ServiceConnection object and overrides its onServiceConnected()
    // and onServiceDisconnected() functions.
    //

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName pName, IBinder pService) {

            service = new Messenger(pService);

            registerWithService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            service = null;

        }

    };//end of TallyDeviceConnectionStatusActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::exitActivity
    //
    // Finishes and closes the activity.
    //

    public void exitActivity() {

        finish();

    }//end of TallyDeviceConnectionStatusActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::handleConnectedState
    //
    // Displays text to the user about being connected to the remote device with
    // the preset name.
    //

    public void handleConnectedState() {

        //DEBUG HSS//
        Log.d(LOG_TAG, "Handle connected state");

        setProgressBarVisible(false);
        setGreenCheckMarkVisible(true);
        setMessageText("Connected to " + tallyDeviceName);

        // Waits for 2 seconds so that the user
        // can see the message and then exits the
        // activity
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                exitActivity();

            }

        }, 2000);

    }//end of TallyDeviceConnectionStatusActivity::handleConnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::handleConnectingState
    //
    // Displays text to the user about connecting to the remote device with the
    // preset name.
    //

    public void handleConnectingState() {

        //DEBUG HSS//
        Log.d(LOG_TAG, "Handle connecting state");
        setProgressBarVisible(true);
        setGreenCheckMarkVisible(false);
        setMessageText("Connecting to " + tallyDeviceName);

    }//end of TallyDeviceConnectionStatusActivity::handleConnectingState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::handleDisconnectedState
    //
    // Displays a message to the user saying that connecting failed.
    //

    public void handleDisconnectedState() {

        setProgressBarVisible(false);
        setGreenCheckMarkVisible(false);
        setMessageText("Failed to connect to " + tallyDeviceName);

    }//end of TallyDeviceConnectionStatusActivity::handleDisconnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::registerWithService
    //
    // Sends a message to the TallyDeviceService to register.
    //

    private void registerWithService() {

        try {

            Message msg = Message.obtain(null, TallyDeviceService.MSG_REGISTER_MESSAGE_ACTIVITY);
            if (msg == null) { return; }
            msg.obj = this;
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of TallyDeviceConnectionStatusActivity::registerWithService
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::setGreenCheckMarkVisible
    //
    // Gets the green check mark.
    //
    // The green check mark is set to VISIBLE or INVISIBLE depending on the passed
    // in boolean.
    //

    private void setGreenCheckMarkVisible(boolean pBool) {

        View tempCheck = findViewById(R.id.tallyDeviceConnectionStatusMessageGreenCheckMark);

        if (pBool) {
            tempCheck.setVisibility(View.VISIBLE);
        }
        else {
            tempCheck.setVisibility(View.GONE);
        }

    }//end of TallyDeviceConnectionStatusActivity::setGreenCheckMarkVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::setMessageText
    //
    // Gets the text view used for messages and sets its text to the passed in
    // message.
    //

    private void setMessageText(String pMessage) {

        TextView tempText = (TextView) findViewById(R.id.tallyDeviceConnectionStatusMessageTextView);

        tempText.setText(pMessage);

    }//end of TallyDeviceConnectionStatusActivity::setMessageText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::setProgressBarVisible
    //
    // Sets the progress bar is set to VISIBLE or GONE depending on the passed in
    // boolean.
    //

    private void setProgressBarVisible(boolean pBool) {

        ProgressBar tempBar = (ProgressBar)findViewById(R.id.tallyDeviceConnectionStatusMessageProgressBar);

        if (pBool) { tempBar.setVisibility(View.VISIBLE); }
        else { tempBar.setVisibility(View.GONE); }

    }//end of TallyDeviceConnectionStatusActivity::setProgressBarVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(TallyDeviceService.State pNewState, Message pMsg) {

        //DEBUG HSS//
        Log.d(LOG_TAG, "connection state changed: " + pNewState);

        state = pNewState;

        if (state == TallyDeviceService.State.CONNECTED) {
            tallyDeviceName = (String)pMsg.obj;
            handleConnectedState();
        } else if (state == TallyDeviceService.State.CONNECTING) {
            tallyDeviceName = (String)pMsg.obj;
            handleConnectingState();
        } else if (state == TallyDeviceService.State.DISCONNECTED) {
            handleDisconnectedState();
        }

    }//end of TallyDeviceConnectionStatusActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class TallyDeviceConnectionStatusActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<TallyDeviceConnectionStatusActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(TallyDeviceConnectionStatusActivity pActivity) {

            activity = new WeakReference<TallyDeviceConnectionStatusActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            TallyDeviceConnectionStatusActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {

                    case TallyDeviceService.MSG_CONNECTION_STATE:
                        tempActivity.stateChanged(TallyDeviceService.State.values()[pMsg.arg1], pMsg);
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class TallyDeviceConnectionStatusActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class TallyDeviceConnectionStatusActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------