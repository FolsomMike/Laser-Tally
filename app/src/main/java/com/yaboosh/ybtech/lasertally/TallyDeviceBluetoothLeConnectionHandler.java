/******************************************************************************
* Title: TallyDeviceBluetoothLeConnectionHandler.java
* Author: Hunter Schoonover
* Create Date: 09/28/14
* Last Edit: 
*
* Purpose:
*
* This class is a child of TallyDeviceConnectionHandler and is used for
* connecting to a laser tally device via Bluetooth Low Energy.
*
* The connection properties in this class are specifically designed for the
* DISTO Laser Measurement device.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceBluetoothLeConnectionHandler
//

import android.app.Activity;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public class TallyDeviceBluetoothLeConnectionHandler extends TallyDeviceConnectionHandler
                                                    implements BluetoothAdapter.LeScanCallback {

    public static final String TAG = "TallyDeviceBluetoothLeConnectionHandler";

    // Bluetooth Le Variables specific to the Disto //
    private final UUID DISTO_SERVICE = UUID.fromString("3ab10100-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_DISTANCE = UUID.fromString("3ab10101-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_DISTANCE_DISPLAY_UNIT = UUID.fromString("3ab10102-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_INCLINATION = UUID.fromString("3ab10103-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_INCLINATION_DISPLAY_UNIT = UUID.fromString("3ab10104-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION = UUID.fromString("3ab10105-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION_DISTPLAY_UNIT = UUID.fromString("3ab10106-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_HORIZONTAL_INCLINE = UUID.fromString("3ab10107-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_VERTICAL_INCLINE = UUID.fromString("3ab10108-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_COMMAND = UUID.fromString("3ab10109-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_CHARACTERISTIC_STATE_RESPONSE = UUID.fromString("3ab1010A-f831-4395-b29d-570977d5bf94");
    private final UUID DISTO_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final String TURN_LASER_ON_CMD = "o";
    private final String TURN_LASER_OFF_CMD = "p";
    private final String TRIGGER_DISTANCE_MEASUREMENT = "g";
    // End of variables specific to the Disto //

    //Meters to feet conversion factor
    private final Double METERS_FEET_CONVERSION_FACTOR = 3.2808;

    // Standard activity result: operation canceled.
    private final int ACTIVITY_RESULT_CANCELED = 0;
    // Standard activity result: operation succeeded.
    private final int ACTIVITY_RESULT_OK = -1;

    private Handler handler = new Handler();
    private Context context;
    private TallyDeviceService parentService;
    private BluetoothGatt gatt = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Stack<BluetoothGattCharacteristic> subscribeStack = new Stack<BluetoothGattCharacteristic>();

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");

    private Map<String, RemoteLeDevice> tallyDevices = new HashMap<String, RemoteLeDevice>();

    private String nameOfDeviceToSearchFor = null;
    private boolean scanning = false;
    private boolean connectedToTallyDevice = false;
    private boolean attemptReconnectToDevice = true;
    private String connectedTallyDeviceName = null;
    private String previouslyConnectedTallyDeviceName = null;
    private boolean bleScanTurnedOffForNullDevice = false;

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::TallyDeviceBluetoothLeConnectionHandler
    // (constructor)
    //

    public TallyDeviceBluetoothLeConnectionHandler(TallyDeviceService pService) {

        parentService = pService;

    }//end of TallyDeviceBluetoothLeConnectionHandler::TallyDeviceBluetoothLeConnectionHandler
    // (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //
    // Connects to the tally device with the passed in name.
    //

    @Override
    public boolean connectToTallyDevice(String pDeviceName) {

        //debug hss//
        Log.d(TAG, "connectToTallyDevice() pDeviceName = " + pDeviceName);

        attemptReconnectToDevice = true;

        RemoteLeDevice tempDevice = tallyDevices.get(pDeviceName);
        if (tempDevice == null) { return false; }

        if (scanning) { stopBluetoothLeScan(); }

        GattCallback gattCallback = new GattCallback(this);
        gatt = tempDevice.getDevice().connectGatt(context, false, gattCallback);
        connectedTallyDeviceName = pDeviceName;

        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::disconnectFromTallyDevice
    //
    // Disconnects from the connected tally device, if one is connected.
    //

    @Override
    public boolean disconnectFromTallyDevice() {

        attemptReconnectToDevice = false;

        // Still returns true because although this function
        // did not disconnect from the tally device, the device
        // is still no longer connected
        if (!connectedToTallyDevice || gatt == null) { return true; }

        //Disconnect the gatt connection from the remote device
        gatt.disconnect();

        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::disconnectFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::getConnectedTallyDeviceName
    //
    // Returns the name of the connected tally device.
    //

    @Override
    public String getConnectedTallyDeviceName() {

        return connectedTallyDeviceName;

    }//end of TallyDeviceBluetoothLeConnectionHandler::getConnectedTallyDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleActivityResult
    //
    // Performs different actions depending on the performs depending on the passed
    // in request and result codes.
    //

    @Override
    public void handleActivityResult(int pRequestCode, int pResultCode, Intent pData) {

        switch (pRequestCode) {

            case Keys.ACTIVITY_RESULT_ENABLE_BT:
                if (pResultCode == ACTIVITY_RESULT_OK ) {
                    handleBluetoothTurnedOn();
                }
                else if (pResultCode == ACTIVITY_RESULT_CANCELED ) {
                    handleBluetoothTurnedOff();
                }
        }

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::sendMeasureCommandToTallyDevice
    //
    // Sends the command for measuring to the connected tally device.
    //

    @Override
    public boolean sendMeasureCommandToTallyDevice() {

        return sendCommand(TRIGGER_DISTANCE_MEASUREMENT);

    }//end of TallyDeviceBluetoothLeConnectionHandler::sendMeasureCommandToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::setContext
    //
    // Sets the context variable to the passed in Context.
    //
    // Should be called each time a new activity is launched and registers with
    // the TallyDeviceService. (The context changes when the activity does.)
    //
    // NOTE: The context is not set before this function is called.
    //

   @Override
    public void setContext(Context pContext) {

       context = pContext;
       handler = new Handler(context.getMainLooper());

    }//end of TallyDeviceBluetoothLeConnectionHandler::setContext
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::startScanForTallyDevices
    //
    // If the Bluetooth is enabled, a Bluetooth LE scan is started for tally
    // devices in the area by hosting a GATT server.
    //
    // If the Bluetooth is not enabled, a request is sent to the user to enable it.
    //

    @Override
    public void startScanForTallyDevices() {

        if (bluetoothAdapter == null) {

            // bluetoothAdapter was null -- attempt to initialize it
            BluetoothManager bluetoothMgr = (BluetoothManager)
                                                context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothMgr.getAdapter();

        }

        if (!bluetoothAdapter.isEnabled()) { bluetoothAdapter.enable(); }

        if (bluetoothAdapter.isEnabled()) {

            // bluetooth was enabled -- start scan

            if (startBluetoothLeScan(null)) {

                // bluetooth scan was started successfully
                // -- call function in parentService
                parentService.handleStartScanForTallyDevicesSuccess();

            } else {

                // bluetooth scan was not started successfully
                // -- call function in parentService
                parentService.handleStartScanForTallyDevicesFailure();

            }

        }

    }//end of TallyDeviceBluetoothLeConnectionHandler::startScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::stopScanForTallyDevices
    //
    // Stops the Bluetooth LE scan.
    //

    @Override
    public boolean stopScanForTallyDevices() {

        return stopBluetoothLeScan();

    }//end of TallyDeviceBluetoothLeConnectionHandler::stopScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::onLeScan
    //
    // Checks that the remote device meets certain conditions and then
    // stores the device and its address. The remote device's address is
    // sent in a message to the BluetoothScanActivity.
    //
    // Automatically called when the Le scan discovers a remote device.
    //

    @Override
    public void onLeScan(final BluetoothDevice pDevice, int pRssi, byte[] pScanRecord) {

        //debug hss///
        Log.d(TAG, "Device found: " + pDevice.getName());

        if (pDevice == null || pDevice.getName() == null) { handleNullTallyDeviceFound(); return; }
        if (!pDevice.getName().contains("disto") && !pDevice.getName().contains("DISTO")) {
            //the name did not contain disto -- return
            return;
        }

        // Check to see if there was a specific name to search for in the scan and if the
        // name of the device found matches the one to search for.
        if (pDevice.getName().equals(nameOfDeviceToSearchFor)) {
            nameOfDeviceToSearchFor = null;
            connectToTallyDevice(pDevice);
            return;
        }

        handleTallyDeviceFound(pDevice);

    }//end of TallyDeviceBluetoothLeConnectionHandler::onLeScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::bondingBroadcastReceiver
    //
    // Not a function.
    //
    // Creates an object to listen for changes in the bond state on whatever context
    // it was registered in.
    //

    private BroadcastReceiver bondingBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            Log.d(TAG, "Bond state changed:: " + "new state: " + bondState + " previous: " + previousBondState);

            if (bondState == BluetoothDevice.BOND_BONDED) {
                //debug hss//
                Log.d(TAG, "Bond state: BOND_BONDED -- " + bondState);
                sendCommand(TURN_LASER_ON_CMD);
            } else if (bondState == BluetoothDevice.BOND_BONDING) {
                //debug hss//
                Log.d(TAG, "Bond state: BOND_BONDING -- " + bondState);
            } else if (bondState == BluetoothDevice.BOND_NONE) {
                //debug hss//
                Log.d(TAG, "Bond state: BOND_NONE -- " + bondState);
            }

        }

    };//end of TallyDeviceBluetoothLeConnectionHandler::bondingBroadcastReceiver
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //
    // Connects to the passed in tally device.
    //

    private boolean connectToTallyDevice(BluetoothDevice pDevice) {

        if (pDevice == null) { return false; }
        if (scanning) { stopBluetoothLeScan(); }

        GattCallback gattCallback = new GattCallback(this);
        gatt = pDevice.connectGatt(context, false, gattCallback);
        connectedTallyDeviceName = pDevice.getName();

        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleBluetoothTurnedOn
    //
    // Start a BLE scan and and call a handling function in the parentService.
    //

    private void handleBluetoothTurnedOn() {

        if (startBluetoothLeScan(null)) { parentService.handleStartScanForTallyDevicesSuccess(); }

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleBluetoothTurnedOn
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleBluetoothTurnedOff
    //
    // Call a handling function in the parentService.
    //

    private void handleBluetoothTurnedOff() {

        parentService.handleStartScanForTallyDevicesFailure();

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleBluetoothTurnedOff
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleCharacteristicChanged
    //
    // Performs different operations depending on the passed in characteristic's
    // UUID.
    //

    public void handleCharacteristicChanged(BluetoothGattCharacteristic pCharacteristic) {

        //debug hss/
        Log.d(TAG, "Characteristic changed");

        /*if (pCharacteristic.getUuid() == DISTO_CHARACTERISTIC_DISTANCE) {

            // The characteristic's UUID matched the distance characteristic

            float distance = ByteBuffer.wrap(pCharacteristic.getValue()).order
                                                            (ByteOrder.LITTLE_ENDIAN).getFloat();

            // Convert the value from meters to inches with the proper decimal format
            Double distanceValue = distance * METERS_FEET_CONVERSION_FACTOR;
            String distanceValueString = tallyFormat.format(distanceValue);

            parentService.handleNewDistanceValue(distanceValueString);

        }*/

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleCharacteristicChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleCharacteristicWriteSuccess
    //
    // Currently has no functionality.
    //

    public void handleCharacteristicWriteSuccess() {

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleCharacteristicWriteSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleConnectedToTallyDevice
    //
    // Sets the connected boolean to true and calls a handing function in the
    // parentService.
    //

    public void handleConnectedToTallyDevice() {

        connectedToTallyDevice = true;
        gatt.discoverServices();

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleConnectedToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteSuccess
    //
    // If the subscribe stack is empty, call a handling function in the parentService
    // that handles being connected to the tally device.
    //
    // If the subscribe stack is not empty, subscribe to the next characteristic
    // in the stack.
    //

    public void handleDescriptorWriteSuccess() {

        if (subscribeStack.empty()) {
            parentService.handleConnectedToTallyDevice(connectedTallyDeviceName);
            return;
        }

        subscribeToCharacteristic(subscribeStack.pop(), gatt);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleDisconnectedFromTallyDevice
    //
    // Sets the connected boolean to false and calls a handing function in the
    // parentService.
    //

    public void handleDisconnectedFromTallyDevice() {

        connectedToTallyDevice = false;
        previouslyConnectedTallyDeviceName = connectedTallyDeviceName;
        connectedTallyDeviceName = null;

        //Close the gatt connection
        gatt.close();

        // If the disconnect was not initiated on purpose,
        // then attempt to reconnect to the device
        if (attemptReconnectToDevice) {
            reconnectToTallyDevice(previouslyConnectedTallyDeviceName);
            return;
        }

        parentService.handleDisconnectedFromTallyDevice();

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDisconnectedFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleDiscoverServicesFailed
    //
    // Calls a handling function in the parentService.
    //

    public void handleDiscoverServicesFailed() {

        //Disconnect the gatt connection from the remote device
        gatt.disconnect();

        parentService.handleConnectToTallyDeviceFailed();

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDiscoverServicesFailed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleDiscoverServicesSuccess
    //
    // Subscribe to the distance characteristic.
    //

    public void handleDiscoverServicesSuccess() {

        BluetoothGattService tempBluetoothGattService =
                                                    gatt.getService(DISTO_SERVICE);

        BluetoothGattCharacteristic distanceChar = tempBluetoothGattService.getCharacteristic
                                                                    (DISTO_CHARACTERISTIC_DISTANCE);
        subscribeToCharacteristic(distanceChar, gatt);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDiscoverServicesSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleNullTallyDeviceFound
    //
    // Restarts the Bluetooth Low Energy scan, if one is in progress.
    //

    private void handleNullTallyDeviceFound() {

        //debug hss//
        Log.d(TAG, "handleNullTallyDeviceFound() function reached");

        bleScanTurnedOffForNullDevice = true;

        // Stop the bluetooth le scan if one
        // is in progress. Return if one isn't.
        if (scanning) { stopBluetoothLeScan(); }
        else {
            Log.d(TAG, "handleNullTallyDeviceFound() :: scanning was false -- return");
            return;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                if (!bleScanTurnedOffForNullDevice) { return; }
                startBluetoothLeScan(nameOfDeviceToSearchFor);
            }
        }, 1000);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleNullTallyDeviceFound
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleTallyDeviceFound
    //
    // Stores the passed in device and its information.
    //

    private void handleTallyDeviceFound(BluetoothDevice pDevice) {

        // Store the device for later use
        RemoteLeDevice tempDevice = new RemoteLeDevice(pDevice,
                                                        pDevice.getAddress(),
                                                        pDevice.getName());
        tallyDevices.put(tempDevice.getName(), tempDevice);

        parentService.handleTallyDeviceFound(pDevice.getName());

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleTallyDeviceFound
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::reconnectToTallyDevice
    //
    // Reconnects to the tally device with the passed in name, following a series
    // of steps that ensure that the GATT_INSUFFICIENT_ENCRYPTION descriptor write
    // error does not arise.
    //
    // Step 1:
    //      Turn off the Bluetooth
    // Step 2:
    //      After 1 second, turn on the bluetooth
    // Step 3:
    //      Start a BLE scan, searching specifically
    //      for the device with the name passed in to
    //      startBluetoothLeScan().
    // Step 4:
    //      Start a timer that calls a function to
    //      stop the BLE scan in 10 seconds. If the
    //      tally device is not connected when the
    //      scan is stopped, a handling function in
    //      the parent service is called, indicating
    //      that the tally device is disconnected.
    //

    private boolean reconnectToTallyDevice(String pDeviceName) {

        final String deviceName = pDeviceName;

        //Step 1 -- disable Bluetooth
        if (bluetoothAdapter.isEnabled()) { bluetoothAdapter.disable(); }

        //Step 2 -- enable Bluetooth after 1 sec
        handler.postDelayed((new Runnable() {

            @Override
            public void run() {

                //Step 2 -- enable Bluetooth after 1 sec
                if(!bluetoothAdapter.isEnabled()) { bluetoothAdapter.enable(); }

                //Step 3 -- Start BLE scan after 1 sec
                handler.postDelayed((new Runnable() {
                    @Override
                    public void run() {

                        //Step 3 -- Start BLE scan after 1 sec
                        startBluetoothLeScan(deviceName);

                        //Step 4 -- Stop BLE scan after 10 sec
                        handler.postDelayed((new Runnable() {
                            @Override
                            public void run() {
                                //debug hss//
                                Log.d(TAG, "reconnectToTallyDevice :: stop BLE scan reached");
                                bleScanTurnedOffForNullDevice = false;
                                if (scanning) { stopBluetoothLeScan(); }
                                if (!connectedToTallyDevice) {
                                    parentService.handleConnectToTallyDeviceFailed();
                                }
                            }
                        }), 30000);
                    }

                }), 1000);

            }

        }), 1000);

        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::reconnectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::sendCommand
    //
    // Sends the passed in string command to the DISTO.
    //

    private synchronized boolean sendCommand(String pCmd) {

        boolean success = false;

        BluetoothGattService tempBluetoothGattService =gatt.getService(DISTO_SERVICE);
        if (tempBluetoothGattService == null) { success = false; return success; }

        BluetoothGattCharacteristic tempBluetoothGattCharacteristic =
            tempBluetoothGattService.getCharacteristic(DISTO_CHARACTERISTIC_COMMAND);
        if (tempBluetoothGattCharacteristic == null) { success = false; return success; }

        byte[] tempBytes = null;
        try { tempBytes = pCmd.getBytes("UTF-8"); } catch(Exception e) {}

        if (tempBytes == null) { success = false; return success; }

        success = tempBluetoothGattCharacteristic.setValue(tempBytes);
        success = gatt.writeCharacteristic(tempBluetoothGattCharacteristic);

        return success;

    }//end of TallyDeviceBluetoothLeConnectionHandler::sendCommand
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::startBluetoothLeScan
    //
    // Start a Bluetooth Le scan.
    //
    // The passed in string is the name of a device to search for. If a search is
    // not needed, then just pass in null.
    //

    private boolean startBluetoothLeScan(String pNameOfDeviceToSearchFor) {

        nameOfDeviceToSearchFor = pNameOfDeviceToSearchFor;

        tallyDevices.clear();

        scanning = bluetoothAdapter.startLeScan(this);
        return scanning;

    }//end of TallyDeviceBluetoothLeConnectionHandler::startBluetoothLeScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::stopBluetoothLeScan
    //
    // Stops a Bluetooth Le scan if one is in process.
    //

    private boolean stopBluetoothLeScan() {

        if (scanning) { scanning = false; bluetoothAdapter.stopLeScan(this); }
        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::stopBluetoothLeScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::subscribeToCharacteristic
    //
    // Attempts to enable the notification status/subscribe to the passed in
    // characteristic, using the passed in gatt.
    //
    // Returns true upon success; Returns false upon failure.
    //

    private boolean subscribeToCharacteristic(BluetoothGattCharacteristic pChar, BluetoothGatt pGatt) {

        BluetoothGattDescriptor tempDes = pChar.getDescriptor(DISTO_DESCRIPTOR);
        tempDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        pGatt.setCharacteristicNotification(pChar, true);
        return pGatt.writeDescriptor(tempDes);

    }//end of TallyDeviceBluetoothLeConnectionHandler::subscribeToCharacteristic
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class TallyDeviceBluetoothLeConnectionHandler::GattCallback
    //
    // Purpose:
    //
    // This class serves as a customized GattCallback for whichever GATT an
    // instance of this class was assigned to when connecting to a remote device.
    //

    public class GattCallback extends BluetoothGattCallback {

        TallyDeviceBluetoothLeConnectionHandler parentHandler;

        public static final String TAG = "TallyDeviceBluetoothLeConnectionHandler::GattCallback";

        //-----------------------------------------------------------------------------
        // GattCallback::GattCallback (constructor)
        //

        public GattCallback(TallyDeviceBluetoothLeConnectionHandler pHandler) {

            parentHandler = pHandler;

        }//end of GattCallback::GattCallback (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // GattCallback::onConnectionStateChange
        //
        // Performs different actions based on the different connection states.
        //
        // Automatically called when the connection state changes.
        //

        @Override
        public void onConnectionStateChange(BluetoothGatt pGatt, int pStatus, int pNewState) {

            super.onConnectionStateChange(pGatt, pStatus, pNewState);

            if (pNewState == BluetoothProfile.STATE_CONNECTED) {
                parentHandler.handleConnectedToTallyDevice();
            }
            else if (pNewState == BluetoothProfile.STATE_DISCONNECTED) {
                //debug hss//
                Log.d(TAG, "disconnected from tally device");
                parentHandler.handleDisconnectedFromTallyDevice();
            }

        }//end of GattCallback::onConnectionStateChange
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // GattCallback::onServicesDiscovered
        //
        // If discovering the services was successful, calls a function in the parent
        // service that handles when the services have been discovered.
        //
        // Automatically when the services discovered status changes.
        //

        @Override
        public void onServicesDiscovered(BluetoothGatt pGatt, int pStatus) {

            if (pStatus != BluetoothGatt.GATT_SUCCESS) {
                parentHandler.handleDiscoverServicesFailed();
                return;
            }

            parentHandler.handleDiscoverServicesSuccess();

        }//end of GattCallback::onServicesDiscovered
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // GattCallback::onCharacteristicChanged
        //
        // Calls a function in the parent service to read the characteristic.
        //
        // Automatically called when a characteristic is changed.
        //

        @Override
        public void onCharacteristicChanged(BluetoothGatt pGatt,
                                            BluetoothGattCharacteristic pCharacteristic) {

            parentHandler.handleCharacteristicChanged(pCharacteristic);

        }//end of GattCallback::onCharacteristicChanged
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // GattCallback::onCharacteristicWrite
        //
        // Takes different actions depending on the status of the characteristic write.
        //
        // Automatically called when a characteristic is written to.
        //

        @Override
        public void onCharacteristicWrite(BluetoothGatt pGatt,
                                          BluetoothGattCharacteristic pCharacteristic, int pStatus) {

            Log.d(TAG, "Characteristic write: " + pStatus);

            if (pStatus == BluetoothGatt.GATT_SUCCESS) {
                parentHandler.handleCharacteristicWriteSuccess();
            }

        }//end of GattCallback::onCharacteristicWrite
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // GattCallback::onDescriptorWrite
        //
        // Takes different actions depending on the status of the descriptor write.
        //
        // Automatically when a descriptor is written to.
        //

        @Override
        public void onDescriptorWrite(BluetoothGatt pGatt,
                                      BluetoothGattDescriptor pDescriptor,
                                      int pStatus) {

        //debug hss//
        Log.d(TAG, "Descriptor write: " + pStatus);
        if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {

            //debug hss//
            Log.d(TAG, "GATT_INSUFFICIENT_AUTHENTICATION descriptor write");

            // register broadcast receiver to listen for bonding changes
            final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(bondingBroadcastReceiver, filter);

        } else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {

            //debug hss//
            Log.d(TAG, "GATT_INSUFFICIENT_ENCRYPTION descriptor write");
            //debug hss//parentHandler.handleDescriptorWriteInsufficientEncryption();

        } else if (pStatus == BluetoothGatt.GATT_SUCCESS) {

            //debug hss//
            Log.d(TAG, "GATT_SUCCESS descriptor write");
            parentHandler.handleDescriptorWriteSuccess();

        }

        }//end of GattCallback::onDescriptorWrite
        //-----------------------------------------------------------------------------

    }// end of class TallyDeviceBluetoothLeConnectionHandler::GattCallback
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}// end of class TallyDeviceBluetoothLeConnectionHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
