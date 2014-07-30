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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//class BluetoothLeClient
//

public class BluetoothLeClient {

	private Activity parentActivity;
	private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    BluetoothConnectionManager bluetoothConnectionManager = null;
    Handler timerHandler = new Handler();

	private AlertDialog scanningAlertDialog = null;

    ArrayList<String> scanningAlertDialogContent = new ArrayList<String>();
    //used HashMap because it was the simplest implementation of Map
    //and in this case it was not necessary to maintain the order of
    //the map
    private Map<String, String> remoteLeNamesAndAddresses = new HashMap<String, String>();

	private boolean scanningLe = false;
    private boolean leDeviceFound = false;

    private int mode = BluetoothLeVars.MODE0;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

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

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice pDevice, int pRssi, byte[] pScanRecord) {

            parentActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    handleFoundRemoteDevice(pDevice);

                }

            });

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

        //debug hss//
        displayMessageToUser("DEBUG HSS :: User clicked on " + pName, 3000);

        bluetoothConnectionManager = new BluetoothConnectionManager
                                                            (remoteLeNamesAndAddresses.get(pName));
        bluetoothConnectionManager.init();

    }//end of BluetoothLeClient::connectToLeDeviceByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::closeScanningAlertDialog
    //
    // Dismisses the scanningAlertDialog and sets its value to null.
    //

    public void closeScanningAlertDialog()
    {

        scanningAlertDialog.dismiss();
        scanningAlertDialog = null;

    }//end of BluetoothLeClient::closeScanningAlertDialog
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::displayMessageToUser
    //
    // Displays the passed in message to the user for a passed in amount of time.
    //

    public void displayMessageToUser(String pMessage, int pTime)
    {

        AlertDialog.Builder mesBuilder = new AlertDialog.Builder(parentActivity);

        mesBuilder.setMessage(pMessage);

        // Create and show the AlertDialog
        // declared final so that it could be accessed from
        // the timer runnable
        final AlertDialog mesDialog = mesBuilder.create();
        mesDialog.setCanceledOnTouchOutside(false);
        mesDialog.setCancelable(false);
        mesDialog.show();

        // Dismiss the message after a specified time period.
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                mesDialog.dismiss();

            }

        }, pTime);

    }//end of BluetoothLeClient::displayMessageToUser
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
    // Returns the names of all of the remote devices found.
    //

    public String[] getRemoteDevicesNames() {

        Set<String> tempNamesSet = remoteLeNamesAndAddresses.keySet();

        String[] tempDeviceNames = new String[tempNamesSet.size()];
        int i = 0;
        for(String tName : tempNamesSet) {
            //debug hss//
            System.out.println("Device from get names: " + tName.toString());
            tempDeviceNames[i++] = tName.toString();
        }

        return tempDeviceNames;

    }//end of BluetoothLeClient::getRemoteDevicesNames
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::handleActivityDestroy
    //
    // Disconnects from the remote device.
    //

    public void handleActivityDestroy()
    {

        if (bluetoothConnectionManager != null) {
            bluetoothConnectionManager.disconnectLeDevice();
        }

    }//end of BluetoothLeClient::handleActivityDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::handleCancelCloseListItemClick
    //
    // Determines whether to start or stop scanning depending on whether or not
    // the program is currently scanning.
    //

    public void handleCancelScanningListItemClick()
    {

        closeScanningAlertDialog();
        stopLeScan();

    }//end of BluetoothLeClient::handleCancelScanningListItemClick
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

        //debug hss//
        System.out.println("found device");

        if (mode == BluetoothLeVars.MODE0) {

            leDeviceFound = true;

            //debug hss//
            System.out.println("inside of if for mode zero");

            remoteLeNamesAndAddresses.put(pDevice.getName(), pDevice.getAddress());
            updateAndDisplayRemoteDeviceChoiceListDialog();
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
    // BluetoothLeClient::startLeScan
    //
    // Starts the discovery of remote devices acting as servers within the area.
    //

    public void startLeScan()
    {

        leDeviceFound = false;

        // Stops scanning after a pre-defined scan period.
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if (!leDeviceFound && scanningLe) {
                    stopLeScan();
                    closeScanningAlertDialog();
                    displayMessageToUser("Could not find any Disto Laser Measurement devices.",
                                            3000);
                }

                else if (leDeviceFound && scanningLe) {
                    stopLeScan();
                    updateAndDisplayRemoteDeviceChoiceListDialog();
                }

            }

        }, SCAN_PERIOD);

        bluetoothAdapter.startLeScan(leScanCallback);
        scanningLe = true;
        updateAndDisplayRemoteDeviceChoiceListDialog();

    }//end of BluetoothLeClient::startLeScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::stopLeScan
    //
    // Stops the discovery of devices and updates the alertDialog used for scanning.
    //

    public void stopLeScan()
    {

        if (scanningLe) {
            //debug hss//
            System.out.println("stopping Le Scan");
            bluetoothAdapter.stopLeScan(leScanCallback);
            scanningLe = false;
        }

    }//end of BluetoothLeClient::stopLeScan
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

        startLeScan();

	}//end of BluetoothLeClient::startMode0
	//-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothLeClient::turnLaserOn
    //
    // Turns on the Disto laser.
    //

    public void turnLaserOn()
    {

        //debug hss//
        bluetoothConnectionManager.sendCommandToDisto("o");

    }//end of BluetoothLeClient::turnLaserOn
    //-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// BluetoothLeClient::updateAndDisplayRemoteDeviceChoiceListDialog
	//
	// Dismisses the old dialog and then updates and displays a new dialog
	// containing all of the remote Bluetooth devices available for connection in a
	// traditional single-choice list.
	//
	// The user's selection (choice) is also handled here.
	//

	public void updateAndDisplayRemoteDeviceChoiceListDialog()
	{

        scanningAlertDialogContent.clear();
        String[] remoteLeDeviceNames = getRemoteDevicesNames();

        if (scanningAlertDialog != null) { scanningAlertDialog.dismiss(); }

        //Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

        if (scanningLe) {
            builder.setTitle("Select a device (scanning...)");
            scanningAlertDialogContent.add("Touch here to Cancel Scan");
        } else {
            builder.setTitle("Select a device (not scanning)");
            scanningAlertDialogContent.add("Touch here to Cancel");
        }

        if (leDeviceFound) {
            for (int i=0; i<remoteLeDeviceNames.length; i++) {
                scanningAlertDialogContent.add(remoteLeDeviceNames[i]);
                //debug hss//
                System.out.println("Device Name from for loop in dialog update: " + remoteLeDeviceNames[i]);
            }
        }

        System.out.println("Size: " + scanningAlertDialogContent.size());

        // Sets the content and handles the user's selection
        // If the user clicked the item at index 0, a special handling
        // function is called before quitting the onClick() function
        String[] temp = scanningAlertDialogContent.toArray(
                                                    new String[scanningAlertDialogContent.size()]);
		builder.setItems(temp, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface pDialog, int pWhich) {

                stopLeScan();

                if (pWhich == 0) {
                    handleCancelScanningListItemClick();
                    return;
                }

                //debug hss//
                System.out.println("made it past the if statement in onclick");

				// The 'which' argument contains the index position
				// of the selected item
				connectToLeDeviceByName(scanningAlertDialogContent.get(pWhich));

			}

	    });

	    // Create and show the AlertDialog
		scanningAlertDialog = builder.create();
        scanningAlertDialog.setCanceledOnTouchOutside(false);
        scanningAlertDialog.setCancelable(false);
		scanningAlertDialog.show();

    }//end of BluetoothLeClient::updateAndDisplayRemoteDeviceChoiceListDialog
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // class BluetoothLeClient::BluetoothConnectionManager
    //
    // Purpose:
    //
    // This class is used to start and manage a connection to a GATT server
    // hosted on a remote Le device.
    //
    // Gets the device specified by the passed in address and connects to it.
    //

    public class BluetoothConnectionManager {

        private String address;
        private String lastConnectedBluetoothDeviceAddress = null;
        private BluetoothGatt bluetoothGatt;

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::BluetoothConnectionManager (constructor)
        //

        private BluetoothConnectionManager(String pAddress) {

            address = pAddress;

        }//end of BluetoothConnectionManager::BluetoothConnectionManager (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::init
        //

        private void init() {

            connectToLeDevice();

        }//end of BluetoothConnectionManager::init
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::leGattCallback
        //
        // Not really a function.
        //
        // Defines a BluetoothGattCallback that automatically gets called whenever
        // the connection state of what it was assigned to changes.
        //
        // Does actions depending on the different connection states.
        //

        private final BluetoothGattCallback leGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt pGatt, int pStatus, int pNewState) {

                if (pNewState == BluetoothProfile.STATE_CONNECTED) {

                    //debug hss//
                    System.out.println("Connected to device");
                    lastConnectedBluetoothDeviceAddress = address;

                    handleConnectedToLeDevice();

                } else if (pNewState == BluetoothProfile.STATE_DISCONNECTED) {

                    //debug hss//
                    System.out.println("Disconnected to device");

                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt pBluetoothGatt, int pInt) {

                //debug hss//
                System.out.println("Found services");

                BluetoothGattService localBluetoothGattService =
                                    pBluetoothGatt.getService(BluetoothLeVars.DISTO_SERVICE);

                if (localBluetoothGattService == null) {
                    //debug hss//
                    System.out.println("localBluetoothGattService was null from onServicesDiscovered()");
                }

            }

        };//end of BluetoothConnectionManager::leGattCallback
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::connectToLeDevice
        //
        // Extracts the address from the remoteLeDevice object, pairs to the device,
        // and then connects to it and connects to the device using a predefined address.
        //

        public void connectToLeDevice() {

            //debug hss//
            System.out.println("inside of connectToLeDevice");

            final BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(address);

            /*try {
                Class tempClass = Class.forName("android.bluetooth.BluetoothDevice");
                Method createBondMethod = tempClass.getMethod("createBond");
                createBondMethod.invoke(btDevice);
            } catch (Exception e) {}*/

            if (btDevice == null) {
                //debug hss//
                System.out.println("Device not found. Unable to connect.");
                return;
            }

            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            bluetoothGatt = btDevice.connectGatt(parentActivity, false, leGattCallback);

        }// end of BluetoothConnectionManager::connectToLeDevice
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::disconnectLeDevice
        //
        // Disconnects from a connected device by calling the BluetoothGatt close so
        // that the system can release resources appropriately.
        //

        public void disconnectLeDevice() {

            if (bluetoothGatt == null) {
                return;
            }
            bluetoothGatt.close();
            bluetoothGatt = null;

        }// end of BluetoothConnectionManager::disconnectLeDevice
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::handleConnectedToLeDevice
        //
        // Pairs with the connected device.
        //

        public void handleConnectedToLeDevice() {

        }// end of BluetoothConnectionManager::handleConnectedToLeDevice
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // BluetoothConnectionManager::sendCommandToDisto
        //
        // Sends the passed in command to the connected Disto device.
        //

        public void sendCommandToDisto(String pCommand) {

            BluetoothGattService localBluetoothGattService =
                                            bluetoothGatt.getService(BluetoothLeVars.DISTO_SERVICE);

            if (localBluetoothGattService == null) {
                System.out.println("Service was null");
                return;
            }

            BluetoothGattCharacteristic localBluetoothGattCharacteristic =
                                                localBluetoothGattService.getCharacteristic
                                                    (BluetoothLeVars.DISTO_CHARACTERISTIC_COMMAND);

            if (localBluetoothGattCharacteristic == null) {
                System.out.println("Characteristic was null");
                return;
            }

            localBluetoothGattCharacteristic.setValue(pCommand.getBytes());
            bluetoothGatt.writeCharacteristic(localBluetoothGattCharacteristic);


        }// end of BluetoothConnectionManager::sendCommandToDisto
        //-----------------------------------------------------------------------------

    }//end of class BluetoothLeClient::BluetoothConnectionManager
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class BluetoothLeClient
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
