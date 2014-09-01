/******************************************************************************
* Title: GattCallback.java
* Author: Hunter Schoonover
* Create Date: 08/08/14
* Last Edit: 
*
* Purpose:
*
* This class serves as a GattCallback for whichever GATT an instance
* (of this class) was assigned to when connecting to a remote device.
*
* Different overridden functions are automatically called for several different
* changes concerning the gatt to which it was given.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class GattCallback
//

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.IntentFilter;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GattCallback extends BluetoothGattCallback {

    BluetoothLeService parentService;

    public static final String TAG = "GattCallback";

    //-----------------------------------------------------------------------------
    // GattCallback::GattCallback (constructor)
    //

    public GattCallback(BluetoothLeService pService) {

        parentService = pService;

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

        Log.v(TAG, "Connection State Changed: " +
                (pNewState == BluetoothProfile.STATE_CONNECTED ? "Connected" : "Disconnected"));

        if (pNewState == BluetoothProfile.STATE_CONNECTED) {
            parentService.handleStateConnected();
        }
        else {
            parentService.handleElseConnectionState();
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

        Log.d(TAG, "onServicesDiscovered: " + pStatus);

        if (pStatus != BluetoothGatt.GATT_SUCCESS) { return; }

        parentService.handleServicesDiscoveredSuccess(pGatt.getDevice());

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

        Log.d(TAG, "Characteristic Changed");

        float value = ByteBuffer.wrap(pCharacteristic.getValue()).order
                                                            (ByteOrder.LITTLE_ENDIAN).getFloat();

        parentService.handleCharacteristicChanged(value);

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
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic + ") GATT_SUCCESS");
            parentService.handleCharacteristicWrite();
        }
        else if (pStatus == BluetoothGatt.GATT_FAILURE) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic + ") GATT_FAILURE");
        }
        else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                            ") GATT_INSUFFICIENT_AUTHENTICATION");
        }
        else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_INSUFFICIENT_ENCRYPTION");
        }
        else if (pStatus == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_INVALID_ATTRIBUTE_LENGTH");
        }
        else if (pStatus == BluetoothGatt.GATT_INVALID_OFFSET) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_INVALID_OFFSET");
        }
        else if (pStatus == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_READ_NOT_PERMITTED");
        }
        else if (pStatus == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_REQUEST_NOT_SUPPORTED");
        }
        else if (pStatus == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
            Log.d(TAG, "Writing to characteristic (" + pCharacteristic +
                                                                ") GATT_WRITE_NOT_PERMITTED");
        }

        //debug hss// parentService.handleCharacteristicWrite();

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

        if (pStatus == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_SUCCESS");
            parentService.handleDescriptorWriteSuccess();
        }
        else if (pStatus == BluetoothGatt.GATT_FAILURE) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_FAILURE");
        }
        else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor +
                                                            ") GATT_INSUFFICIENT_AUTHENTICATION");

            System.out.println("Device bond state: " + pGatt.getDevice().getBondState());

            if (pGatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
                Log.d(TAG, "Device not bonded");

                parentService.handleDeviceNotBonded();
            }
        }
        else if (pStatus == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_INSUFFICIENT_ENCRYPTION");
        }
        else if (pStatus == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_INVALID_ATTRIBUTE_LENGTH");
        }
        else if (pStatus == BluetoothGatt.GATT_INVALID_OFFSET) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_INVALID_OFFSET");
        }
        else if (pStatus == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_READ_NOT_PERMITTED");
        }
        else if (pStatus == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_REQUEST_NOT_SUPPORTED");
        }
        else if (pStatus == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
            Log.d(TAG, "Writing to descriptor (" + pDescriptor + ") GATT_WRITE_NOT_PERMITTED");
        }

    }//end of GattCallback::onDescriptorWrite
    //-----------------------------------------------------------------------------

}// end of class GattCallback
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
