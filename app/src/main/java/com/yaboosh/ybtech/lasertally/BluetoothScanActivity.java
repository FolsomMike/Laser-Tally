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
    private final int ENABLE_BT = 1;
    private BluetoothLeService.State state = BluetoothLeService.State.UNKNOWN;
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

        setContentView(R.layout.activity_bluetooth_scan);

        this.setFinishOnTouchOutside(false);

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

        super.onDestroy();

    }//end of BluetoothScanActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onStart
    //
    // Automatically called when the activity is started.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onStart() {

        super.onStart();

        startService(serviceIntent);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of BluetoothScanActivity::onStart
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::onStop
    //
    // Automatically called when the activity is stopped.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onStop() {

        super.onStop();

        unbindService(connection);

        if (service == null) {
            //debug hss//
            Log.d(TAG, "service was null -- return from function");
            return;
        }

        //debug hss//
        Log.d(TAG, "Made it past service == null");

        try {
            Message msg = Message.obtain(null,
                    BluetoothLeService.MSG_UNREGISTER);
            if (msg != null) {
                msg.replyTo = messenger;
                service.send(msg);
            }
        } catch (Exception e) {
            Log.w(TAG,
                    "Error unregistering with BleService",
                    e);
            service = null;
        } finally {
            stopService(serviceIntent);
        }

    }//end of BluetoothScanActivity::onStop
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

            //debug hss//
            Log.d(TAG, "Service connected");

            service = new Messenger(pService);

            try {

                Message msg = Message.obtain(null, BluetoothLeService.MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = messenger;
                    service.send(msg);
                } else {
                    //debug hss//
                    Log.d(TAG, "service = null");
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
    // BluetoothScanActivity::handleBluetoothOffState
    //
    // Starts an intent to request for the user to enable the Bluetooth
    // capabilities for this device.
    //

    public void handleBluetoothOffState() {

        ProgressBar tempBar = (ProgressBar) findViewById(R.id.bluetoothScanProgressBar);
        TextView tempText = (TextView) findViewById(R.id.bluetoothScanningText);
        View tempHorizontalSpacer = findViewById(R.id.specialHorizontalSpacer);

        setEmptyText(getString(R.string.empty_view_no_text));
        tempBar.setVisibility(View.GONE);
        tempHorizontalSpacer.setVisibility(View.GONE);
        tempText.setText("Waiting for Bluetooth to be turned on.");

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, ENABLE_BT);

    }//end of BluetoothScanActivity::handleBluetoothOffState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleCloseXButtonPressed
    //
    // Closes the activity.
    //

    public void handleCloseXButtonPressed(View pView) {

        finish();

    }//end of BluetoothScanActivity::handleCloseXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleDeviceClick
    //
    // Sends the passed in a message to the BluetoothLeService.
    //

    public void handleDeviceClick(String pName) {

        Message msg = Message.obtain(null, BluetoothLeService.MSG_DEVICE_CONNECT);
        if (msg == null) { return; }

        msg.obj = pName;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.w(TAG, "Lost connection to service", e);
            unbindService(connection);
        }

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
            if (pResultCode == RESULT_OK) {
                startScan();
            }
            else {
                //The user has elected not to turn on
                //Bluetooth. There's nothing we can do
                //without it, so let's finish().
                finish();
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

    private void stateChanged(BluetoothLeService.State pNewState) {

        state = pNewState;
        switch (state) {
            case SCANNING:
                handleScanningState();
                break;

            case BLUETOOTH_OFF:
                handleBluetoothOffState();
                break;

            case IDLE:
                handleIdleState();
                break;
        }

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

            BluetoothScanActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {
                    case BluetoothLeService.MSG_STATE_CHANGED:
                        tempActivity.stateChanged(BluetoothLeService.State.values()[pMsg.arg1]);
                        break;

                    case BluetoothLeService.MSG_DEVICE_FOUND:
                        Bundle data = pMsg.getData();
                        if (data != null && data.containsKey(BluetoothLeService.KEY_NAMES)) {

                            tempActivity.setDevices(tempActivity,
                                                data.getStringArray(BluetoothLeService.KEY_NAMES));

                        }
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