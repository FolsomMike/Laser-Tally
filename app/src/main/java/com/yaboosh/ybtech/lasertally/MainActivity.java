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
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    Button measureConnectButton;
    Button redoButton;

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");

    BluetoothLeClient bluetoothLeClient = null;

    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    private BluetoothLeVars.State state = BluetoothLeVars.State.UNKNOWN;

    TableLayout measurementsTable;

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

        measurementsTable = (TableLayout)findViewById(R.id.measurementsTable);
        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        redoButton = (Button)findViewById(R.id.redoButton);

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

        handleNewDistanceValue((float)5.05);

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
    // MainActivity::createNewColumn
    //
    // Creates and returns a TextView object with the properties of a column used
    // with the measurement table. The column's text is set to the passed in string.
    //

    public TextView createNewColumn(String pColumnText) {

        TextView col = new TextView(this);
        col.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        col.setText(pColumnText);
        col.setTextColor(Color.BLACK);
        col.setTextSize(30);
        col.setPadding(15, 25, 15, 25);

        return col;

    }//end of MainActivity::createNewColumn
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::createNewColumnDivider
    //
    // Creates and returns a View object with the properties of a column
    // divider used with the measurements table.
    //

    public View createNewColumnDivider() {

        View cd = new View(this);
        cd.setLayoutParams(new TableRow.LayoutParams(2, TableRow.LayoutParams.MATCH_PARENT));
        cd.setBackgroundColor(Color.parseColor("#505050"));

        return cd;

    }//end of MainActivity::createNewColumnDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::createNewRow
    //
    // Creates and returns a View object with the properties of a side border used
    // with the measurements table.
    //

    public TableRow createNewRow(String pCol1Text, String pCol2Text, String pCol3Text) {

        TableRow newRow = new TableRow(getApplicationContext());

        newRow.addView(createNewSideBorder());

        newRow.addView(createNewColumn(pCol1Text));

        newRow.addView(createNewColumnDivider());

        newRow.addView(createNewColumn(pCol2Text));

        newRow.addView(createNewColumnDivider());

        newRow.addView(createNewColumn(pCol3Text));

        newRow.addView(createNewSideBorder());

        return newRow;

    }//end of MainActivity::createNewRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::createNewRowDivider
    //
    // Creates and returns a View object with the properties of a row divider used
    // with the measurements table.
    //

    public View createNewRowDivider() {

        View rd = new View(this);
        rd.setId(R.id.measurementsTableRowDivider);
        rd.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3));
        rd.setBackgroundColor(Color.parseColor("#000000"));

        return rd;

    }//end of MainActivity::createNewRowDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::createNewSideBorder
    //
    // Creates and returns a View object with the properties of a side border used
    // with the measurements table.
    //

    public View createNewSideBorder() {

        View sb = new View(this);
        sb.setLayoutParams(new TableRow.LayoutParams(6, TableRow.LayoutParams.MATCH_PARENT));
        sb.setBackgroundColor(Color.parseColor("#000000"));

        return sb;

    }//end of MainActivity::createNewSideBorder
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleDeviceConnected
    //
    // Sets the measureConnectButton to its "measuring" look and text and sets
    // the redo button visible.
    //

    public void handleDeviceConnected() {

        measureConnectButton.setBackground(getResources().getDrawable
                (R.drawable.blue_styled_button));
        measureConnectButton.setText(measureButtonText);
        measureConnectButton.setOnClickListener(onClickListener);

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
            startMeasuringProcess();
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
    // Creates a new row in the measurementsTable and inserts the appropriate data
    // using the passed in value.
    //

    public void handleNewDistanceValue(Float pDistance) {

        //Convert the value from meters to inches
        Double distanceValue = pDistance * BluetoothLeVars.METERS_FEET_CONVERSION_FACTOR;
        String distanceValueString = tallyFormat.format(distanceValue);

        View measurementsTableBottomBorderLine = findViewById(R.id.measurementsTableBottomBorderLine);

        //zzz

        measurementsTable.removeView(measurementsTableBottomBorderLine);

        measurementsTable.addView(createNewRow(distanceValueString, "No value", "No Value"));
        measurementsTable.addView(createNewRowDivider());

        measurementsTableBottomBorderLine.setVisibility(View.VISIBLE);
        measurementsTable.addView(measurementsTableBottomBorderLine);

        //enable the measureConnect and redo buttons
        //so that the user can use them now that the
        //measuring process has been completed
        setMeasureConnectButtonEnabled(true);
        setRedoButtonEnabled(true);

    }//end of MainActivity::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleRedoButtonPressed
    //
    // Retrieves and removes the final row added to the measurementsTable.
    //

    private void handleRedoButtonPressed() {

        Log.d(TAG, "Redo button pressed");

        int rowCount = 0;
        // HashMap is used to store the number of rows and the position each
        // one is at in the measurementsTable <Integer, Integer> = <RowNumber, RowPosition>
        SparseIntArray rowAndPosition = new SparseIntArray();

        int divCount = 0;
        // HashMap is used to store the number of row dividers and the position each
        // one is at in the measurementsTable <Integer, Integer> = <DivNumber, DivPosition>
        SparseIntArray dividerAndPosition = new SparseIntArray();

        // Determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            if (measurementsTable.getChildAt(i).getId() == R.id.measurementsTableRowDivider) {
                dividerAndPosition.put(++divCount, i);
            }

            // Checks to see if the child at i is a TableRow
            if (!(measurementsTable.getChildAt(i) instanceof TableRow)) { continue; }

            rowAndPosition.put(++rowCount, i);

        }

        if (rowCount == 0) {
            Log.d(TAG, "There were no rows in the measurement table -- return;");
            return;
        }
        // Get the position of the last row found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(rowAndPosition.get(rowCount)));

        if (divCount == 0) {
            Log.d(TAG, "There were no row dividers in the measurement table -- return;");
            return;
        }
        // Get the position of the last row divider found and remove it from the table
        // 1 is subtracted from the divider position because its position has been moved
        // up one since one of the children above it was removed (row)
        measurementsTable.removeView(measurementsTable.getChildAt(dividerAndPosition.get(divCount)-1));

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
    // MainActivity::setMeasureConnectButtonEnabled
    //
    // Enables or disables the measure connect button according to the passed in
    // boolean. The style is also set according to the passed in boolean.
    //

    private void setMeasureConnectButtonEnabled(boolean pBool) {

        measureConnectButton.setEnabled(pBool);

        if (pBool) {
            measureConnectButton.setTextAppearance(getApplicationContext(), R.style.styledButtonWhiteText);
        } else {
            measureConnectButton.setTextAppearance(getApplicationContext(), R.style.disabledStyledButton);
        }

    }//end of MainActivity::disableMeasureConnectButton
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::setRedoButtonEnabled
    //
    // Disables the redo button and sets its text color to the disabled text color.
    //

    private void setRedoButtonEnabled(boolean pBool) {

        redoButton.setEnabled(pBool);

        if (pBool) {
            redoButton.setTextAppearance(getApplicationContext(), R.style.redStyledButton);
        } else {
            redoButton.setTextAppearance(getApplicationContext(), R.style.disabledStyledButton);
        }

    }//end of MainActivity::setRedoButtonEnabled
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
    // MainActivity::startMeasuringProcess
    //
    // Sends a message to the BluetoothLeService to start the measuring process and
    // disables the measure and redo button.
    //

    private void startMeasuringProcess() {

        Message msg = Message.obtain(null, BluetoothLeService.MSG_START_DEVICE_MEASURING_PROCESS);
        if (msg != null) {
            try {
                service.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(connection);
            }
        }

        //disable the measureConnect and redo buttons
        //so that the user can't use them during the
        //measuring process
        setMeasureConnectButtonEnabled(false);
        setRedoButtonEnabled(false);

    }//end of MainActivity::startMeasuringProcess
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // BluetoothScanActivity::stateChanged
    //
    // Performs different operations depending on the passed in state.
    //

    private void stateChanged(BluetoothLeVars.State pNewState) {

        Log.d(TAG, "state changed");

        state = pNewState;

        handleDeviceConnected();

        /*//debug hss//if (state == BluetoothLeVars.State.BLUETOOTH_OFF) {
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
        }*/

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
