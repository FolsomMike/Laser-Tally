/******************************************************************************
* Title: TallyDeviceSimulationConnectionHandler.java
* Author: Hunter Schoonover
* Create Date: 10/01/14
* Last Edit: 
*
* Purpose:
*
* This class is a child of TallyDeviceConnectionHandler and is used for
* simulating connecting to a laser tally device.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceSimulationConnectionHandler
//

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TallyDeviceSimulationConnectionHandler extends TallyDeviceConnectionHandler {

    private String TAG = "TallyDeviceSimulationConnectionHandler";

    private Handler timerHandler = new Handler();
    private TallyDeviceService parentService;

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");
    private int tallyDeviceNumber = 1;

    private String connectedTallyDeviceName;

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::TallyDeviceSimulationConnectionHandler (constructor)
    //

    public TallyDeviceSimulationConnectionHandler(TallyDeviceService pService) {

        parentService = pService;

    }//end of TallyDeviceSimulationConnectionHandler::TallyDeviceSimulationConnectionHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::connectToTallyDevice
    //
    // Simulates connecting to the device with the passed in name.
    //

    @Override
    public boolean connectToTallyDevice(String pDeviceName) {

        connectedTallyDeviceName = pDeviceName;
        parentService.handleConnectedToTallyDevice(connectedTallyDeviceName);
        return true;

    }//end of TallyDeviceSimulationConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::disconnectFromTallyDevice
    //
    // Simulates connecting to the device with the passed in name.
    //

    @Override
    public boolean disconnectFromTallyDevice() {

        return true;

    }//end of TallyDeviceSimulationConnectionHandler::disconnectFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::getConnectedTallyDeviceName
    //

    @Override
    public String getConnectedTallyDeviceName() {

        return connectedTallyDeviceName;

    }//end of TallyDeviceSimulationConnectionHandler::getConnectedTallyDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::handleActivityResult
    //
    // Simulation has no need to process activities results, so this function is
    // just returned.
    //

    @Override
    public void handleActivityResult(int pRequestCode, int pResultCode, Intent pData) {

        return;

    }//end of TallyDeviceSimulationConnectionHandler::handleActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::sendMeasureCommandToTallyDevice
    //
    // Simulates sending the measure command to the simulated tally device.
    //

    @Override
    public boolean sendMeasureCommandToTallyDevice() {

        parentService.handleNewDistanceValue(simulateNewDistanceValue());

        return true;

    }//end of TallyDeviceSimulationConnectionHandler::sendMeasureCommandToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::startScanForTallyDevices
    //
    // Simulates starting a scan for tally devices.
    //

    @Override
    public void startScanForTallyDevices() {

        parentService.handleStartScanForTallyDevicesSuccess();

        tallyDeviceNumber = 1;

        // Simulates finding a new tally device
        // every second -- up to five devices

        final Timer timer = new Timer();

        TimerTask timerTask = new TimerTask () {
            @Override
            public void run() {

                // Cancels the timer if the tallyDeviceNumber is 5
                if (tallyDeviceNumber >= 5) { timer.cancel(); }
                handleTallyDeviceFound("Sim. Device " + tallyDeviceNumber++);

            }
        };


        timer.schedule(timerTask, 0, 1000);

    }//end of TallyDeviceSimulationConnectionHandler::startScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::stopScanForTallyDevices
    //
    // Since this is a simulation, there is no scan to stop so it just returns true.
    //

    @Override
    public boolean stopScanForTallyDevices() {

        return true;

    }//end of TallyDeviceSimulationConnectionHandler::stopScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::handleTallyDeviceFound
    //
    // Stores the passed in device and its information.
    //

    private void handleTallyDeviceFound(String pDeviceName) {

        parentService.handleTallyDeviceFound(pDeviceName);

    }//end of TallyDeviceSimulationConnectionHandler::handleTallyDeviceFound
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceSimulationConnectionHandler::simulateNewDistanceValue
    //
    // Simulates a new distance value.
    //

    private String simulateNewDistanceValue() {

        Random r = new Random();
        float varianceValue = r.nextInt(12 + 1) + 12;
        float distanceValue = 40 + varianceValue;

        //debug hss//
        Log.d(TAG, "value: " + distanceValue);

        return tallyFormat.format(distanceValue);

    }//end of TallyDeviceSimulationConnectionHandler::simulateNewDistanceValue
    //-----------------------------------------------------------------------------

}// end of class TallyDeviceSimulationConnectionHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------