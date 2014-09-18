/******************************************************************************
 * Title: BluetoothScanActivity.java
 * Author: Hunter Schoonover
 * Date: 7/27/14
 *
 * Purpose:
 *
 * This class is an activity used to scan for Bluetooth Le devices. Upon user
 * selection, a remote device is connected to and this activity is closed.
 *
 * The connection is passed back to the MainActivity to be
 * used for interaction.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class BluetoothScanActivity
//

public class BluetoothScanActivity extends Activity implements AbsListView.OnItemClickListener {

    public static final String TAG = "BluetoothScanActivity";

    private View decorView;
    private int uiOptions;

    private final int ENABLE_BT = 1;
    private BluetoothLeVars.State state = BluetoothLeVars.State.UNKNOWN;
    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;

    public static final String KEY_NAMES = "KEY_NAMES";
    private static final String[] KEYS = {KEY_NAMES};
    private static final int[] IDS = {android.R.id.text1};

    private AbsListView listView;
    private TextView emptyView;

    private ListAdapter adapter;

    private String[] deviceNames = null;
    private boolean bluetoothRequestActive = false;

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::BluetoothScanActivity (constructor)
    //

    public BluetoothScanActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of BluetoothScanActivity::BluetoothScanActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of BluetoothScanActivity onCreate");

        setContentView(R.layout.activity_bluetooth_scan);

        this.setFinishOnTouchOutside(false);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        serviceIntent = new Intent(this, BluetoothLeService.class);

        // Set the adapter
        listView = (AbsListView) findViewById(android.R.id.list);

        emptyView = (TextView) findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

    }//end of BluetoothScanActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of BluetoothScanActivity onDestroy");

        super.onDestroy();

        try {
            unbindService(connection);
        } catch (Exception e) {}

        if (service == null) {
            Log.d(TAG, "service was null -- return from function");
            return;
        }

        try {
            Message msg = Message.obtain(null, BluetoothLeService.MSG_UNREGISTER_BLUETOOTH_SCAN_ACTIVITY);
            if (msg != null) {
                Log.d(TAG, "msg was not null -- sending unregister scan message");
                msg.replyTo = messenger;
                service.send(msg);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering with BleService", e);
            service = null;
        }

    }//end of BluetoothScanActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of BluetoothScanActivity onResume");

        decorView.setSystemUiVisibility(uiOptions);

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of BluetoothScanActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        Log.d(TAG, "Inside of BluetoothScanActivity onPause");

        super.onPause();

        unbindService(connection);

        if (service == null) {
            Log.d(TAG, "service was null -- return from function");
            return;
        }

        try {
            Message msg = Message.obtain(null, BluetoothLeService.MSG_UNREGISTER_BLUETOOTH_SCAN_ACTIVITY);
            if (msg != null) {
                Log.d(TAG, "msg was not null -- sending unregister scan message");
                msg.replyTo = messenger;
                service.send(msg);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering with BleService", e);
            service = null;
        }

    }//end of BluetoothScanActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::connection
    //
    // Not really a function
    //
    // Creates a new ServiceConnection object and overrides its onServiceConnected()
    // and onServiceDisconnected() functions.
    //

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName pName, IBinder pService) {

            Log.d(TAG, "Service connected to BluetoothScanActivity");

            service = new Messenger(pService);

            try {

                Message msg = Message.obtain(null,
                                        BluetoothLeService.MSG_REGISTER_BLUETOOTH_SCAN_ACTIVITY);
                if (msg != null) {
                    msg.replyTo = messenger;
                    service.send(msg);
                } else {
                    Log.d(TAG, "service is null");
                    service = null;
                }

            } catch (Exception e) {
                Log.w(TAG, "Error connecting to BleService",
                        e);
                service = null;
            }

            if (service != null) {
                startScan();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "Service disconnected");

            service = null;

        }

    };//end of BluetoothScanActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::createUiChangeListener
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

    }//end of BluetoothScanActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::exitActivity
    //
    // Finishes and closes the activity.
    //

    public void exitActivity() {

        finish();

    }//end of BluetoothScanActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleBluetoothOffState
    //
    // Starts an intent to request for the user to enable the Bluetooth
    // capabilities for this device.
    //

    public void handleBluetoothOffState() {

        //debug hss//
        Log.d(TAG, "made it to handleBluetoothOffState()");

        ProgressBar tempBar = (ProgressBar) findViewById(R.id.bluetoothScanProgressBar);
        TextView tempText = (TextView) findViewById(R.id.bluetoothScanningText);
        View tempHorizontalSpacer = findViewById(R.id.specialHorizontalSpacer);

        setEmptyText(getString(R.string.empty_view_no_text));
        tempBar.setVisibility(View.GONE);
        tempHorizontalSpacer.setVisibility(View.GONE);
        tempText.setText("Waiting for Bluetooth to be turned on.");

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, ENABLE_BT);
        bluetoothRequestActive = true;

    }//end of BluetoothScanActivity::handleBluetoothOffState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleCloseXButtonPressed
    //
    // Closes the activity.
    //

    public void handleCloseXButtonPressed(View pView) {

        exitActivity();

    }//end of BluetoothScanActivity::handleCloseXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleDeviceClick
    //
    // Sends the passed in a message to the BluetoothLeService.
    //

    public void handleDeviceClick(String pName) {

        Log.d(TAG, "User clicked on " + pName);

        Message msg = Message.obtain(null, BluetoothLeService.MSG_DEVICE_CONNECT);
        if (msg == null) { return; }

        msg.obj = pName;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.w(TAG, "Lost connection to service", e);
            unbindService(connection);
        }

        finishActivityAndStartBluetoothMessageActivity();

    }//end of BluetoothScanActivity::handleDeviceClick
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleIdleState
    //
    // Sets the scanning state of deviceList to false and sets the progress bar's
    // visible state to false.
    //

    public void handleIdleState() {

        setScanning(false);

    }//end of BluetoothScanActivity::handleIdleState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::finishActivityAndStartBluetoothMessageActivity
    //
    // Closes this activity and starts the BluetoothMessageActivity.
    //

    private void finishActivityAndStartBluetoothMessageActivity() {

        Intent intent = new Intent(this, BluetoothMessageActivity.class);
        startActivity(intent);

        exitActivity();

    }//end of BluetoothScanActivity::finishActivityAndStartBluetoothMessageActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleScanningState
    //
    // Sets the scanning state of deviceList to true and sets the progress bar's
    // visible state to false.
    //

    public void handleScanningState() {

        setScanning(true);

    }//end of BluetoothScanActivity::handleScanningState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onActivityResult
    //
    // Listens for activity results and performs different actions depending
    // on their request and result codes.
    //

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {

        if (pRequestCode == ENABLE_BT) {

            bluetoothRequestActive = false;

            if (pResultCode == RESULT_OK) {
                startScan();
            }
            else {
                //The user has elected not to turn on
                //Bluetooth. There's nothing we can do
                //without it, so we exit the activity
                exitActivity();
            }
        }
        else {
            super.onActivityResult(pRequestCode, pResultCode, pData);
        }

    }//end of BluetoothScanActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onItemClick
    //
    // Notifies the active callbacks interface (the activity, if the
    // fragment is attached to one) that an item has been selected.
    //
    // Automatically called when an item is clicked on.
    //

    @Override
    public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pId) {

        handleDeviceClick(deviceNames[pPosition]);

    }//end of BluetoothScanActivity::onItemClick
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::setDevices
    //
    // Adds the passed in devices to the listView.
    //

    public void setDevices(Context pContext, String[] pNames) {

        //debug hss//
        Log.d(TAG, "Setting device names -- inside of setDevices()");

        deviceNames = pNames;

        if (deviceNames == null) {
            //debug hss//
            Log.d(TAG, "deviceNames was null -- return");
            return;
        }

        List<Map<String, String>> items = new ArrayList<Map<String, String>>();

        for (String names : deviceNames) {
            Map<String, String> item = new HashMap<String, String>();
            item.put(KEY_NAMES, names);
            items.add(item);
        }

        adapter = new SimpleAdapter(pContext, items, R.layout.device_list_item, KEYS, IDS);

        listView.setAdapter(adapter);

    }//end of BluetoothScanActivity::setDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // DeviceListFragment::setEmptyText
    //
    // Sets the text of the emptyView to the passed in CharSequence.
    //

    public void setEmptyText(CharSequence pEmptyText) {

        if (emptyView == null) {
            return;
        }

        emptyView.setText(pEmptyText);

    }//end of DeviceListFragment::setEmptyText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::setScanning
    //
    // Adds the passed in devices to the listView.
    //

    public void setScanning(boolean pScanning) {

        ProgressBar tempBar = (ProgressBar) findViewById(R.id.bluetoothScanProgressBar);
        TextView tempText = (TextView) findViewById(R.id.bluetoothScanningText);
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

    }//end of BluetoothScanActivity::setScanning
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::startScan
    //
    // Goes through the process of starting a scan for remote devices.
    //

    private void startScan() {

        setDevices(this, null);
        setScanning(true);

        Message msg = Message.obtain(null, BluetoothLeService.MSG_START_SCAN);
        if (msg != null) {
            try {
                service.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(connection);
            }
        }

    }//end of BluetoothScanActivity::startScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(BluetoothLeVars.State pNewState) {

        //debug hss//
        Log.d(TAG, "state changed message received");

        state = pNewState;
        if (state == BluetoothLeVars.State.SCANNING) { handleScanningState(); }
        else if (state == BluetoothLeVars.State.BLUETOOTH_OFF) { handleBluetoothOffState(); }
        else if (state == BluetoothLeVars.State.IDLE) { handleIdleState(); }

    }//end of BluetoothScanActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class MainActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the mesenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<BluetoothScanActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(BluetoothScanActivity pActivity) {

            activity = new WeakReference<BluetoothScanActivity>(pActivity);

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

            BluetoothScanActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {
                    case BluetoothLeService.MSG_BT_STATE:
                        tempActivity.stateChanged(BluetoothLeVars.State.values()[pMsg.arg1]);
                        break;

                    case BluetoothLeService.MSG_DEVICE_FOUND:
                        Bundle data = pMsg.getData();
                        if (data != null && data.containsKey(BluetoothLeService.KEY_NAMES)) {

                            tempActivity.setDevices(tempActivity,
                                                data.getStringArray(BluetoothLeService.KEY_NAMES));

                        }
                        break;

                    case BluetoothLeService.MSG_EXIT_SCAN_ACTIVITY:
                        tempActivity.exitActivity();
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class MainActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class BluetoothScanActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------