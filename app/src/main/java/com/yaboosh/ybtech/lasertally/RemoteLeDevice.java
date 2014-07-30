/******************************************************************************
* Title: RemoteLeDevice.java
* Author: Hunter Schoonover
* Create Date: 07/29/14
* Last Edit: 
*
* Purpose:
*
* This class contains a Bluetooth device and information about it.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class RemoteLeDevice
//

public class RemoteLeDevice {
	
    private String address;
	public String getAddress() { return address; }
    public void setAddress(String pAddress) { address = pAddress; }

    private String name;
    public String getName() { return name; }
    public void setName(String pName) { name = pName; }

    private BluetoothDevice device;
    public BluetoothDevice getDevice() { return device; }
    public void setDevice(BluetoothDevice pDevice) { device = pDevice; }

    //-----------------------------------------------------------------------------
    // RemoteLeDevice::RemoteLeDevice (constructor)
    //

    public RemoteLeDevice(BluetoothDevice pDevice, String pAddress, String pName)
    {

        device = pDevice;
        address = pAddress;
        name = pName;

    }//end of RemoteLeDevice::RemoteLeDevice (constructor)
    //-----------------------------------------------------------------------------
	
}//end of class RemoteLeDevice
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------