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

import android.app.ActionBar;
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
    public static final int TABLE_ROW_EDITOR_ACTIVITY_RESULT = 1231;
    public static final int JOB_INFO_ACTIVITY_RESULT = 1232;

    private View decorView;
    private int uiOptions;

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

    private float protectorMakeupValue = 0;

    private TableRow lastRowEdited;

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

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        measurementsTable = (TableLayout)findViewById(R.id.measurementsTable);
        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        redoButton = (Button)findViewById(R.id.redoButton);

        serviceIntent = new Intent(this, BluetoothLeService.class);
        startService(serviceIntent);

        handleNewDistanceValue((float)4.04);
        handleNewDistanceValue((float)5.06);
        handleNewDistanceValue((float)3434);
        handleNewDistanceValue((float)5.43);
        handleNewDistanceValue((float)6.54);
        handleNewDistanceValue((float)6.34);
        handleNewDistanceValue((float)6.34);

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

        decorView.setSystemUiVisibility(uiOptions);

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
    // MainActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

        if (pRequestCode == TABLE_ROW_EDITOR_ACTIVITY_RESULT) {

            if (pResultCode == RESULT_OK) {
                handleTableRowEditorActivityResultOk(
                        pData.getStringExtra(TableRowEditorActivity.PIPE_NUMBER_KEY),
                        pData.getStringExtra(TableRowEditorActivity.TOTAL_LENGTH_KEY),
                        pData.getBooleanExtra(TableRowEditorActivity.RENUMBER_ALL_CHECKBOX_KEY, false));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleTableRowEditorActivityResultCancel();
            }
        }
        else if (pRequestCode == JOB_INFO_ACTIVITY_RESULT) {

            if (pResultCode == RESULT_OK) {
                handleJobInfoActivityResultOk(pData.getStringExtra(JobInfoActivity.JOB_KEY),
                            pData.getStringExtra(JobInfoActivity.PROTECTOR_MAKE_UP_ADJUSTMENT_KEY));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleJobInfoActivityResultCancel();
            }
        }
        else {
            super.onActivityResult(pRequestCode, pResultCode, pData);
        }

    }//end of MainActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onWindowFocusChanged
    //
    // Listens for window focus changes.
    //
    // If the activity has focus, the system visibility is set to the uiOptions.
    //


    @Override
    public void onWindowFocusChanged(boolean pHasFocus) {

        super.onWindowFocusChanged(pHasFocus);

        if(pHasFocus) {
            decorView.setSystemUiVisibility(uiOptions);
        }

    }//end of MainActivity::onWindowFocusChanged
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

                case R.id.measurementsTableRow:
                    handleTableRowPressed((TableRow)pV);
                    break;

                case R.id.redoButton:
                    handleRedoButtonPressed();
                    break;

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
        newRow.setId(R.id.measurementsTableRow);
        newRow.setClickable(true);
        newRow.setOnClickListener(onClickListener);

        newRow.addView(createNewSideBorder());

        View pipeNumCol = createNewColumn(pCol1Text);
        pipeNumCol.setId(R.id.measurementsTableColumnPipeNum);
        newRow.addView(pipeNumCol);

        newRow.addView(createNewColumnDivider());

        View actualCol = createNewColumn(pCol2Text);
        actualCol.setId(R.id.measurementsTableColumnActual);
        newRow.addView(actualCol);

        newRow.addView(createNewColumnDivider());

        View adjustedCol = createNewColumn(pCol3Text);
        adjustedCol.setId(R.id.measurementsTableColumnAdjusted);
        newRow.addView(adjustedCol);

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
    // MainActivity::createUiChangeListener
    //
    // Listens for visibility changes in the ui.
    //
    // If the system bars are visible, the system visibility is set to the uiOptions.
    //
    //

    private void createUiChangeListener() {

        decorView.setOnSystemUiVisibilityChangeListener (
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int pVisibility) {

                        if ((pVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                    }

                });

    }//end of MainActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getAdjustedColumnOfRow
    //
    // Returns a pointer for the TextView used for the Adjusted column in the passed
    // in row.
    //

    private TextView getAdjustedColumnOfRow(TableRow pR) {

        TextView adjustedColumn = null;

        // For each child in the row, check its id
        // and see if it is the Adjusted column.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnAdjusted) {
                adjustedColumn = (TextView)pR.getChildAt(i);
                return adjustedColumn;
            }

        }

        return adjustedColumn;

    }//end of MainActivity::getAdjustedColumnOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getAdjustedColValueOfRow
    //
    // Gets the value under the Adjusted column in the passed in row.
    //

    private String getAdjustedColValueOfRow(TableRow pR) {

        String adjusted = "";

        // For each child in the row, check its id
        // and see if it is the Adjusted column.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnAdjusted) {
                TextView tV = (TextView)pR.getChildAt(i);
                adjusted = tV.getText().toString();
            }

        }

        return adjusted;

    }//end of MainActivity::getAdjustedColValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getPositionOfRow
    //
    // Returns the position of the passed in row in the measurementsTable.
    //

    private int getPositionOfRow(TableRow pR) {

        // For each child in the row, get a pointer
        // to the row and compare it to the passed
        // in row.
        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            if (measurementsTable.getChildAt(i) == pR) {
                return i;
            }

        }

        return 0;

    }//end of MainActivity::getChildPositionOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getDividerCountAndPositions
    //
    // Gets the number of dividers and each of their positions in the measurements
    // table and returns them in a SparseIntArray.
    //

    private SparseIntArray getDividerCountAndPositions() {

        SparseIntArray divAndPos = new SparseIntArray();

        int divCount = 0;

        // Determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            // Checks to see if the child at i is a TableRow
            if (measurementsTable.getChildAt(i).getId() == R.id.measurementsTableRowDivider) {
                divAndPos.put(++divCount, i);
            }

        }

        return divAndPos;

    }//end of MainActivity::getDividerCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getPipeNumberColValueOfRow
    //
    // Returns the value under the Pipe # column in the passed in row.
    //

    private String getPipeNumberColValueOfRow(TableRow pR) {

        String pipeNum = "0";

        // For each child in the row, check its id
        // and see if it is the Pipe # column.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnPipeNum) {
                TextView tV = (TextView)pR.getChildAt(i);
                pipeNum = tV.getText().toString();
                Log.d(TAG, "Pipe Number: " + pipeNum);
            }

        }

        return pipeNum;

    }//end of MainActivity::getPipeNumberColValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getRowCountAndPositions
    //
    // Gets the number of rows and each of their positions in the measurements
    // table and returns them in a SparseIntArray.
    //

    private SparseIntArray getRowCountAndPositions() {

        SparseIntArray rowAndPos = new SparseIntArray();

        int rowCount = 0;

        // Determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            // Checks to see if the child at i is a TableRow
            if (!(measurementsTable.getChildAt(i) instanceof TableRow)) { continue; }

            rowAndPos.put(++rowCount, i);

        }

        return rowAndPos;

    }//end of MainActivity::getRowCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::getTotalLengthColValueOfRow
    //
    // Returns the value under the Total Length column in the passed in row.
    //

    private String getTotalLengthColValueOfRow(TableRow pR) {

        String actual = "";

        // For each child in the row, check its id
        // and see if it is the Actual column.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnActual) {
                TextView tV = (TextView)pR.getChildAt(i);
                actual = tV.getText().toString();
            }

        }

        return actual;

    }//end of MainActivity::getTotalLengthColValueOfRow
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
    // MainActivity::handleJobInfoActivityResultOk
    //
    // Sets the job title to the passed in job title and sets the adjusted columns
    // using the passed in protector/makeup adjustment value.
    //

    private void handleJobInfoActivityResultOk(String pJob, String pProtectorMakeupValue) {

        setJobTitle(pJob);
        setAdjustedColumns(Float.parseFloat(pProtectorMakeupValue));

    }//end of MainActivity::handleJobInfoActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleJobInfoActivityResultCancel
    //
    // Currently does nothing.
    //

    private void handleJobInfoActivityResultCancel() {

        //Currently does nothing

    }//end of MainActivity::handleJobInfoActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleJobInfoButtonPressed
    //
    // Starts an activity for Job Info.
    // Should be called from the "Job Info" button onClick().
    //

    public void handleJobInfoButtonPressed(View pView) {

        Intent intent = new Intent(this, JobInfoActivity.class);
        startActivityForResult(intent, JOB_INFO_ACTIVITY_RESULT);

    }//end of MainActivity::handleJobInfoButtonPressed
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

        if (btnText.equals(connectButtonText)) {
            //stuff to do to connect to device
           startLeScanningProcess();
        }

        else if (btnText.equals(measureButtonText)) {
            startMeasuringProcess();
        }

    }//end of MainActivity::handleMeasureConnectButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleTableRowEditorActivityResultOk
    //
    // Sets the pipe number and total length of the last edited row to the passed
    // in values.
    // Also sets the background color of the last edited row back to white.
    //

    private void handleTableRowEditorActivityResultOk(String pPipeNum, String pTotalLength,
                                                    boolean pRenumberAll) {

        lastRowEdited.setBackgroundColor(getResources().getColor(R.color.measurementsTableColor));
        setPipeNumberOfRow(lastRowEdited, pPipeNum);
        setTotalLengthOfRow(lastRowEdited, pTotalLength);

        if (!pRenumberAll) { return; }

        renumberAllAfterRow(lastRowEdited, Integer.parseInt(pPipeNum)+1);

    }//end of MainActivity::handleTableRowEditorActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleTableRowEditorActivityResultCancel
    //
    // Sets the background color of the last edited row back to white.
    //

    private void handleTableRowEditorActivityResultCancel() {

        lastRowEdited.setBackgroundColor(getResources().getColor(R.color.measurementsTableColor));

    }//end of MainActivity::handleTableRowEditorActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleTableRowPressed
    //
    // Gets the values in the Pipe #, Actual, and Adjusted columns and sends the
    // values to the EditPipeRowActivity to be displayed to and edited by the user.
    //

    public void handleTableRowPressed(TableRow pR) {

        Log.d(TAG, "Measurements Table Row pressed");
        lastRowEdited = pR;
        pR.setBackgroundColor(getResources().getColor(R.color.selectedTableRowColor));
        String pipeNum = getPipeNumberColValueOfRow(pR);
        String totalLength = getTotalLengthColValueOfRow(pR);

        Intent intent = new Intent(this, TableRowEditorActivity.class);
        intent.putExtra(TableRowEditorActivity.PIPE_NUMBER_KEY, pipeNum);
        intent.putExtra(TableRowEditorActivity.TOTAL_LENGTH_KEY, totalLength);
        startActivityForResult(intent, TABLE_ROW_EDITOR_ACTIVITY_RESULT);

    }//end of MainActivity::handleTableRowPressed
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

        measurementsTable.removeView(measurementsTableBottomBorderLine);

        // Get the row count and each of their positions
        SparseIntArray rowAndPos = getRowCountAndPositions();
        // Check to see if the row count is greater than 0
        // If it is greater than 0, then pipeNumInt is set
        //  to the pipe number found in the last row. If
        // not, then the pipeNumInt remains equal to 0.
        int pipeNumInt = rowAndPos.size();
        Log.d(TAG, "row and pos size :: " + rowAndPos.size());
        if (pipeNumInt > 0) {
            TableRow tR = (TableRow) measurementsTable.getChildAt(rowAndPos.get(rowAndPos.size()));
            pipeNumInt = Integer.parseInt(getPipeNumberColValueOfRow(tR));
        }
        Log.d(TAG, "pipeNumInt: " + pipeNumInt);
        pipeNumInt = pipeNumInt + 1;
        String pipeNumString = Integer.toString(pipeNumInt);

        measurementsTable.addView(createNewRow(pipeNumString, distanceValueString, "No Value"));
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

        // HashMap is used to store the number of rows and the position each
        // one is at in the measurementsTable <Integer, Integer> = <RowNumber, RowPosition>
        SparseIntArray rowAndPosition = getRowCountAndPositions();
        int rowCount = rowAndPosition.size();
        if (rowCount == 0) {
            Log.d(TAG, "There were no rows in the measurement table -- return;");
            return;
        }
        // Get the position of the last row found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(rowAndPosition.get(rowCount)));

        // HashMap is used to store the number of row dividers and the position each
        // one is at in the measurementsTable <Integer, Integer> = <DivNumber, DivPosition>
        SparseIntArray dividerAndPosition = getDividerCountAndPositions();
        int divCount = dividerAndPosition.size();
        if (divCount == 0) {
            Log.d(TAG, "There were no row dividers in the measurement table -- return;");
            return;
        }
        // Get the position of the last row divider found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(dividerAndPosition.get(divCount)));

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
    // MainActivity::renumberAllAfterRow
    //
    // Renumbers all of the pipe numbers after the passed in row starting with the
    // passed in value.
    //

    private void renumberAllAfterRow(TableRow pR, int pPipeNum) {

        for (int rowPos=(getPositionOfRow(pR)+1); rowPos<measurementsTable.getChildCount(); rowPos++) {

            View v = measurementsTable.getChildAt(rowPos);

            if (!(v instanceof TableRow)) { continue; }

            setPipeNumberOfRow((TableRow)v, Integer.toString(pPipeNum++));

        }

    }//end of MainActivity::renumberAllAfterRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::setAdjustedColumns
    //
    // If the passed in protector/makeup value is not the same as the old one, the
    // Adjusted columns are set using the passed in float.
    //
    // Each Adjusted column's value is determined by subtracting the passed in
    // float from the Total Length column in the corresponding row.
    //

    private void setAdjustedColumns(float pNewProtectorMakeupValue) {

        if (pNewProtectorMakeupValue == protectorMakeupValue) { return; }

        protectorMakeupValue = pNewProtectorMakeupValue;

        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            View v = measurementsTable.getChildAt(i);

            if (!(v instanceof TableRow)) { continue; }

            float totalLength = Float.parseFloat(getTotalLengthColValueOfRow((TableRow)v));
            float adjustedValue = totalLength - protectorMakeupValue;
            getAdjustedColumnOfRow((TableRow)v).setText(Float.toString(adjustedValue));

        }

    }//end of MainActivity::setAdjustedColumns
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::setJobTitle
    //
    // If the passed in job title is not the same as the old one, the job title
    // is changed to the passed in string.
    //

    private void setJobTitle(String pNewJobTitle) {

        TextView jobTitleTextView = (TextView)findViewById(R.id.jobTitleTextView);

        if (pNewJobTitle.equals(jobTitleTextView.getText().toString())) {
            return;
        }

        jobTitleTextView.setText(pNewJobTitle);

    }//end of MainActivity::setJobTitle
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::setPipeNumberOfRow
    //
    // Sets the value under the Pipe # column in the passed in row to the passed in
    // string.
    //

    private void setPipeNumberOfRow(TableRow pR, String pPipeNum) {

        // For each child in the row, check its id
        // and see if it is the Pipe # column.
        // If it is, then the TextView is set
        // to the passed in string.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnPipeNum) {
                TextView tV = (TextView)pR.getChildAt(i);
                tV.setText(pPipeNum);
            }

        }

    }//end of MainActivity::setPipeNumberOfRow
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
    // MainActivity::setTotalLengthOfRow
    //
    // Sets the value under the Total Length column in the passed in row to the
    // passed in string.
    //

    private void setTotalLengthOfRow(TableRow pR, String pTotalLength) {

        // For each child in the row, check its id
        // and see if it is the TotalLength column.
        // If it is, the text of the TextView is
        // set to pTotalLength.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnActual) {
                TextView tV = (TextView)pR.getChildAt(i);
                tV.setText(pTotalLength);
            }

        }

    }//end of MainActivity::setTotalLengthOfRow
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
