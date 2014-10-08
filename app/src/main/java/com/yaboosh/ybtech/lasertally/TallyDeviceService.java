/******************************************************************************
* Title: TallyDeviceService.java
* Author: Hunter Schoonover
* Create Date: 09/28/14
* Last Edit: 
*
* Purpose:
*
* This class is a Service; it runs on the main thread but is kept separate from
* the UI. This class is used to perform connection operations.
*
* The service should only be started once from JobDisplayActivity. Once the
* service is started, activities can bind to it until it is stopped.
*
* Only activities that need access to connection information need to bind with
* the service.
*
*/

//-----------------------------------------------------------------------------


package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDeviceService
//

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TallyDeviceService extends Service {

    public static final String TAG = "TallyDeviceService";
    static final int MSG_REGISTER_JOB_DISPLAY_ACTIVITY = 1;
    static final int MSG_REGISTER_MESSAGE_ACTIVITY = 2;
    static final int MSG_REGISTER_TALLY_DEVICE_SCAN_ACTIVITY = 3;
    static final int MSG_UNREGISTER_JOB_DISPLAY_ACTIVITY = 4;
    static final int MSG_UNREGISTER_MESSAGE_ACTIVITY = 5;
    static final int MSG_UNREGISTER_TALLY_DEVICE_SCAN_ACTIVITY = 6;
    static final int MSG_CONNECT_TO_TALLY_DEVICE = 7;
    static final int MSG_DISCONNECT_FROM_TALLY_DEVICE = 8;
    static final int MSG_START_SCAN_FOR_TALLY_DEVICES = 11;
    static final int MSG_SEND_MEASURE_COMMAND_TO_TALLY_DEVICE = 12;
    static final int MSG_MEASUREMENT_VALUE = 14;
    static final int MSG_CONNECTION_STATE = 15;
    static final int MSG_START_ACTIVITY_FOR_RESULT = 18;
    static final int MSG_ACTIVITY_RESULT = 19;
    static final int MSG_TALLY_DEVICE_NAME = 20;
    static final int MSG_NEW_DISTANCE_VALUE = 21;
    static final int MSG_START_SCAN_FOR_TALLY_DEVICES_FAILED = 22;

    private static final long SCAN_PERIOD = 10000;

    public enum State {
        UNKNOWN,
        IDLE,
        SCANNING,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    Handler timerHandler = new Handler();

    Context context;
    private TallyDeviceConnectionHandler tallyDeviceConnectionHandler = new
                                            TallyDeviceBluetoothLeConnectionHandler(context, this);

    private final Messenger messenger;
    private Messenger messengerClient;

    // The device will always be disconnected at first
    private State connectionState = State.DISCONNECTED;

    private String attemptingConnectionToTallyDeviceName = null;
    private String connectedTallyDeviceName = null;

    //-----------------------------------------------------------------------------
    // TallyDeviceService::TallyDeviceService (constructor)
    //

    public TallyDeviceService() {

        messenger = new Messenger(new IncomingHandler(this));

    }//end of TallyDeviceService::TallyDeviceService (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::onBind
    //

    @Override
    public IBinder onBind(Intent pIntent) {

        return messenger.getBinder();

    }//end of TallyDeviceService::onBind
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::connectToDeviceByName
    //
    // Connects to the device with the passed in name.
    //

    private void connectToDeviceByName(String pDeviceName) {

        attemptingConnectionToTallyDeviceName = pDeviceName;
        tallyDeviceConnectionHandler.connectToTallyDevice(attemptingConnectionToTallyDeviceName);

    }//end of TallyDeviceService::connectToDeviceByName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleActivityResultMessage
    //
    // Send the request code (arg1), the result code (arg2), and the Intent (obj)
    // to the connection handler's handleActivityResult() function.
    //

    public void handleActivityResultMessage(Message pMsg) {

        tallyDeviceConnectionHandler.handleActivityResult(pMsg.arg1, pMsg.arg2, (Intent) pMsg.obj);

    }//end of TallyDeviceService::handleActivityResultMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleConnectedToTallyDevice
    //
    // Sets the state to connected.
    //

    public void handleConnectedToTallyDevice(String pDeviceName) {

        connectedTallyDeviceName = pDeviceName;
        setState(State.CONNECTED);

    }//end of TallyDeviceService::handleConnectedToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleConnectToTallyDeviceFailed
    //
    // Sets the state to disconnected.
    //

    public void handleConnectToTallyDeviceFailed() {

        setState(State.DISCONNECTED);

    }//end of TallyDeviceService::handleConnectToTallyDeviceFailed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleConnectToTallyDeviceMessage
    //
    // Gets the device name from the passed in message and passes the name on to
    // connectToDeviceByName().
    //

    public void handleConnectToTallyDeviceMessage(Message pMsg) {

        connectToDeviceByName((String)pMsg.obj);

    }//end of TallyDeviceService::handleConnectToTallyDeviceMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleDisconnectedFromTallyDevice
    //
    // Sets the state to disconnected.
    //

    public void handleDisconnectedFromTallyDevice() {

        connectedTallyDeviceName = null;
        setState(State.DISCONNECTED);

    }//end of TallyDeviceService::handleDisconnectedFromTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleDisconnectFromTallyDeviceMessage
    //
    // Disconnects from the connected tally device.
    //

    public void handleDisconnectFromTallyDeviceMessage(Message pMsg) {

        if (tallyDeviceConnectionHandler.disconnectFromTallyDevice()) {
            handleDisconnectedFromTallyDevice();
        }

    }//end of TallyDeviceService::handleDisconnectFromTallyDeviceMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleNewDistanceValue
    //
    // Sends the new distance value to the messenger client.
    //

    public void handleNewDistanceValue(String pDistanceValue) {

        Message msg = Message.obtain(null, MSG_NEW_DISTANCE_VALUE);
        if (msg == null) { return; }
        msg.obj = pDistanceValue;
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleRegisterJobDisplayActivity
    //
    // Sets the messenger client to the sender of the passed in message and sends
    // the current connection state to that sender.
    //

    public void handleRegisterJobDisplayActivityMessage(Message pMsg) {

        messengerClient = pMsg.replyTo;

        Message msg = getStateMessage();
        if (msg == null) { return; }
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleRegisterJobDisplayActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleStartScanForTallyDevicesFailure
    //
    // Sends a message to the messenger client indicating that starting the scan
    // failed.
    //

    public void handleStartScanForTallyDevicesFailure() {

        Message msg = Message.obtain(null, MSG_START_SCAN_FOR_TALLY_DEVICES_FAILED);
        if (msg == null) { return; }
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleStartScanForTallyDevicesFailure
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleStartScanForTallyDevicesMessage
    //
    // Instructs the connection handler to start a scan for tally devices.
    // The scan is stopped after the SCAN_PERIOD has passed.
    //

    public void handleStartScanForTallyDevicesMessage(Message pMsg) {

        tallyDeviceConnectionHandler.startScanForTallyDevices();

        // stop the scan in the time specified
        // by the SCAN_PERIOD variable
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if (connectionState == State.SCANNING) {
                    tallyDeviceConnectionHandler.stopScanForTallyDevices();
                }

            }

        }, SCAN_PERIOD);

    }//end of TallyDeviceService::handleStartScanForTallyDevicesMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleStartScanForTallyDevicesSuccess
    //
    // Sets the state to scanning.
    //

    public void handleStartScanForTallyDevicesSuccess() {

        setState(State.SCANNING);

    }//end of TallyDeviceService::handleStartScanForTallyDevicesSuccess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleSendMeasureCommandToTallyDeviceMessage
    //
    // Sends the measure command to the connected tally device.
    //

    public void handleSendMeasureCommandToTallyDeviceMessage(Message pMsg) {

        tallyDeviceConnectionHandler.sendMeasureCommandToTallyDevice();

    }//end of TallyDeviceService::handleSendMeasureCommandToTallyDeviceMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleTallyDeviceFound
    //
    // Sends a message containing the device name to the stored messenger client.
    //

    public void handleTallyDeviceFound(String pDeviceName) {

        Message msg = Message.obtain(null, MSG_TALLY_DEVICE_NAME);
        if (msg == null) { return; }
        msg.obj = pDeviceName;
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleTallyDeviceFound
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleUnregisterJobDisplayActivityMessage
    //
    // Sets the messenger client to null.
    //

    public void handleUnregisterJobDisplayActivityMessage(Message pMsg) {

        messengerClient = null;

    }//end of TallyDeviceService::handleUnregisterJobDisplayActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleRegisterMessageActivityMessage
    //
    // Sets the messenger client to the sender of the passed in message and sends
    // text to the MessageActivity (sender) to be displayed.
    //

    public void handleRegisterMessageActivityMessage(Message pMsg) {

        messengerClient = pMsg.replyTo;

        Message msg = getStateMessage();
        if (msg != null) { sendMessageToMessengerClient(msg); }

        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleRegisterMessageActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleUnregisterMessageActivityMessage
    //
    // Sets the messenger client to null.
    //

    public void handleUnregisterMessageActivityMessage(Message pMsg) {

        messengerClient = null;

    }//end of TallyDeviceService::handleUnregisterMessageActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleRegisterTallyDeviceScanActivityMessage
    //
    // Sets the messenger client to the sender of the passed in message and sends
    // the current connection state to that sender.
    //

    public void handleRegisterTallyDeviceScanActivityMessage(Message pMsg) {

        messengerClient = pMsg.replyTo;

        Message msg = getStateMessage();
        if (msg == null) { return; }
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::handleRegisterTallyDeviceScanActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::handleUnregisterTallyDeviceScanActivityMessage
    //
    // Sets the messenger client to null.
    //

    public void handleUnregisterTallyDeviceScanActivityMessage(Message pMsg) {

        messengerClient = null;

    }//end of TallyDeviceService::handleUnregisterTallyDeviceScanActivityMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::startActivityForResult
    //
    // Sends a message to the messenger client activity instructing to to start
    // an activity for a result using the passed in action and request code.
    //

    public void startActivityForResult(String pAction, int pRequestCode) {

        Message msg = Message.obtain(null, MSG_START_ACTIVITY_FOR_RESULT);
        if (msg == null) { return; }
        msg.obj = pAction;
        msg.arg1 = pRequestCode;
        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::startActivityForResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::setState
    //
    // Sets the connection state to the passed in state and then sends a state
    // message to the stored messenger client.
    //

    private void setState(State pNewState) {

        connectionState = pNewState;

        Message msg = getStateMessage();
        if (msg == null) { return; }

        sendMessageToMessengerClient(msg);

    }//end of TallyDeviceService::setState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::getStateMessage
    //
    // Gets and returns a message with a what type of MSG_STATE_CHANGED and the
    // arg1 set the the state.ordinal().
    //

    private Message getStateMessage() {

        Message msg = Message.obtain(null, MSG_CONNECTION_STATE);
        if (msg == null) { return null; }
        msg.arg1 = connectionState.ordinal();

        // Put the appropriate name in the message
        // if the status is CONNECTED or CONNECTING.
        if (connectionState == State.CONNECTED) {
            msg.obj = connectedTallyDeviceName;
        }
        else if (connectionState == State.CONNECTING) {
            msg.obj = attemptingConnectionToTallyDeviceName;
        }

        return msg;

    }//end of TallyDeviceService::getStateMessage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDeviceService::sendMessageToMessengerClient
    //
    // Sends the passed in message to the stored messenger client.
    //
    // Returns true upon success; returns false upon failure.
    //

    private boolean sendMessageToMessengerClient(Message pMsg) {

        boolean success = true;
        try {
            messengerClient.send(pMsg);
        } catch (RemoteException e) {
            Log.w(TAG, "Lost connection to client", e);
            success = false;
        }
        return success;

    }//end of TallyDeviceService::sendMessageToMessengerClient
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class TallyDeviceService::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<TallyDeviceService> service;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(TallyDeviceService pService) {

            service = new WeakReference<TallyDeviceService>(pService);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the service is null and performs different operations
        // depending on the Message's "what".
        //

        @Override
        public void handleMessage(Message pMsg) {

            Log.d(TAG, "message received");

            TallyDeviceService tempService = service.get();
            if (tempService != null) {

                switch (pMsg.what) {

                    case MSG_ACTIVITY_RESULT:
                        tempService.handleActivityResultMessage(pMsg);
                        break;

                    case MSG_CONNECT_TO_TALLY_DEVICE:
                        tempService.handleConnectToTallyDeviceMessage(pMsg);
                        break;

                    case MSG_DISCONNECT_FROM_TALLY_DEVICE:
                        tempService.handleDisconnectFromTallyDeviceMessage(pMsg);
                        break;

                    case MSG_START_SCAN_FOR_TALLY_DEVICES:
                        tempService.handleStartScanForTallyDevicesMessage(pMsg);
                        break;

                    case MSG_SEND_MEASURE_COMMAND_TO_TALLY_DEVICE:
                        tempService.handleSendMeasureCommandToTallyDeviceMessage(pMsg);

                    case MSG_REGISTER_JOB_DISPLAY_ACTIVITY:
                        tempService.handleRegisterJobDisplayActivityMessage(pMsg);
                        break;

                    case MSG_UNREGISTER_JOB_DISPLAY_ACTIVITY:
                        tempService.handleUnregisterJobDisplayActivityMessage(pMsg);
                        break;

                    case MSG_REGISTER_MESSAGE_ACTIVITY:
                        tempService.handleRegisterMessageActivityMessage(pMsg);
                        break;

                    case MSG_UNREGISTER_MESSAGE_ACTIVITY:
                        tempService.handleUnregisterMessageActivityMessage(pMsg);
                        break;

                    case MSG_REGISTER_TALLY_DEVICE_SCAN_ACTIVITY:
                        tempService.handleRegisterTallyDeviceScanActivityMessage(pMsg);
                        break;

                    case MSG_UNREGISTER_TALLY_DEVICE_SCAN_ACTIVITY:
                        tempService.handleUnregisterTallyDeviceScanActivityMessage(pMsg);
                        break;

                    default:
                        super.handleMessage(pMsg);
                }

            }

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class TallyDeviceService::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}// end of class TallyDeviceService
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
