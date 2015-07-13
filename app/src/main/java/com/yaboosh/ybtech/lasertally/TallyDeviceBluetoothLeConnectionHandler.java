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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

public class TallyDeviceBluetoothLeConnectionHandler extends TallyDeviceConnectionHandler
                                                    implements BluetoothAdapter.LeScanCallback {

    public static final String LOG_TAG = "TalDeviceBLeConnHandler";

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
    private String connectedTallyDeviceName = null;
    private String previouslyConnectedTallyDeviceName = null;
    private boolean bleScanTurnedOffForNullDevice = false;
    private boolean reconnectToTallyDevice = false;

    private Runnable noMeasurementReceived = new Runnable () {

        @Override
        public void run() {

            parentService.handleNoDistanceValueReceived();

        }

    };

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

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().contains("DISTO") || bt.getName().contains("disto")) {
                unpairDevice(bt);
            }
        }

        reconnectToTallyDevice = false;

        RemoteLeDevice tempDevice = tallyDevices.get(pDeviceName);
        if (tempDevice == null) { return false; }

        if (scanning) { stopBluetoothLeScan(); }

        connectToTallyDevice(tempDevice.getDevice());

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

        reconnectToTallyDevice = false;

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
    // TallyDeviceBluetoothLeConnectionHandler::sendMeasureCommandToTallyDevice
    //
    // Sends the command for measuring to the connected tally device.
    //

    @Override
    public boolean sendMeasureCommandToTallyDevice() {

        // If a new distance value hasn't been received in the
        // specified amount of time, then a handling function
        // is called in the parentService.
        // This covers the Disto failing to take a measurement.
        handler.postDelayed(noMeasurementReceived, 1000);

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

        if (bluetoothAdapter == null) { bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); }

        if (!bluetoothAdapter.isEnabled()) { bluetoothAdapter.enable(); }
        else { handleBluetoothOn(); }

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

        if (pDevice == null || pDevice.getName() == null) { return; }
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
            if (bondState == BluetoothDevice.BOND_BONDED) {
                handleBondStateBonded();
                context.unregisterReceiver(this);
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

        if (scanning) { stopBluetoothLeScan(); }

        GattCallback gattCallback = new GattCallback(this);
        gatt = pDevice.connectGatt(context, false, gattCallback);
        connectedTallyDeviceName = pDevice.getName();

        return true;

    }//end of TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleBluetoothOn
    //
    // Start a Bluetooth Le scan for tally devices.
    //

    public void handleBluetoothOn() {

        if (startBluetoothLeScan(null)) {

            // bluetooth scan was started successfully
            // -- call function in parentService
            parentService.handleStartScanForTallyDevicesSuccess();

        } else {

            // bluetooth scan was not started successfully
            // -- call function in parentService
            parentService.handleStartScanForTallyDevicesFailure();

        }

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleBluetoothOn
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleBondStateBonded
    //
    // Subscribe to the characteristic in the subscribeStack if the tally device
    // is connected.
    //

    private void handleBondStateBonded() {

        if (!connectedToTallyDevice) { return; }
        subscribeToCharacteristic(subscribeStack.pop(), gatt);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleBondStateBonded
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleCharacteristicChanged
    //
    // Performs different operations depending on the passed in characteristic's
    // UUID.
    //

    public void handleCharacteristicChanged(BluetoothGattCharacteristic pCharacteristic) {

        if (pCharacteristic.getUuid().equals(DISTO_CHARACTERISTIC_DISTANCE)) {

            // The characteristic's UUID matched the distance characteristic

            handler.removeCallbacks(noMeasurementReceived);

            float distance = ByteBuffer.wrap(pCharacteristic.getValue()).order
                                                            (ByteOrder.LITTLE_ENDIAN).getFloat();

            // Convert the value from meters to inches with the proper decimal format
            Double distanceValue = distance * METERS_FEET_CONVERSION_FACTOR;
            String distanceValueString = tallyFormat.format(distanceValue);

            parentService.handleNewDistanceValue(distanceValueString);

        }

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
    // TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteInsufficientAuthentication
    //
    // Add the distance characteristic to the subscribe stack and register a
    // BroadcastReceiver to listen for bonding changes.
    //
    // The distance characteristic is added to the subscribe stack because
    // subscribing to the characteristic was unsuccessful before and needs to be
    // tried again after the tally device and the tablet have bonded.
    //
    //

    public void handleDescriptorWriteInsufficientAuthentication() {

        BluetoothGattService tempBluetoothGattService = gatt.getService(DISTO_SERVICE);

        BluetoothGattCharacteristic distanceChar = tempBluetoothGattService.getCharacteristic
                                                                    (DISTO_CHARACTERISTIC_DISTANCE);
        subscribeStack.add(distanceChar);

        // register broadcast receiver to listen for bonding changes
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(bondingBroadcastReceiver, filter);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteInsufficientAuthentication
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteInsufficientEncryption
    //
    // Calls a handling function in the parent service, indicating that connecting
    // to the tally device failed.
    //

    public void handleDescriptorWriteInsufficientEncryption() {

        parentService.handleConnectToTallyDeviceFailed();

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDescriptorWriteInsufficientEncryption
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

    public void handleDisconnectedFromTallyDevice(BluetoothDevice pDevice) {

        connectedToTallyDevice = false;
        previouslyConnectedTallyDeviceName = connectedTallyDeviceName;
        connectedTallyDeviceName = null;

        //Close the gatt connection
        gatt.close();

        //Call a handling function in parentService
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

        BluetoothGattService tempBluetoothGattService = gatt.getService(DISTO_SERVICE);

        BluetoothGattCharacteristic distanceChar = tempBluetoothGattService.getCharacteristic
                                                                    (DISTO_CHARACTERISTIC_DISTANCE);
        subscribeToCharacteristic(distanceChar, gatt);

    }//end of TallyDeviceBluetoothLeConnectionHandler::handleDiscoverServicesSuccess
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
        try { tempBytes = pCmd.getBytes("UTF-8"); }
        catch(Exception e) { Log.e(LOG_TAG, "Line 678 :: " + e.getMessage()); }

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
    // TallyDeviceBluetoothLeConnectionHandler::unpairDevice
    //
    // Unpairs from the passed in BluetoothDevice.
    //

    private void unpairDevice(BluetoothDevice pDevice) {

        try {
            Method m = pDevice.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(pDevice, (Object[]) null);
        } catch (Exception e) { Log.e(LOG_TAG, "Line 755 :: " + e.getMessage()); }

    }//end of TallyDeviceBluetoothLeConnectionHandler::unpairDevice
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
                parentHandler.handleDisconnectedFromTallyDevice(pGatt.getDevice());
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

        if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {

            parentHandler.handleDescriptorWriteInsufficientAuthentication();

        } else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {

            parentHandler.handleDescriptorWriteInsufficientEncryption();

        } else if (pStatus == BluetoothGatt.GATT_SUCCESS) {

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
