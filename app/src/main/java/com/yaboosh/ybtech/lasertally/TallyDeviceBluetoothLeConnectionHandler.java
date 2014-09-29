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
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceBluetoothLeConnectionHandler
//

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TallyDeviceBluetoothLeConnectionHandler extends TallyDeviceConnectionHandler
                                                    implements BluetoothAdapter.LeScanCallback {

    public static final String TAG = "TallyDeviceBluetoothLeConnectionHandler";

    private Context context;
    private BluetoothGatt gatt = null;
    private BluetoothAdapter bluetoothAdapter = null;

    private final Map<String, RemoteLeDevice> tallyDevices = new HashMap<String, RemoteLeDevice>();

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::TallyDeviceBluetoothLeConnectionHandler
    // (constructor)
    //

    public TallyDeviceBluetoothLeConnectionHandler(Context pContext) {

        context = pContext;

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

        //hss wip//

    }//end of TallyDeviceBluetoothLeConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::disconnectFromTallyDevice
    //
    // Disconnects from the connected tally device, if one is connected.
    //

    @Override
    public boolean disconnectFromTallyDevice() {

        //hss wip//

    }//end of TallyDeviceBluetoothLeConnectionHandler::disconnectFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::getConnectedTallyDeviceName
    //
    // Returns the name of the connected tally device.
    //

    @Override
    public String getConnectedTallyDeviceName() {

        //hss wip//

    }//end of TallyDeviceBluetoothLeConnectionHandler::getConnectedTallyDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::sendMeasureCommandToTallyDevice
    //
    // Sends the command for measuring to the connected tally device.
    //

    @Override
    public boolean sendMeasureCommandToTallyDevice() {

        //hss wip//

    }//end of TallyDeviceBluetoothLeConnectionHandler::sendMeasureCommandToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::startScanForTallyDevices
    //
    // Starts a Bluetooth LE scan for tally devices in the area by hosting a GATT
    // server.
    //

    @Override
    public boolean startScanForTallyDevices(long pScanPeriod) {

        tallyDevices.clear();

        if (bluetoothAdapter == null) {
            BluetoothManager bluetoothMgr = (BluetoothManager)
                                                context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothMgr.getAdapter();
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

            // Bluetooth was not enabled -- send request to user
            parentService.startActivityForResult(BluetoothAdapter.ACTION_REQUEST_ENABLE, Keys.ENABLE_BT);

        }
        else {

            // bluetooth was enabled -- start scan
            bluetooth.startLeScan(this);

            // turn off the scan in the passed
            // in amount of time (pScanPeriod)
            timerHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (scanning) {
                        bluetoothAdapter.stopLeScan(TallyDeviceBluetoothLeConnectionHandler.this);

                    }

                }

            }, pScanPeriod);

            setState(BluetoothLeVars.State.SCANNING);
        }

    }//end of TallyDeviceBluetoothLeConnectionHandler::startScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceBluetoothLeConnectionHandler::stopScanForTallyDevices
    //

    @Override
    public boolean stopScanForTallyDevices() {

        return null;

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

        Log.d(TAG, "Found a device");

        //Checks to make sure that pDevice is not null, is already stored,
        //if the device name is null.
        //Then checks to make sure that the device name has the word "DISTO"
        //within it. This is done in a separate if statement after the other
        //conditions have already been checked for because indexOf() throws
        //an exception if the string is not found; the first if statement
        //checks to make sure that the string exists.
        /*//debug hss//if (pDevice == null || pDevice.getName() == null) {
            Log.d(TAG, "Device name did not contain 'DISTO'");
            return;
        }*/

        if (!pDevice.getName().contains("DISTO")) {
            Log.d(TAG, "Device name did not contain 'DISTO'");
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

    }//end of TallyDeviceBluetoothLeConnectionHandler::onLeScan
    //-----------------------------------------------------------------------------

    }// end of class TallyDeviceBluetoothLeConnectionHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
