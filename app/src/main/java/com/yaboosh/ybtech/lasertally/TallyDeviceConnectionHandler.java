/******************************************************************************
* Title: TallyDeviceConnectionHandler.java
* Author: Hunter Schoonover
* Create Date: 09/28/14
* Last Edit: 
*
* Purpose:
*
* This class is used for generic connection operations. The operations are
* intended to be overridden by children of the class.
*
* Different children should be created for different connection types
* (Bluetooth Le, simulation, etc.). When defined, the children should be
* of type TallyDeviceConnectionHandler (this class).
* For example:
*       TallyDeviceConnectionHandler handler = new tallyDeviceSimulationHandler();
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceConnectionHandler
//

public class TallyDeviceConnectionHandler {

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::TallyDeviceConnectionHandler (constructor)
    //

    public TallyDeviceConnectionHandler() {

    }//end of TallyDeviceConnectionHandler::TallyDeviceConnectionHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::connectToTallyDevice
    //

    public boolean connectToTallyDevice(String pDeviceName) {

        return false;

    }//end of TallyDeviceConnectionHandler::connectToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::disconnectFromTallyDevice
    //

    public boolean disconnectFromTallyDevice() {

        return false;

    }//end of TallyDeviceConnectionHandler::disconnectFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::getConnectedTallyDeviceName
    //

    public String getConnectedTallyDeviceName() {

        return null;

    }//end of TallyDeviceConnectionHandler::getConnectedTallyDeviceName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::sendMeasureCommandToTallyDevice
    //

    public boolean sendMeasureCommandToTallyDevice() {

        return false;

    }//end of TallyDeviceConnectionHandler::sendMeasureCommandToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::startScanForTallyDevices
    //

    public boolean startScanForTallyDevices() {

        return false;

    }//end of TallyDeviceConnectionHandler::startScanForTallyDevices
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceConnectionHandler::stopScanForTallyDevices
    //

    public boolean stopScanForTallyDevices() {

        return false;

    }//end of TallyDeviceConnectionHandler::stopScanForTallyDevices
    //-----------------------------------------------------------------------------

    }// end of class TallyDeviceConnectionHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
