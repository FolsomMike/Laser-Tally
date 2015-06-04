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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceScanActivity
//

public class TallyDeviceScanActivity extends Activity implements AbsListView.OnItemClickListener {

    public static final String TAG = "DeviceScanActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;

    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;

    private AbsListView listView;
    private TextView emptyView;

    ArrayList<String> deviceNames = new ArrayList<String>();

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::TallyDeviceScanActivity (constructor)
    //

    public TallyDeviceScanActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of TallyDeviceScanActivity::TallyDeviceScanActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tally_device_scan);

        this.setFinishOnTouchOutside(false);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        Bundle bundle = getIntent().getExtras();
        sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);

        serviceIntent = new Intent(this, TallyDeviceService.class);

        listView = (AbsListView) findViewById(android.R.id.list);

        emptyView = (TextView) findViewById(android.R.id.empty);

        listView.setEmptyView(emptyView);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

    }//end of TallyDeviceScanActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

    }//end of TallyDeviceScanActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of TallyDeviceScanActivity::onResume
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
    // TallyDeviceScanActivity::onItemClick
    //
    // Notifies the active callbacks interface (the activity, if the
    // fragment is attached to one) that an item has been selected.
    //
    // Automatically called when an item is clicked on.
    //

    @Override
    public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pId) {

        handleDeviceClick(deviceNames.get(pPosition));

    }//end of TallyDeviceScanActivity::onItemClick
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
    // TallyDeviceScanActivity::createUiChangeListener
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

    }//end of TallyDeviceScanActivity::createUiChangeListener
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

        Intent intent = new Intent(this, TallyDeviceConnectionStatusMessageActivity.class);
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
    // TallyDeviceScanActivity::handleFinishScanActivityAndStartMessageActivityMessage
    //

    public void handleFinishScanActivityAndStartMessageActivityMessage(Message pMsg) {

        //debug hss//
        Log.d(TAG, "finish and start message received");

        finishActivityAndStartMessageActivity();

    }//end of TallyDeviceScanActivity::handleFinishScanActivityAndStartMessageActivityMessage
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

        setDevices(this, (ArrayList<String>)pMsg.obj);

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
    // TallyDeviceScanActivity::setDevices
    //
    // Sets the list view to the passed in list.
    //

    private void setDevices(Context pContext, ArrayList<String> pNamesList) {

        deviceNames = pNamesList;

        if (deviceNames == null) { return; }

        ListAdapter adapter = new ArrayAdapter<String>(pContext, R.layout.device_list_item,
                                                                                    deviceNames);

        listView.setAdapter(adapter);

    }//end of TallyDeviceScanActivity::setDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::setEmptyText
    //
    // Sets the text of the emptyView to the passed in CharSequence.
    //

    public void setEmptyText(CharSequence pEmptyText) {

        if (emptyView == null) { return; }

        emptyView.setText(pEmptyText);

    }//end of TallyDeviceScanActivity::setEmptyText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::setScanning
    //
    // Sets the scanning state of deviceList and sets the progress bar's visible
    // state depending on the passed in boolean.
    //

    public void setScanning(boolean pScanning) {

        ProgressBar tempBar = (ProgressBar) findViewById(R.id.tallyDeviceScanProgressBar);
        TextView tempText = (TextView) findViewById(R.id.tallyDeviceScanningText);
        View tempHorizontalSpacer = findViewById(R.id.specialHorizontalSpacer);

        if (pScanning) {
            setEmptyText(getString(R.string.empty_view_no_text));
            tempBar.setVisibility(View.VISIBLE);
            tempHorizontalSpacer.setVisibility(View.VISIBLE);
            tempText.setText(R.string.scanning);
            tempText.setVisibility(View.VISIBLE);
        }
        else {
            setEmptyText(getString(R.string.no_devices));
            tempBar.setVisibility(View.GONE);
            tempHorizontalSpacer.setVisibility(View.GONE);
            tempText.setText(R.string.not_scanning);
        }

    }//end of TallyDeviceScanActivity::setScanning
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceScanActivity::startScan
    //
    // Goes through the process of starting a scan for tally devices.
    //

    private void startScan() {

        setDevices(this, null);
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