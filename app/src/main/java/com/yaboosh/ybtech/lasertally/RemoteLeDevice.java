/******************************************************************************
* Title: BluetoothLeClient.java
* Author: Hunter Schoonover
* Create Date: 07/23/14
* Last Edit: 
*
* Purpose:
*
* This class is used for storing a RemoteLeDevice.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
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
	
	private BluetoothDevice remoteDevice;
	public BluetoothDevice getAsBluetoothDevice() { return remoteDevice; }
	
	//Simple getters used to get information from the remote device -- the
	//getters are used instead of extending BluetoothDevice Class because
	//the BluetoothDevice Class is final
	public int describeContents() { return remoteDevice.describeContents(); }
	public String getAddress() { return remoteDevice.getAddress(); }
	public boolean fetchUuidsWithSdp() { return remoteDevice.fetchUuidsWithSdp(); }
	public BluetoothClass getBluetoothClass() { return remoteDevice.getBluetoothClass(); }
	public int getBondState() { return remoteDevice.getBondState(); }
	public Class<? extends BluetoothDevice> getClassFromDevice() { return remoteDevice.getClass(); }
	public String getName() { return remoteDevice.getName(); }
	public int getType() { return remoteDevice.getType(); }
	public ParcelUuid[] getUuids() { return remoteDevice.getUuids(); }
	public int hashCode() { return remoteDevice.hashCode(); }
	public boolean setPin(byte[] pPin) { return remoteDevice.setPin(pPin); }
	public String toString() { return remoteDevice.toString(); }
	
	
	//-----------------------------------------------------------------------------
	// RemoteLeDevice::RemoteLeDevice (constructor)
	//
	
	public RemoteLeDevice(BluetoothDevice pDevice)
	{
		
		remoteDevice = pDevice;
	
	}//end of RemoteLeDevice::RemoteLeDevice (constructor)
	//-----------------------------------------------------------------------------
	
}//end of class RemoteLeDevice
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------