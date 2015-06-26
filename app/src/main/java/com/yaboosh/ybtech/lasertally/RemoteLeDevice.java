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

import android.bluetooth.BluetoothDevice;

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