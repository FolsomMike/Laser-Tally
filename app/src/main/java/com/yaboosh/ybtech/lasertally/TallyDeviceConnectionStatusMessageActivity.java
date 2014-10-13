/******************************************************************************
 * Title: TallyDeviceConnectionStatusMessageActivity.java
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

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceConnectionStatusMessageActivity
//

public class TallyDeviceConnectionStatusMessageActivity extends Activity {

    public static final String TAG = "TallyDeviceConnectionStatusMessageActivity";

    private View decorView;
    private int uiOptions;

    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    Handler timerHandler = new Handler();

    private static String tallyDeviceName;

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::MessageActivity (constructor)
    //

    public TallyDeviceConnectionStatusMessageActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of TallyDeviceConnectionStatusMessageActivity::MessageActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tally_device_connection_status_message);

        this.setFinishOnTouchOutside(false);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        serviceIntent = new Intent(this, TallyDeviceService.class);

    }//end of TallyDeviceConnectionStatusMessageActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

    }//end of TallyDeviceConnectionStatusMessageActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon resume should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        decorView.setSystemUiVisibility(uiOptions);

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of TallyDeviceConnectionStatusMessageActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::onPause
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

    }//end of TallyDeviceConnectionStatusMessageActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::connection
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

    };//end of TallyDeviceConnectionStatusMessageActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::createUiChangeListener
    //
    // Listens for visibility changes in the ui.
    //
    // If the system bars are visible, the system visibility is set to the uiOptions.
    //
    //

    private void createUiChangeListener() {

        decorView.setOnSystemUiVisibilityChangeListener (
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int pVisibility) {

                        if ((pVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                    }

                });

    }//end of TallyDeviceConnectionStatusMessageActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::exitActivity
    //
    // Finishes and closes the activity.
    //

    public void exitActivity() {

        finish();

    }//end of TallyDeviceConnectionStatusMessageActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::handleConnectedState
    //
    // Displays text to the user about being connected to the remote device with
    // the preset name.
    //

    public void handleConnectedState() {

        ///debug hss//
        Log.d(TAG, "Handle connected state");

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

    }//end of TallyDeviceConnectionStatusMessageActivity::handleConnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::handleConnectingState
    //
    // Displays text to the user about connecting to the remote device with the
    // preset name.
    //

    public void handleConnectingState() {

        //debug hss//
        Log.d(TAG, "Handle connecting state");
        setProgressBarVisible(true);
        setGreenCheckMarkVisible(false);
        setMessageText("Connecting to " + tallyDeviceName);

    }//end of TallyDeviceConnectionStatusMessageActivity::handleConnectingState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::registerWithService
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

    }//end of TallyDeviceConnectionStatusMessageActivity::registerWithService
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::setGreenCheckMarkVisible
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

    }//end of TallyDeviceConnectionStatusMessageActivity::setGreenCheckMarkVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::setMessageText
    //
    // Gets the text view used for messages and sets its text to the passed in
    // message.
    //

    private void setMessageText(String pMessage) {

        TextView tempText = (TextView) findViewById(R.id.tallyDeviceConnectionStatusMessageTextView);

        tempText.setText(pMessage);

    }//end of TallyDeviceConnectionStatusMessageActivity::setMessageText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::setProgressBarVisible
    //
    // Sets the progress bar is set to VISIBLE or GONE depending on the passed in
    // boolean.
    //

    private void setProgressBarVisible(boolean pBool) {

        ProgressBar tempBar = (ProgressBar)findViewById(R.id.tallyDeviceConnectionStatusMessageProgressBar);

        if (pBool) { tempBar.setVisibility(View.VISIBLE); }
        else { tempBar.setVisibility(View.GONE); }

    }//end of TallyDeviceConnectionStatusMessageActivity::setProgressBarVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionStatusMessageActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(TallyDeviceService.State pNewState, Message pMsg) {

        //debug hss//
        Log.d(TAG, "connection state changed: " + pNewState);

        state = pNewState;

        if (state == TallyDeviceService.State.CONNECTED) {
            tallyDeviceName = (String)pMsg.obj;
            handleConnectedState();
        }
        else if (state == TallyDeviceService.State.CONNECTING) {
            tallyDeviceName = (String)pMsg.obj;
            handleConnectingState();
        }

    }//end of TallyDeviceConnectionStatusMessageActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class TallyDeviceConnectionStatusMessageActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<TallyDeviceConnectionStatusMessageActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(TallyDeviceConnectionStatusMessageActivity pActivity) {

            activity = new WeakReference<TallyDeviceConnectionStatusMessageActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            TallyDeviceConnectionStatusMessageActivity tempActivity = activity.get();
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

    }//end of class TallyDeviceConnectionStatusMessageActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class TallyDeviceConnectionStatusMessageActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------