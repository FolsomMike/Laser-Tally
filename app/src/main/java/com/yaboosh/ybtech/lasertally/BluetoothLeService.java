/******************************************************************************
* Title: BluetoothLeService.java
* Author: Hunter Schoonover
* Create Date: 07/25/14
* Last Edit: 
*
* Purpose:
*
* This class is a service which will enable us to keep all of the Bluetooth
* operations decoupled from the UI while allowing us to update the UI when we
* receive data over BLE.
*
*/

//-----------------------------------------------------------------------------


package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class BluetoothLeService
//

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BluetoothLeService extends Service implements BluetoothAdapter.LeScanCallback {

    public static final String TAG = "BluetoothLeService";
    static final int MSG_REGISTER = 1;
    static final int MSG_UNREGISTER = 2;
    static final int MSG_START_SCAN = 3;
    static final int MSG_STATE_CHANGED = 4;
    static final int MSG_DEVICE_FOUND = 5;
    static final int MSG_DEVICE_CONNECT = 6;
    static final int MSG_DEVICE_DISCONNECT = 7;
    private static final long SCAN_PERIOD = 10000;
    public static final String KEY_MAC_ADDRESSES = "KEY_MAC_ADDRESSES";
    public static final String KEY_NAMES = "KEY_NAMES";

    private final Messenger messenger;
    private final IncomingHandler handler;
    private final List<Messenger> clients = new LinkedList<Messenger>();
    private final Map<String, RemoteLeDevice> devices = new HashMap<String, RemoteLeDevice>();


    public enum State {
        UNKNOWN,
        IDLE,
        SCANNING,
        BLUETOOTH_OFF,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    private BluetoothGatt gatt = null;
    private BluetoothAdapter bluetooth = null;
    private State state = State.UNKNOWN;

    //-----------------------------------------------------------------------------
    // BluetoothLeService::BluetoothLeService (constructor)
    //

    public BluetoothLeService() {

        handler = new IncomingHandler(this);
        messenger = new Messenger(new IncomingHandler(this));

    }//end of BluetoothLeService::BluetoothLeService (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::onBind
    //

    @Override
    public IBinder onBind(Intent pIntent) {

        return messenger.getBinder();

    }//end of BluetoothLeService::onBind
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::onLeScan
    //
    // Checks that the remote device meets certain conditions and then
    // stores the device and its address. The remote device's address is
    // sent in a message to the BluetoothScanActivity.
    //
    // Automatically called when the Le scan discovers a remote device.
    //

    @Override
    public void onLeScan(final BluetoothDevice pDevice, int pRssi, byte[] pScanRecord) {

        //Checks to make sure that pDevice is not null, is already stored,
        //if the device name is null.
        //Then checks to make sure that the device name has the word "DISTO"
        //within it. This is done in a separate if statement after the other
        //conditions have already been checked for because indexOf() throws
        //an exception if the string is not found; the first if statement
        //checks to make sure that the string exists.
        if (pDevice == null || devices.containsValue(pDevice) || pDevice.getName() == null) {
            return;
        }

        if (pDevice.getName().indexOf("DISTO") == -1) {
            return;
        }

        RemoteLeDevice tempDevice = new RemoteLeDevice(pDevice, pDevice.getAddress(),
                                                                                pDevice.getName());
        devices.put(tempDevice.getName(), tempDevice);
        Log.d(TAG, "Added " + tempDevice.getName() + ": " + tempDevice.getAddress());

        Message msg = Message.obtain(null, MSG_DEVICE_FOUND);
        if (msg != null) {
            Bundle bundle = new Bundle();

            String[] tempNames = devices.keySet().toArray(new String[devices.size()]);
            bundle.putStringArray(KEY_NAMES, tempNames);
            //debug hss//
            Log.d(TAG, "Sent " + tempNames[0] + " to BluetoothScanActivity");

            msg.setData(bundle);
            sendMessage(msg);
        }

    }//end of BluetoothLeService::onLeScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::gattCallback
    //
    // Not really a function.
    //
    // Defines a BluetoothGattCallback that automatically gets called whenever
    // the connection state of what it was assigned to changes.
    //
    // Does actions depending on the different connection states.
    //

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt pGatt, int pStatus, int pNewState) {

            super.onConnectionStateChange(pGatt, pStatus, pNewState);

            Log.v(TAG, "Connection State Changed: " +
                    (pNewState == BluetoothProfile.STATE_CONNECTED ? "Connected" : "Disconnected"));

            if (pNewState == BluetoothProfile.STATE_CONNECTED) {
                setState(State.CONNECTED);
            } else {
                setState(State.IDLE);
            }

        }

    };//end of BluetoothLeService::gattCallback
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::connectByName
    //
    // Connects to passed in device using the passed in address.
    //

    public void connect(BluetoothDevice pDevice, String pAddress) {

        if (state == State.SCANNING) {
            bluetooth.stopLeScan(BluetoothLeService.this);
            setState(State.IDLE);
        }

        gatt = pDevice.connectGatt(this, true, gattCallback);

    }//end of BluetoothLeService::connectByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::connectByName
    //
    // Gets the address of the device that corresponds to the passed in name and
    // then uses that address to connect to a remote device.
    //

    public void connectByName(String pName) {

        RemoteLeDevice tempDevice = devices.get(pName);
        if (tempDevice == null) { return; }
        connect(tempDevice.getDevice(), tempDevice.getAddress());

    }//end of BluetoothLeService::connectByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::startScan
    //
    // Goes through the process of starting a Bluetooth Low Energy scan for
    // remote devices hosting a GATT server.
    //

    private void startScan() {

        devices.clear();

        if (bluetooth == null) {
            BluetoothManager bluetoothMgr = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
            bluetooth = bluetoothMgr.getAdapter();
        }

        if (bluetooth == null || !bluetooth.isEnabled()) {
            setState(State.BLUETOOTH_OFF);
        }
        else {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (state == State.SCANNING) {
                        bluetooth.stopLeScan(BluetoothLeService.this);
                        setState(State.IDLE);
                    }

                }

            }, SCAN_PERIOD);

            bluetooth.startLeScan(this);
            setState(State.SCANNING);
        }

    }//end of BluetoothLeService::startScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::setState
    //
    // Sets the state to the state passed in and sends it to contained in message
    // to all the stored clients.
    //

    private void setState(State pNewState) {

        if (state == pNewState) {
            return;
        }

        state = pNewState;

        Message msg = getStateMessage();
        if (msg != null) {
            sendMessage(msg);
        }

    }//end of BluetoothLeService::setState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::getStateMessage
    //
    // Gets and returns a message with a what type of MSG_STATE_CHANGED and the
    // arg1 set the the state.ordinal().
    //

    private Message getStateMessage() {

        Message msg = Message.obtain(null, MSG_STATE_CHANGED);
        if (msg != null) {
            msg.arg1 = state.ordinal();
        }

        return msg;

    }//end of BluetoothLeService::getStateMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::sendMessage
    //
    // Sends the passed in message to all of the stored clients.
    //
    // If the sending of the message to a client failed, that client is removed
    // from the list of clients.
    //

    private void sendMessage(Message pMsg) {

        for (int i = clients.size() - 1; i >= 0; i--) {

            Messenger messenger = clients.get(i);
            if (!sendMessage(messenger, pMsg)) {
                clients.remove(messenger);
            }

        }

    }//end of BluetoothLeService::sendMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::sendMessage
    //
    // Sends the passed in Message to the passed in Messenger.
    //
    // Returns true upon success; returns false upon failure.
    //

    private boolean sendMessage(Messenger pMessenger, Message pMsg) {

        boolean success = true;
        try {
            pMessenger.send(pMsg);
        } catch (RemoteException e) {
            Log.w(TAG, "Lost connection to client", e);
            success = false;
        }
        return success;

    }//end of BluetoothLeService::sendMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class BluetoothLeService::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the mesenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<BluetoothLeService> service;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(BluetoothLeService pService) {

            service = new WeakReference<BluetoothLeService>(pService);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the service is null and performs different operations
        // depending on the Message's "what".
        //

        @Override
        public void handleMessage(Message pMsg) {

            BluetoothLeService tempService = service.get();
            if (tempService != null) {

                switch (pMsg.what) {
                    case MSG_REGISTER:
                        tempService.clients.add(pMsg.replyTo);
                        Log.d(TAG, "Registered");
                        break;

                    case MSG_UNREGISTER:
                        tempService.clients.remove(pMsg.replyTo);
                        Log.d(TAG, "Unregistered");
                        break;

                    case MSG_START_SCAN:
                        tempService.startScan();
                        Log.d(TAG, "Start Scan");
                        break;

                    case MSG_DEVICE_CONNECT:
                        tempService.connectByName((String) pMsg.obj);
                        break;

                    case MSG_DEVICE_DISCONNECT:
                        if (tempService.state == State.CONNECTED && tempService.gatt != null) {
                            tempService.gatt.disconnect();
                        }
                        break;

                    default:
                        super.handleMessage(pMsg);
                }

            }

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class BluetoothLeService::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}// end of class BluetoothLeService
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
