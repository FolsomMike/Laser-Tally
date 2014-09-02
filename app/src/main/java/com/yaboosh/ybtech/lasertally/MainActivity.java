/******************************************************************************
* Title: MainActivity.java
* Author: Hunter Schoonover
* Date: 7/22/14
*
* Purpose:
*
* This class creates the main activity for the application.
* It is created and used upon app startup.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    Button measureConnectButton;
    Button redoButton;

    BluetoothLeClient bluetoothLeClient = null;

    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    private BluetoothLeVars.State state = BluetoothLeVars.State.UNKNOWN;

    final String connectButtonText = "Connect";
    final String measureButtonText = "Measure";

    //-----------------------------------------------------------------------------
    // MainActivity::MainActivity (constructor)
    //

    public MainActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of MainActivity::MainActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of MainActivity onCreate");

        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, BluetoothLeService.class);
        startService(serviceIntent);

    }//end of MainActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of MainActivity onDestroy");

        stopService(serviceIntent);

        super.onDestroy();

    }//end of MainActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of MainActivity onResume");

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }//end of MainActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of MainActivity onPause");

        unbindService(connection);

        if (service == null) {
            Log.d(TAG, "service was null -- return from function");
            return;
        }

        try {
            Message msg = Message.obtain(null, BluetoothLeService.MSG_UNREGISTER_MAIN_ACTIVITY);
            if (msg != null) {
                msg.replyTo = messenger;
                service.send(msg);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering with BleService", e);
            service = null;
        }

    }//end of MainActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::connection
    //
    // Not really a function
    //
    // Creates a new ServiceConnection object and overrides its onServiceConnected()
    // and onServiceDisconnected() functions.
    //

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName pName, IBinder pService) {

            Log.d(TAG, "Service connected to MainActivity");

            service = new Messenger(pService);

            try {

                Message msg = Message.obtain(null, BluetoothLeService.MSG_REGISTER_MAIN_ACTIVITY);
                if (msg != null) {
                    msg.replyTo = messenger;
                    service.send(msg);
                } else {
                    service = null;
                }

            } catch (Exception e) {
                Log.w(TAG, "Error connecting to BleService", e);
                service = null;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "Service disconnected");

            service = null;

        }

    };//end of BluetoothScanActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

    }//end of MainActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onClickListener
    //
    // Not really a function.
    //
    // Listeners for clicks on the objects to which it was handed.
    //
    // Ids are used to determine which object was pressed.
    // When assigning this listener to any new objects, add the object's id to the
    // switch statement and handle the case properly.
    //

    View.OnClickListener onClickListener = new View.OnClickListener() {

        public void onClick(View pV) {

            int id = pV.getId();

            switch (id) {

                case R.id.measureConnectButton:
                    handleMeasureConnectButtonPressed();
                    break;

                case R.id.redoButton:
                    handleRedoButtonPressed();
                    break;

                default:
                    return;

            }

        }

    };//end of MainActivity::onClickListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleDeviceConnected
    //
    // Sets the measureConnectButton to its "measuring" look and text and sets
    // the redo button visible.
    //

    public void handleDeviceConnected() {

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        measureConnectButton.setBackground(getResources().getDrawable
                (R.drawable.blue_styled_button));
        measureConnectButton.setText(measureButtonText);
        measureConnectButton.setOnClickListener(onClickListener);

        redoButton = (Button)findViewById(R.id.redoButton);
        redoButton.setOnClickListener(onClickListener);
        redoButton.setVisibility(View.VISIBLE);

    }//end of MainActivity::handleDeviceConnected
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleDeviceNotConnected
    //
    // Sets the measureConnectButton to its "connecting" look and text and sets
    // the redo button invisible.
    //

    public void handleDeviceNotConnected() {

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        measureConnectButton.setBackground(getResources().getDrawable
                                                                (R.drawable.green_styled_button));
        measureConnectButton.setText(connectButtonText);
        measureConnectButton.setOnClickListener(onClickListener);

        redoButton = (Button)findViewById(R.id.redoButton);
        redoButton.setVisibility(View.INVISIBLE);

    }//end of MainActivity::handleDeviceNotConnected
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleMeasureConnectButtonPressed
    //
    // Calls functions depending on the text of the handleMeasureConnectButton
    // button.
    //

    public void handleMeasureConnectButtonPressed() {

        String btnText = measureConnectButton.getText().toString();

        //debug hss//
        System.out.println("handleMeasureConnectButton was pressed -- text: " + btnText);

        if (btnText == connectButtonText) {
            //stuff to do to connect to device
           startLeScanningProcess();
        }

        else if (btnText == measureButtonText) {
            //hss wip//stuff to do to measure
        }

    }//end of MainActivity::handleMeasureConnectButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleMoreButtonPressed
    //
    // Starts an activity for "More".
    // Should be called from the "More" onClick().
    //

    public void handleMoreButtonPressed(View pView) {

        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);

    }//end of MainActivity::handleMoreButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleNewDistanceValue
    //
    // Creates a new row in the measurementsTable using the passed in value.
    //

    public void handleNewDistanceValue(Float pDistance) {

        TableLayout measurementsTable = (TableLayout)findViewById(R.id.measurementsTable);
        TableRow newRow = new TableRow(getApplicationContext());

        // Add side border
        View sB1 = new View(this);
        sB1.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        sB1.setBackgroundColor(Color.parseColor("#000000"));
        newRow.addView(sB1);

        TextView col1 = new TextView(this);
        col1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        col1.setTextSize(30);
        col1.setTextColor(Color.BLACK);
        String distanceValue = Float.toString(pDistance);
        col1.setText(distanceValue);
        newRow.addView(col1);

        // Add vertical spacer
        View vS1 = new View(this);
        vS1.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        vS1.setBackgroundColor(Color.parseColor("#000000"));
        newRow.addView(vS1);

        TextView col2 = new TextView(this);
        col2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        col2.setTextSize(30);
        col1.setTextColor(Color.BLACK);
        col2.setText("No value");
        newRow.addView(col2);

        // Add vertical spacer
        View vS2 = new View(this);
        vS2.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        vS2.setBackgroundColor(Color.parseColor("#000000"));
        newRow.addView(vS2);

        TextView col3 = new TextView(this);
        col3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        col3.setTextSize(30);
        col1.setTextColor(Color.BLACK);
        col3.setText("No value");
        newRow.addView(col3);

        // Add side border
        View sB2 = new View(this);
        sB2.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        sB2.setBackgroundColor(Color.parseColor("#000000"));
        newRow.addView(sB2);

        measurementsTable.addView(newRow);

    }//end of MainActivity::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleRedoButtonPressed
    //
    // //hss wip//
    //

    private void handleRedoButtonPressed() {

        //hss wip//Code needs to added that redoes the last measurement
        // stored.

    }//end of MainActivity::handleRedoButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleStateUnknown
    //

    private void handleStateUnknown() {

        handleDeviceNotConnected();

    }//end of MainActivity::handleStateUnknown
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::startLeScanningProcess
    //
    // Creates a BluetoothLeClient and initiate it using MODE0.
    //

    private void startLeScanningProcess() {

        Intent intent = new Intent(this, BluetoothScanActivity.class);
        startActivity(intent);

    }//end of MainActivity::startLeScanningProcess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(BluetoothLeVars.State pNewState) {

        Log.d(TAG, "state changed");

        state = pNewState;

        if (state == BluetoothLeVars.State.BLUETOOTH_OFF) {
            Log.d(TAG, "state bluetooth off");
            handleBluetoothOffState();
        }
        else if (state == BluetoothLeVars.State.CONNECTED) {
            Log.d(TAG, "state connected");
            handleDeviceConnected();
        }
        else if (state == BluetoothLeVars.State.IDLE) {
            Log.d(TAG, "state idle");
            handleIdleState();
        }
        else if (state == BluetoothLeVars.State.NOT_CONNECTED) {
            Log.d(TAG, "state not connected");
            handleDeviceNotConnected();
        }
        else if (state == BluetoothLeVars.State.UNKNOWN) {
            Log.d(TAG, "state is unknown");
            handleStateUnknown();
        }
        else {
            Log.d(TAG, "state not listed in switch statement " + state);
        }

    }//end of BluetoothScanActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleBluetoothOffState
    //

    public void handleBluetoothOffState() {

        handleDeviceNotConnected();

    }//end of BluetoothScanActivity::handleBluetoothOffState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::handleIdleState
    //

    public void handleIdleState() {

    }//end of BluetoothScanActivity::handleIdleState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class MainActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<MainActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(MainActivity pActivity) {

            activity = new WeakReference<MainActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            MainActivity tempActivity = activity.get();
            if (tempActivity != null) {

                switch (pMsg.what) {

                    case BluetoothLeService.MSG_DEVICE_CONNECTED:
                        tempActivity.handleDeviceConnected();
                        break;

                    case BluetoothLeService.MSG_DISTANCE_VALUE:
                        Log.d(TAG, "Received distance value message");
                        tempActivity.handleNewDistanceValue((Float)pMsg.obj);
                        break;

                    case BluetoothLeService.MSG_BT_STATE:
                        tempActivity.stateChanged(BluetoothLeVars.State.values()[pMsg.arg1]);
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class MainActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
