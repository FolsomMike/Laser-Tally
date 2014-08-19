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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
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
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothLeService extends Service implements BluetoothAdapter.LeScanCallback {

    public static final String TAG = "BluetoothLeService";
    static final int MSG_REGISTER = 1;
    static final int MSG_UNREGISTER = 2;
    static final int MSG_START_SCAN = 3;
    static final int MSG_STATE_CHANGED = 4;
    static final int MSG_DEVICE_FOUND = 5;
    static final int MSG_DEVICE_CONNECT = 6;
    static final int MSG_DEVICE_DISCONNECT = 7;
    static final int MSG_APP_EXITING = 8;
    private static final long SCAN_PERIOD = 10000;
    public static final String KEY_MAC_ADDRESSES = "KEY_MAC_ADDRESSES";
    public static final String KEY_NAMES = "KEY_NAMES";

    private final Messenger messenger;
    private final IncomingHandler handler;
    Handler timerHandler = new Handler();
    private final List<Messenger> clients = new LinkedList<Messenger>();
    private final Map<String, RemoteLeDevice> devices = new HashMap<String, RemoteLeDevice>();
    private Stack subscribeStack = new Stack();

    //QUEUE COMMANDS//
    private static final String NO_NEW_COMMAND = "NONEWCOMMAND";
    private static final String SUBSCRIBE_TO_DISTANCE_CHAR = "SUBSCRIBETODISTANCECHAR";
    private static final String SUBSCRIBE_TO_DISTANCE_DISPLAY_UNIT_CHAR =
                                                        "SUBSCRIBETODISTANCEDISPLAYUNITCHAR";
    private static final String SUBSCRIBE_TO_INCLINATION_CHAR = "SUBSCRIBETOINCLINATIONCHAR";
    private static final String SUBSCRIBE_TO_INCLINATION_DISPLAY_UNIT_CHAR =
                                                        "SUBSCRIBETOINCLINATIONDISPLAYUNITCHAR";
    private static final String TURN_LASER_ON = "TURNLASERON";
    //END OF QUEUE COMMANDS//

    /*//debug hss//private static final Queue<String> queue = new ConcurrentLinkedQueue<String>();*/
    private boolean writing = false;
    private static final Queue<Object> writeQueue = new ConcurrentLinkedQueue<Object>();

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
    // BluetoothLeService::bondingBroadcastReceiver
    //
    // Not really a function.
    //
    // Defines a new BroadcastReceiver object to be used for Bluetooth bonding
    // state changes.
    //
    // Automatically notified/called for several different changes concerning
    // the bonding state of this device to a remote device.
    //
    // In this case, if writing to a descriptor comes back with the gatt status
    // of GATT_INSUFFICIENT_AUTHENTICATION, the Android OS automatically starts
    // bonding with the device to which it was trying to write. To monitor this
    // process, we create an IntentFilter that filters for changes
    // in the bonding state and use it when registering this receiver
    // (bondingBroadcastReceiver). When registering, we pass it this object as
    // the receiver. Once this receiver has been registered, it is used to
    // monitor for different bonding states. Different actions can be  taken
    // for different states.
    //
    //

    private BroadcastReceiver bondingBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context pContext, final Intent pIntent) {

            final BluetoothDevice device = pIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int bondState = pIntent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = pIntent.getIntExtra
                                                    (BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            Log.d(TAG, "Bond state changed for: " + device.getAddress() + " new state: " +
                                                    bondState + " previous: " + previousBondState);

            // skip other devices
            if (!device.getAddress().equals(gatt.getDevice().getAddress())) { return;}

            if (bondState == BluetoothDevice.BOND_BONDED) {
                //debug hss//subscribe();
                //debug hss//
                sendCommand("o");
                pContext.unregisterReceiver(this);
            }

        }

    };//end of BluetoothLeService::bondingBroadcastReceiver
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::initiateBondingProcessByAttemptingToSubscribe
    //
    // Attempts to subscribe to the the Disto distance characteristic by setting
    // the characteristic notification to true and  writing to the descriptor
    // Failure with writing to the descriptor, giving the gatt status of
    // insufficient authentication, is expected. Upon failure, the Android OS will
    // automatically begin attempting to bond with the remote device.
    //
    // So that we are notified when the bond has been created, we register a
    // BroadcastReceiver that listens for Bluetooth bonding changes.
    //
    // Go to the GattCallback class and search for
    // "handleDescriptorInsufficientAuthenticationStatus" to follow the sequence
    // of events and control what happens when the bonding state changes.
    //

    private void initiateBondingProcessByAttemptingToSubscribe() {

        BluetoothGattService tempService = gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempService == null) { return; }

        BluetoothGattCharacteristic tempChar = tempService.getCharacteristic
                                    (BluetoothLeVars.DISTO_CHARACTERISTIC_DISTANCE);
        if (tempChar == null) { return; }

        BluetoothGattDescriptor tempDes = tempChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
        if (tempDes == null) { return; }

        gatt.setCharacteristicNotification(tempChar, true);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        write(tempDes);

    }//end of BluetoothLeService::initiateBondingProcessByAttemptingToSubscribe
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::write
    //
    // Either calls doWrite() using the passed in object or adds the object to
    // the writeQueue.
    //
    // Calls doWrite() if:
    //      the writeQueue is empty
    //      writing is false
    //
    // Adds the object to the write queue if:
    //      the writeQueue is NOT empty
    //      writing is true
    //

    private synchronized void write(Object pO) {

        if (writeQueue.isEmpty() && !writing) {
            doWrite(pO);
        } /*//debu hss//else {
            Log.d(TAG, "Write queue wasn't empty or program was writing");
            writeQueue.add(pO);
        }*/
        else if (!writeQueue.isEmpty()) {
            Log.d(TAG, "Write queue wasn't empty");
            writeQueue.add(pO);
        }
        else if (writing) {
            Log.d(TAG, "Writing was true");
            writeQueue.add(pO);
        }

    }//end of BluetoothLeService::write
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::nextWrite
    //
    // Calls doWrite(), using the next object in the writeQueue if the writeQueue
    // is not empty and if writing is false.
    //

    private synchronized void nextWrite() {
        if (!writeQueue.isEmpty() && !writing) {
            doWrite(writeQueue.poll());
        }
    }//end of BluetoothLeService::nextWrite
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService:: doWrite
    //
    // Writes the value of the passed in object to the remote device.
    //
    // Use either writeCharacteristic() or writeDescriptor() depending on what
    // the passed in object is an instance of.
    //

    private synchronized void doWrite(Object o) {

        if (o instanceof BluetoothGattCharacteristic) {
            writing = true;
            gatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            writing = true;
            gatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else { return; }

    }//end of BluetoothLeService::doWrite
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::connect
    //
    // Connects to passed in device.
    //

    public void connect(BluetoothDevice pDevice) {

        if (state == State.SCANNING) {
            bluetooth.stopLeScan(BluetoothLeService.this);
            setState(State.IDLE);
        }

        GattCallback gattCallback = new GattCallback(this);
        gatt = pDevice.connectGatt(this, false, gattCallback);

    }//end of BluetoothLeService::connect
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
        connect(tempDevice.getDevice());

    }//end of BluetoothLeService::connectByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleCharacteristicWrite
    //

    public void handleCharacteristicWrite() {

        writing = false;
        nextWrite();

    }//end of BluetoothLeService::handleCharacteristicWrite
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleDescriptorWriteSuccess
    //

    public void handleDescriptorWriteSuccess() {

        //debug hss//
        sendCommand("o");
        //debug hss//nextWrite();

    }//end of BluetoothLeService::handleDescriptorWriteSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleDeviceNotBonded
    //

    public void handleDeviceNotBonded() {

        //register a BroadcastReceiver to listen for bonding changes
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondingBroadcastReceiver, filter);

    }//end of BluetoothLeService::handleDeviceNotBonded
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleElseConnectionState
    //

    public void handleElseConnectionState() {

        setState(State.IDLE);

    }//end of BluetoothLeService::handleElseConnectionState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleServicesDiscoveredSuccess
    //

    public void handleServicesDiscoveredSuccess() {

        initiateBondingProcessByAttemptingToSubscribe();

    }//end of BluetoothLeService::handleServicesDiscoveredSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::handleStateConnected
    //

    public void handleStateConnected() {

        setState(State.CONNECTED);
        gatt.discoverServices();

    }//end of BluetoothLeService::handleStateConnected
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::subscribe
    //
    // Subscribes to the required characteristics with the expectancy of success.
    //
    // Should only be called from the bondingBroadcastReceiver if this device
    // has bonded with the remote device.
    //

    private void subscribe() {

        subscribeToDistanceChar();
        subscribeToDistanceDisplayUnitChar();
        subscribeToInclinationChar();
        subscribeToInclinationDisplayUnitChar();

    }//end of BluetoothLeService::subscribe
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::subscribeToDistanceChar
    //
    // Registers with the distance characteristic on the Disto device and tells
    // it that we want to receive notifications each time the distance value
    // changes.
    //

    private synchronized void subscribeToDistanceChar() {

        Log.d(TAG, "inside of subscribeToDistanceChar()");

        BluetoothGattService tempServ = gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempServ == null) {
            Log.d(TAG, "subscribeToDistanceCharacteristic() :: DISTO_SERVICE was null");
            return;
        }

        BluetoothGattCharacteristic tempChar = tempServ.getCharacteristic
                                                    (BluetoothLeVars.DISTO_CHARACTERISTIC_DISTANCE);
        if (tempChar == null) {
            Log.d(TAG,
                "subscribeToDistanceCharacteristic() :: DISTO_CHARACTERISTIC_DISTANCE was null");
            return;
        }

        BluetoothGattDescriptor tempDes = tempChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
        if (tempDes == null) {
            Log.d(TAG,
                    "subscribeToDistanceCharacteristic() :: DISTO_DESCRIPTOR was null");
            return;
        }

        gatt.setCharacteristicNotification(tempChar, true);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        write(tempDes);

    }//end of BluetoothLeService::subscribeToDistanceChar
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::subscribeToDistanceDisplayUnitChar
    //
    // Registers with the distance display unit characteristic on the Disto device
    // and tells it that we want to receive notifications each time the
    // characteristic changes.
    //

    private synchronized void subscribeToDistanceDisplayUnitChar() {

        Log.d(TAG, "subscribeToDistanceDisplayUnitChar()");

        BluetoothGattService tempServ = gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempServ == null) {
            Log.d(TAG,
                    "subscribeToDistanceDisplayUnitChar() :: DISTO_SERVICE was null");
            return;
        }

        BluetoothGattCharacteristic tempChar = tempServ.getCharacteristic
                (BluetoothLeVars.DISTO_CHARACTERISTIC_DISTANCE_DISPLAY_UNIT);
        if (tempChar == null) {
            Log.d(TAG, "subscribeToDistanceDisplayUnitChar() :: " +
                                            "DISTO_CHARACTERISTIC_DISTANCE_DISPLAY_UNIT was null");
            return;
        }

        BluetoothGattDescriptor tempDes = tempChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
        if (tempDes == null) {
            Log.d(TAG,
                    "subscribeToDistanceDisplayUnitChar() :: DISTO_DESCRIPTOR was null");
            return;
        }

        gatt.setCharacteristicNotification(tempChar, true);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        write(tempDes);

    }//end of BluetoothLeService::subscribeToDistanceDisplayUnitChar
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::subscribeToInclinationChar
    //
    // Registers with the inclination characteristic on the Disto device
    // and tells it that we want to receive notifications each time the
    // characteristic changes.
    //

    private synchronized void subscribeToInclinationChar() {

        Log.d(TAG, "subscribeToInclinationChar");

        BluetoothGattService tempServ = gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempServ == null) {
            Log.d(TAG,
                    "subscribeToInclinationChar() :: DISTO_SERVICE was null");
            return;
        }

        BluetoothGattCharacteristic tempChar = tempServ.getCharacteristic
                (BluetoothLeVars.DISTO_CHARACTERISTIC_INCLINATION);
        if (tempChar == null) {
            Log.d(TAG, "subscribeToInclinationChar() :: " +
                    "DISTO_CHARACTERISTIC_INCLINATION was null");
            return;
        }

        BluetoothGattDescriptor tempDes = tempChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
        if (tempDes == null) {
            Log.d(TAG,
                    "subscribeToInclinationChar() :: DISTO_DESCRIPTOR was null");
            return;
        }

        gatt.setCharacteristicNotification(tempChar, true);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        write(tempDes);

    }//end of BluetoothLeService::subscribeToInclinationChar
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::subscribeToInclinationDisplayUnitChar
    //
    // Registers with the inclination display unit characteristic on the Disto
    // device and tells it that we want to receive notifications each time the
    // characteristic changes.
    //

    private synchronized void subscribeToInclinationDisplayUnitChar() {

        Log.d(TAG, "subscribeToInclinationDisplayUnitChar()");

        BluetoothGattService tempServ = gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempServ == null) {
            Log.d(TAG,
                    "subscribeToInclinatioDisplayUnitChar() :: DISTO_SERVICE was null");
            return;
        }

        BluetoothGattCharacteristic tempChar = tempServ.getCharacteristic
                (BluetoothLeVars.DISTO_CHARACTERISTIC_INCLINATION_DISPLAY_UNIT);
        if (tempChar == null) {
            Log.d(TAG, "subscribeToInclinatioDisplayUnitChar() :: " +
                    "DISTO_CHARACTERISTIC_INCLINATION_DISPLAY_UNIT was null");
            return;
        }

        BluetoothGattDescriptor tempDes = tempChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
        if (tempDes == null) {
            Log.d(TAG,
                    "subscribeToInclinatioDisplayUnitChar() :: DISTO_DESCRIPTOR was null");
            return;
        }

        gatt.setCharacteristicNotification(tempChar, true);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        write(tempDes);

    }//end of BluetoothLeService::subscribeToInclinationDisplayUnitChar
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::toggleLaser
    //
    // Toggles the laser on and off according to the passed in boolean
    //
    // True turns the laser on.
    // False turns the laser off.
    //

    private synchronized void toggleLaser(boolean pBool) {

        Log.d(TAG, "toggleLaser()");

        if (pBool) { sendCommand(BluetoothLeVars.TURN_LASER_ON_CMD); }
        else { sendCommand(BluetoothLeVars.TURN_LASER_OFF_CMD); }

    }//end of BluetoothLeService::toggleLaser
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeService::sendCommand
    //
    // Sends the passed in string command to the DISTO.
    //

    private synchronized void sendCommand(String pCmd) {

        BluetoothGattService tempBluetoothGattService =
                                                    gatt.getService(BluetoothLeVars.DISTO_SERVICE);
        if (tempBluetoothGattService == null) {
            Log.d("DistoBluetoothService", "writeGattCharacteristic: DistoService null");
            return;
        }

        BluetoothGattCharacteristic tempBluetoothGattCharacteristic = tempBluetoothGattService.
                                    getCharacteristic(BluetoothLeVars.DISTO_CHARACTERISTIC_COMMAND);
        if (tempBluetoothGattCharacteristic == null) {
            Log.d("DistoBluetoothService", "distoCharacteristicCommand null");
            return;
        }

        //debug hss//writing = true;
        //debug hss//byte[] tempBytes = pCmd.getBytes();
        String temp = "o";
        byte[] tempBytes = null;
        try {
            tempBytes = temp.getBytes("UTF-8");
        } catch(Exception e) {}

        if (tempBytes == null) {
            Log.d(TAG, "tempBytes was null");
        }

        tempBluetoothGattCharacteristic.setValue(tempBytes);
        gatt.writeCharacteristic(tempBluetoothGattCharacteristic);

    }
    //end of BluetoothLeService::sendCommand
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
    // class BluetoothLeService::DelayedEnableNotification
    //
    // Purpose:
    //
    // This class is uses a thread separate from the UI thread to enable the
    // notification status for the passed in characteristic found on the passed
    // in gatt.
    //
    // Continues attempting to retrieve the DISTO_DESCRIPTOR until it does not
    // return null.
    //
    // Continues writing until success.
    //

    class DelayedEnableNotification implements Runnable {

        final BluetoothGattCharacteristic classChar;
        final BluetoothGatt classGatt;

        //-----------------------------------------------------------------------------
        // DelayedEnableNotification::DelayedEnableNotification (constructor)
        //

        DelayedEnableNotification(BluetoothGatt pGatt, BluetoothGattCharacteristic pChar) {

            classGatt= pGatt;
            classChar = pChar;

        }// DelayedEnableNotification::DelayedEnableNotification (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // DelayedEnableNotification::run
        //

        public void run() {

            boolean bool = false;

            //debug hss//do {
                BluetoothGattDescriptor tempDes;

                //do {
                    classGatt.setCharacteristicNotification(classChar, true);
                    tempDes = classChar.getDescriptor(BluetoothLeVars.DISTO_DESCRIPTOR);
                //} while (tempDes == null);

                tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                bool = classGatt.writeDescriptor(tempDes);
            //} while (!bool);

        }// DelayedEnableNotification::run
        //-----------------------------------------------------------------------------

    }//end of class BluetoothLeService::DelayedEnableNotification
    //-----------------------------------------------------------------------------
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

                    case MSG_APP_EXITING:
                        if (tempService.gatt == null) { return; }
                        if (tempService.state != State.CONNECTED) { return; }
                        tempService.gatt.disconnect();
                        tempService.gatt.close();
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
