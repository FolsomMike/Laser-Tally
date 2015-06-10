/******************************************************************************
 * Title: TallyDeviceScanActivity.java
 * Author: Hunter Schoonover
 * Date: 10/01/14
 *
 * Purpose:
 *
 * This class is an activity used to scan for and display tally devices. Upon
 * user selection, a remote device is connected to and this activity is closed.
 *
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceScanActivity
//

public class TallyDeviceScanActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;

    private AbsListView listView;
    private TextView emptyView;

    ArrayList<String> deviceNames = new ArrayList<String>();

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::TallyDeviceConnectionStatusActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public TallyDeviceScanActivity()
    {

        layoutResID = R.layout.activity_tally_device_scan;

        LOG_TAG = "TallyDeviceScanActivity";

        messenger = new Messenger(new IncomingHandler(this));

    }//end of TallyDeviceScanActivity::TallyDeviceConnectionStatusActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onCreate
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

    }//end of TallyDeviceScanActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onDestroy
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

    }//end of TallyDeviceScanActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of TallyDeviceScanActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onPause
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

            Message msg = Message.obtain(null,
                    TallyDeviceService.MSG_UNREGISTER_TALLY_DEVICE_SCAN_ACTIVITY);
            if (msg == null) { return; }
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of TallyDeviceScanActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::performOnResumeActivitySpecificActions
    //
    // All functions that must be done upon activity resume should be called here.
    //

    protected void performOnResumeActivitySpecificActions() {

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of TallyDeviceScanActivity::performOnResumeActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onActivityResult
    //
    // Listens for activity results and performs different actions depending
    // on their request and result codes.
    //

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {

        // This activity does not have a need for any
        // activity results, so it sends them to the
        // tally device service.
        Message msg = Message.obtain(null, TallyDeviceService.MSG_ACTIVITY_RESULT);
        if (msg == null) { return; }
        msg.arg1 = pRequestCode;
        msg.arg2 = pResultCode;
        msg.obj = pData;
        try { service.send(msg); } catch (Exception e) { unbindService(connection); }

    }//end of TallyDeviceScanActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::connection
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

            if (service != null) { startScan(); }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            service = null;

        }

    };//end of TallyDeviceScanActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onClickListener
    //
    // Not really a function.
    //
    // Listeners for clicks on the objects to which it was handed.
    //
    // Ids are used to determine which object was pressed.
    // When assigning this listener to any new objects, add the object's id to the
    // switch statement and handle the case properly.
    //

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View pV) {

            int id = pV.getId();

            if (id == R.id.deviceNameTextView) {
                handleDeviceClick(((TextView) pV).getText().toString());
            }

        }

    };//end of TallyDeviceScanActivity::onClickListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::createDeviceNameTextView
    //
    // Returns a selectable text view containing the passed in string.
    //
    // A pointer to the created TextView is added to the focus array.
    //

    private TextView createDeviceNameTextView(String pString) {

        TextView t = (TextView)getLayoutInflater().inflate
                                                    (R.layout.selectable_text_view_template, null);
        t.setId(R.id.deviceNameTextView);
        t.setClickable(true);
        t.setFocusable(true);
        t.setFocusableInTouchMode(false);
        t.setOnClickListener(onClickListener);
        t.setText(pString);

        focusArray.add(t);

        return t;

    }//end of TallyDeviceScanActivity::createDeviceNameTextView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::exitActivity
    //
    // Finishes and closes the activity.
    //

    public void exitActivity() {

        finish();

    }//end of TallyDeviceScanActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::finishActivityAndStartMessageActivity
    //
    // Stops the scan, closes this activity and starts the message activity.
    //

    private void finishActivityAndStartMessageActivity() {

        Intent intent = new Intent(this, TallyDeviceConnectionStatusActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

        exitActivity();

    }//end of TallyDeviceScanActivity::finishActivityAndStart\MessageActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleRedXButtonPressed
    //
    // Closes the activity.
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivity();

    }//end of TallyDeviceScanActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleDeviceClick
    //
    // Sends the passed in a message to the TallyDeviceService.
    //

    public void handleDeviceClick(String pName) {

        Message msg = Message.obtain(null, TallyDeviceService.MSG_CONNECT_TO_TALLY_DEVICE);
        if (msg == null) { return; }
        msg.obj = pName;
        try { service.send(msg); } catch (Exception e) { unbindService(connection); }

        finishActivityAndStartMessageActivity();

    }//end of TallyDeviceScanActivity::handleDeviceClick
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleDisconnectedState
    //
    // Sets the scanning state of deviceList to false and sets the progress bar's
    // visible state to false.
    //

    public void handleDisconnectedState() {

        setScanning(false);

    }//end of TallyDeviceScanActivity::handleDisconnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleStartActivityForResultMessage
    //
    // Starts an activity using the data obtained from the passed in message for
    // the action and the request code.
    //

    private void handleStartActivityForResultMessage(Message pMsg) {

        if (pMsg == null) { return; }
        Intent intent = new Intent((String)pMsg.obj);
        startActivityForResult(intent, pMsg.arg1);

    }//end of TallyDeviceScanActivity::handleStartActivityForResultMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleScanningState
    //
    // Sets the scanning state to true.
    //

    private void handleScanningState() {

        setScanning(true);

    }//end of TallyDeviceScanActivity::handleScanningState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::handleTallyDeviceNameMessage
    //
    // Adds the name gotten from the message to the tally device name list and
    // displays the list of names to the user.
    //

    private void handleTallyDeviceNameMessage(Message pMsg) {

        addDeviceName((String)pMsg.obj);

    }//end of TallyDeviceScanActivity::handleTallyDeviceNameMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::registerWithService
    //
    // Sends a message to the TallyDeviceService to register.
    //

    private void registerWithService() {

        try {

            Message msg = Message.obtain(null,
                                        TallyDeviceService.MSG_REGISTER_TALLY_DEVICE_SCAN_ACTIVITY);
            if (msg == null) { return; }
            msg.obj = this;
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of TallyDeviceScanActivity::registerWithService
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::addDeviceName
    //
    // Stores and displays to the user the passed in device name.
    //

    private void addDeviceName(String pName) {

        deviceNames.add(pName);

        LinearLayout layout = (LinearLayout)findViewById(R.id.deviceNamesLayout);
        layout.addView(createDeviceNameTextView(pName));

    }//end of TallyDeviceScanActivity::addDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::setScanning
    //
    // Sets the scanning state of deviceList and sets the progress bar's visible
    // state depending on the passed in boolean.
    //

    public void setScanning(boolean pScanning) {

        View            horSpacer = findViewById(R.id.specialHorizontalSpacer);
        TextView        noDevicesFoundTextView = (TextView)findViewById(R.id.noDevicesTextView);
        ProgressBar     progBar = (ProgressBar)findViewById(R.id.tallyDeviceScanProgressBar);
        TextView        statusTextView = (TextView)findViewById(R.id.statusTextView);

        if (pScanning) {
            horSpacer.setVisibility(View.VISIBLE);
            noDevicesFoundTextView.setVisibility(View.GONE);
            progBar.setVisibility(View.VISIBLE);
            statusTextView.setText("Looking for devices...");
        }
        else {
            horSpacer.setVisibility(View.GONE);
            if (deviceNames.isEmpty()) { noDevicesFoundTextView.setVisibility(View.VISIBLE); }
            progBar.setVisibility(View.GONE);
            statusTextView.setText("Done looking for devices.");
        }

    }//end of TallyDeviceScanActivity::setScanning
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::startScan
    //
    // Goes through the process of starting a scan for tally devices.
    //

    private void startScan() {

        focusArray.clear();
        deviceNames.clear();
        setScanning(true);

        Message msg = Message.obtain(null, TallyDeviceService.MSG_START_SCAN_FOR_TALLY_DEVICES);
        if (msg == null) { return; }
        try { service.send(msg); } catch (Exception e) { unbindService(connection); }

    }//end of TallyDeviceScanActivity::startScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::stateChanged
    //
    // Performs different operations depending on the passed in connection state.
    //

    private void stateChanged(TallyDeviceService.State pNewState) {

        state = pNewState;
        if (state == TallyDeviceService.State.SCANNING) { handleScanningState(); }
        else if (state == TallyDeviceService.State.DISCONNECTED) { handleDisconnectedState(); }

    }//end of TallyDeviceScanActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class TallyDeviceScanActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<TallyDeviceScanActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(TallyDeviceScanActivity pActivity) {

            activity = new WeakReference<TallyDeviceScanActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            TallyDeviceScanActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {

                    case TallyDeviceService.MSG_CONNECTION_STATE:
                        tempActivity.stateChanged(TallyDeviceService.State.values()[pMsg.arg1]);
                        break;

                    case TallyDeviceService.MSG_START_ACTIVITY_FOR_RESULT:
                        tempActivity.handleStartActivityForResultMessage(pMsg);
                        break;

                    case TallyDeviceService.MSG_TALLY_DEVICE_NAME:
                        tempActivity.handleTallyDeviceNameMessage(pMsg);
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class TallyDeviceScanActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class TallyDeviceScanActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------