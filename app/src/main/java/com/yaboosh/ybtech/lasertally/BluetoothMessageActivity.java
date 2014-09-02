/******************************************************************************
 * Title: BluetoothMessageActivity.java
 * Author: Hunter Schoonover
 * Date: 08/20/14
 *
 * Purpose:
 *
 * This class is an activity used to display messages to users about the
 * current Bluetooth status.
 * The activity takes the form of an activity dialog.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class BluetoothMessageActivity
//

public class BluetoothMessageActivity extends Activity {

    public static final String TAG = "MessageActivity";
    private BluetoothLeVars.State state = BluetoothLeVars.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    Handler timerHandler = new Handler();

    private static String deviceName;

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::BluetoothMessageActivity (constructor)
    //

    public BluetoothMessageActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of BluetoothMessageActivity::BluetoothMessageActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of BluetoothMessageActivity onCreate");

        setContentView(R.layout.activity_bluetooth_message);

        this.setFinishOnTouchOutside(false);

        serviceIntent = new Intent(this, BluetoothLeService.class);

    }//end of BluetoothMessageActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of BluetoothMessageActivity onDestroy");

        super.onDestroy();

    }//end of BluetoothMessageActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of BluetoothMessageActivity onResume");

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of BluetoothMessageActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of BluetoothMessageActivity onPause");

        unbindService(connection);

        if (service == null) {
            Log.d(TAG, "service was null -- return from function");
            return;
        }

        try {
            Message msg = Message.obtain(null,
                                    BluetoothLeService.MSG_UNREGISTER_BLUETOOTH_MESSAGE_ACTIVITY);
            if (msg != null) {
                msg.replyTo = messenger;
                service.send(msg);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering with BleService", e);
            service = null;
        }

    }//end of BluetoothMessageActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::connection
    //
    // Not really a function
    //
    // Creates a new ServiceConnection object and overrides its onServiceConnected()
    // and onServiceDisconnected() functions.
    //

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName pName, IBinder pService) {

            Log.d(TAG, "Service connected");

            service = new Messenger(pService);

            try {

                Message msg = Message.obtain(null,
                        BluetoothLeService.MSG_REGISTER_BLUETOOTH_MESSAGE_ACTIVITY);
                if (msg != null) {
                    msg.replyTo = messenger;
                    service.send(msg);
                } else {
                    Log.d(TAG, "service is null");
                    service = null;
                }

            } catch (Exception e) {
                Log.w(TAG, "Error connecting to BleService", e);
                service = null;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "Service disconnected");

            service = null;

        }

    };//end of BluetoothMessageActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::exitActivity
    //
    // Finishes and closes the activity.
    //

    public void exitActivity() {

        finish();

    }//end of BluetoothMessageActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::handleConnectedState
    //
    // Displays text to the user about being connected to the remote device with
    // the preset name.
    //

    public void handleConnectedState() {

        setProgressBarVisible(false);
        setGreenCheckMarkVisible(true);
        setMessageText("Connected to " + deviceName);
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                exitActivity();

            }

        }, 10000);

    }//end of BluetoothMessageActivity::handleConnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::handleConnectingState
    //
    // Displays text to the user about connecting to the remote device with the
    // preset name.
    //

    public void handleConnectingState() {

        setProgressBarVisible(true);
        setGreenCheckMarkVisible(false);
        setMessageText("Connecting to " + deviceName);

    }//end of BluetoothMessageActivity::handleConnectingState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::handleDescriptorWriteSuccess
    //
    // Sets the message text to represent a connected state to the remote device.
    //
    // The message text isn't set until now because of some connecting problems.
    // If the descriptor has a successful write, then it signifies that the
    // remote device is properly connected.
    //

    private void handleDescriptorWriteSuccess() {

        Log.d(TAG, "handleDescriptorWriteSuccess::Descriptor write success");

        handleConnectedState();

    }//end of BluetoothMessageActivity::handleDescriptorWriteSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::setDeviceName
    //
    // Sets the device name to the passed in string.
    //

    private void setDeviceName(String pName) {

        deviceName = pName;

    }//end of BluetoothMessageActivity::setDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::setGreenCheckMarkVisible
    //
    // Gets the green check mark.
    //
    // The green check mark is set to VISIBLE or INVISIBLE depending on the passed
    // in boolean.
    //

    private void setGreenCheckMarkVisible(boolean pBool) {

        View tempCheck = findViewById(R.id.bluetoothGreenCheckMark);

        if (pBool) {
            tempCheck.setVisibility(View.VISIBLE);
        }
        else {
            tempCheck.setVisibility(View.GONE);
        }

    }//end of BluetoothMessageActivity::setGreenCheckMarkVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::setMessageText
    //
    // Gets the text view used for messages and sets its text to the passed in
    // message.
    //

    private void setMessageText(String pMessage) {

        TextView tempText = (TextView) findViewById(R.id.bluetoothMessageTextView);

        tempText.setText(pMessage);

    }//end of BluetoothMessageActivity::setMessageText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::setProgressBarVisible
    //
    // Gets the progress bar.
    //
    // The progress bar is set to VISIBLE or INVISIBLE depending on the passed in
    // boolean.
    //

    private void setProgressBarVisible(boolean pBool) {

        ProgressBar tempBar = (ProgressBar) findViewById(R.id.bluetoothMessageProgressBar);

        if (pBool) {
            tempBar.setVisibility(View.VISIBLE);
        }
        else {
            tempBar.setVisibility(View.GONE);
        }

    }//end of BluetoothMessageActivity::setProgressBarVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothMessageActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(BluetoothLeVars.State pNewState) {

        state = pNewState;

        if (state == BluetoothLeVars.State.CONNECTED) {
            Log.d(TAG, "state connected");
        }
        else if (state == BluetoothLeVars.State.CONNECTING) {
            Log.d(TAG, "state connecting");
            handleConnectingState();
        }
        else {
            Log.d(TAG, "else state");
            handleConnectingState();
        }

    }//end of BluetoothMessageActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class BluetoothMessageActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the mesenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<BluetoothMessageActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(BluetoothMessageActivity pActivity) {

            activity = new WeakReference<BluetoothMessageActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            Log.d(TAG, "Message received");

            BluetoothMessageActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {

                    case BluetoothLeService.MSG_BT_STATE:
                        Log.d(TAG, "Received Bluetooth state message");
                        tempActivity.stateChanged(BluetoothLeVars.State.values()[pMsg.arg1]);
                        break;

                    case BluetoothLeService.MSG_DESCRIPTOR_WRITE_SUCCESS:
                        Log.d(TAG, "Received descriptor write success message");
                        tempActivity.handleDescriptorWriteSuccess();
                        break;

                    case BluetoothLeService.MSG_REMOTE_DEVICE_NAME:
                        Log.d(TAG, "Received remote device name message: " + pMsg.obj);
                        //debug hss//tempActivity.setDeviceName((String) pMsg.obj);
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class BluetoothMessageActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class BluetoothMessageActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------