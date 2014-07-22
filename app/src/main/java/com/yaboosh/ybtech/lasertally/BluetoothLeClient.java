/******************************************************************************
* Title: BluetoothLeClient.java
* Author: Hunter Schoonover
* Create Date: 06/27/14
* Last Edit: 
*
* Purpose:
*
* This class is used for connecting to a remote device using Bluetooth Smart as
 * a client.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//class BluetoothLeClient
//

public class BluetoothLeClient {
	
	private Activity parentActivity;
	private BluetoothAdapter bluetoothAdapter;
	
	private AlertDialog alert = null;
	
	List<RemoteLeDevice> remoteLeDevices = new ArrayList<RemoteLeDevice>();
    String[] remoteLeDeviceNames;
	
	public boolean searchingForBluetoothDevices = false;
	
	private int mode = BluetoothLeVars.MODE0;
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::BluetoothLeClient (constructor)
	//
	
	public BluetoothLeClient(Activity pActivity)
	{
		
		parentActivity = pActivity;
		
		bluetoothAdapter = getBluetoothAdapter();
	
	}//end of BluetoothLeClient::BluetoothLeClient (constructor)
	//-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::leScanCallback
    //
    // Not really a function.
    //
    // Defines a LeScanCallback to be used to decide what to do when a device is
    // found.
    //
    // Automatically called by the Le scan it was assigned to when the scan discovers a remote le
    // device.
    //

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice pDevice, int pRssi, byte[] pScanRecord) {

            //hss wip// handle bluetotoh devices found
            handleFoundRemoteDevice(pDevice);

        }

    };// BluetoothLeClient::leScanCallback
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::connectToLeDeviceByName
    //
    // Connects to the device found within the remoteLeDevices list that has the
    // same name as the string passed in.
    //

    public void connectToLeDeviceByName(String pName)
    {



    }//end of BluetoothLeClient::connectToLeDeviceByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::discoverRemoteLeDevices
    //
    // Starts the discovery of remote devices acting as servers within the area.
    //

    public void discoverRemoteLeDevices()
    {

        bluetoothAdapter.startLeScan(leScanCallback);
        searchingForBluetoothDevices = true;

    }//end of BluetoothLeClient::discoverRemoteLeDevices
    //-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::getBluetoothAdapter
	//
	// Gets, initializes and returns the default BluetoothAdapter for this device
	// and returns null if the device does not have Bluetooth capabilities
	// (no adapter is found).
	//
	
	public BluetoothAdapter getBluetoothAdapter()
	{

        final BluetoothManager bluetoothManager =
                    (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter tempBA = bluetoothManager.getAdapter();
		
		if (tempBA == null) { return null; }
		
		return tempBA;
	
	}//end of BluetoothLeClient::getBluetoothAdapter
	//-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::getRemoteDevicesNames
    //
    // Returns the names of all of the remote devices found in an ArrayList.
    //

    public String[] getRemoteDevicesNames() {

        String[] deviceNames = new String[remoteLeDevices.size()];

        for(int i=0; i<remoteLeDevices.size(); i++) {

            deviceNames[i] = remoteLeDevices.get(i).getName();

        }

        return deviceNames;

    }//end of BluetoothLeClient::getRemoteDevicesNames
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::handleFoundRemoteDevice
    //
    // Decides how to handle the discovery of a remote device depending on the mode.
    //
    // Should be called when the BroadcastReceiver for ACTION_FOUND finds a device.
    //

    public void handleFoundRemoteDevice(BluetoothDevice pDevice)
    {

        if (mode == BluetoothLeVars.MODE0) {
            RemoteLeDevice rd = new RemoteLeDevice(pDevice);
            remoteLeDevices.add(rd);
        }

    }//end of BluetoothLeClient::handleFoundRemoteDevice
    //-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::initiate
	//
	// Used to start/initiate the Bluetooth process.
	//
	// The mode determines what functions to call in order to start the process.
	//
	
	public void initiate(int pMode)
	{
		
		if (pMode == BluetoothLeVars.MODE0) { startMode0(); }
	
	}//end of BluetoothLeClient::initiate
	//-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::setMode
	//
	// Sets the mode of the program according to the passed in variable.
	//
	
	public void setMode(int pMode)
	{
		
		mode = pMode;
	
	}//end of BluetoothLeClient::setMode
	//-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::startMode0
	//
	// Mode 0 handles everything to do with connecting to a remote Bluetooth
	// device, including displaying the devices in range to the user and allowing
	// him/her to select to a device. 
	//
	// With this mode, all the developer has to do is call the initiate function
	// (e.g. bluetoothClientObject.initiate()). The program then discovers all
	// of the remote BluetoothServers within the area and displays them to the
	// user for selection; only one device can be chosen. 
	//
	// To send data to the remote device:
	//		//hss wip//
	//
	// To read data sent to the local device from the remote device:
	//		//hss wip//
	//
	
	public void startMode0()
	{
		
		//hss wip// needs to do what is specified in function comments
		
		discoverRemoteLeDevices();
	
	}//end of BluetoothLeClient::startMode0
	//-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// BluetoothLeClient::updateAndDisplayRemoteDeviceChoiceListDialog
	//
	// Dismisses the old dialog and then updates and displays a new dialog
	// containing all of theremote Bluetooth devices available for connection in a
	// traditional single-choice list.
	//
	// The user's selection (choice) is also handled here.
	//

	public void updateAndDisplayRemoteDeviceChoiceListDialog()
	{
		
		remoteLeDeviceNames = getRemoteDevicesNames();
		
		if (alert != null) { alert.dismiss(); }
		
		//Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

		builder.setTitle("Select a device (scanning...)");

        // Handle the user's selection
		builder.setItems(remoteLeDeviceNames, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface pDialog, int pWhich) {

				// The 'which' argument contains the index position
				// of the selected item
				connectToLeDeviceByName(remoteLeDeviceNames[pWhich]);
				searchingForBluetoothDevices = false;

			}

	    });
		
	    // Create and show the AlertDialog
		alert = builder.create();
		alert.show();

    }//end of BluetoothLeClient::updateAndDisplayRemoteDeviceChoiceListDialog
    //-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	//-----------------------------------------------------------------------------
	// class BluetoothLeClient::ConnectUsingSecureSocketThread
	//
    // Purpose:
    //
	// This class is used to start a connection to a remote device. A new thread
	// is used to connect because the connect() method is a blocking call.
	//
	// The device to connect to and the UUID are passed in through the constructor.
	// Since an actual UUID is passed in, we can a secure connection to the remote
	// device.
	//
	// If, for any reason, the connection fails or the connect() method times out 
	// (after about 12 seconds), then it will throw an exception.
	//
	
	private class ConnectUsingSecureSocketThread extends Thread {
		
	    private final BluetoothSocket socket;
	    private final BluetoothDevice rDevice;
	 
		//-----------------------------------------------------------------------------
		// ConnectUsingSecureSocketThread::ConnectUsingSecureSocketThread (constructor)
		//
	    
	    public ConnectUsingSecureSocketThread(RemoteLeDevice pDevice, UUID pRemoteUUID) {
	    	
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        rDevice = pDevice.getAsBluetoothDevice();
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            
	            tmp = rDevice.createRfcommSocketToServiceRecord(pRemoteUUID);
	            
	        } catch (IOException e) { System.out.println("IOException: " + e.getMessage()); }
	        
	        socket = tmp;
	        
	    }//end of ConnectUsingSecureSocketThread::ConnectUsingSecureSocketThread (constructor)
		//-----------------------------------------------------------------------------
	 
	    //-----------------------------------------------------------------------------
		// ConnectUsingSecureSocketThread::run
		//
		// This is the thread run code and is used to attempt to establish a connection
	    // to a remote device.
		//
	    
	    public void run() {
	    	
	        // Cancel discovery to speed up the connection
	    	bluetoothAdapter.cancelDiscovery();
	 
	        try {
	            
	        	// Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            socket.connect();
	            
	            System.out.println("Connected to remote device");
	            
	        } catch (IOException e) {
	        	
	        	System.out.println("IOEception: " + e.getMessage());
	        	
	            // Unable to connect; close the socket and get out
	            try {
	                
	            	socket.close();
	            	
	            } catch (IOException cE) { System.out.println("IOEception: " + cE.getMessage()); }
	            
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);//debug hss//
	        
	    }//end of ConnectUsingSecureSocketThread::run
		//-----------------------------------------------------------------------------
	 
	    //-----------------------------------------------------------------------------
		// ConnectUsingSecureSocketThread::cancel
		//
		// Cancels an in-progress connection attempt and closes the socket.
		//
	    
	    public void cancel() {
	    	
	        try {
	            
	        	socket.close();
	        	
	        } catch (IOException e) { System.out.println("IOEception: " + e.getMessage()); }
	        
	    }//end of ConnectUsingSecureSocketThread::cancel
		//-----------------------------------------------------------------------------
	    
	}//end of class BluetoothLeClient::ConnectUsingSecureSocketThread
	//-----------------------------------------------------------------------------
	//-----------------------------------------------------------------------------	

}//end of class BluetoothLeClient
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
