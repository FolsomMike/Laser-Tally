/******************************************************************************
* Title: JobDisplayActivity.java
* Author: Hunter Schoonover
* Date: 9/24/14
*
* Purpose:
*
* This class extends an Activity used for the main job display screen. It
* is the main screen used for all actions and functions once a job has been
* selected.
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
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobDisplayActivity
//

public class JobDisplayActivity extends Activity {

    public static final String TAG = "JobDisplayActivity";

    private View decorView;
    private int uiOptions;

    private Handler handler = new Handler();

    Button measureConnectButton;
    Button redoButton;

    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;

    TableLayout measurementsTable;

    SharedSettings sharedSettings;

    final String connectButtonText = "connect";
    final String measureButtonText = "measure";
    final String noValueString = "No Value";

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");

    // Job Info Variables
    private float adjustmentValue = 0;
    private String companyName = "";
    private String jobName = "";
    private float tallyGoal;
    // End of Job Info Variables

    private ArrayList<String> adjustedValues = new ArrayList<String>();
    private ArrayList<String> totalLengthValues = new ArrayList<String>();

    private TableRow lastRowEdited;

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::JobDisplayActivity (constructor)
    //

    public JobDisplayActivity() {

        super();

        messenger = new Messenger(new IncomingHandler(this));

    }//end of JobDisplayActivity::JobDisplayActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedSettings = new SharedSettings();
        sharedSettings.init();
        sharedSettings.context = this;

        setContentView(R.layout.activity_job_display);

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

        //debug hss//
        Log.d(TAG, "inside of JobDisplay onCreate");

        Bundle bundle = getIntent().getExtras();
        if (bundle.getBoolean(Keys.JOB_INFO_INCLUDED_KEY, false)) {

            setJobName(bundle.getString(Keys.JOB_NAME_KEY));
            companyName = bundle.getString(Keys.COMPANY_NAME_KEY);
            setAdjustmentValue(bundle.getString(Keys.ADJUSTMENT_KEY));
            setTallyGoal(bundle.getString(Keys.TALLY_GOAL_KEY));

        }

        serviceIntent = new Intent(this, TallyDeviceService.class);
        startService(serviceIntent);

    }//end of JobDisplayActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        stopService(serviceIntent);

        super.onDestroy();

    }//end of JobDisplayActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        decorView.setSystemUiVisibility(uiOptions);

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        //hss wip// -- should load values from file
        setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns();

    }//end of JobDisplayActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        try { unbindService(connection); } catch (Exception e) {}

        if (service == null) { return; }

        try {

            Message msg = Message.obtain(null,
                                            TallyDeviceService.MSG_UNREGISTER_JOB_DISPLAY_ACTIVITY);
            if (msg == null) { return; }
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of JobDisplayActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

        if (pRequestCode == Keys.ACTIVITY_RESULT_JOB_INFO) {

            if (pResultCode == RESULT_OK) {
                handleJobInfoActivityResultOk(pData.getStringExtra(Keys.JOB_NAME_KEY),
                                                    pData.getStringExtra(Keys.ADJUSTMENT_KEY),
                                                    pData.getStringExtra(Keys.TALLY_GOAL_KEY));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleJobInfoActivityResultCancel();
            }

        }
        else if (pRequestCode == Keys.ACTIVITY_RESULT_TABLE_ROW_EDITOR) {

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
        else {

            // This activity did recognize any of the
            // activity results, so it sends them to the
            // tally device service.
            Message msg = Message.obtain(null, TallyDeviceService.MSG_ACTIVITY_RESULT);
            if (msg == null) { return; }
            msg.arg1 = pRequestCode;
            msg.arg2 = pResultCode;
            msg.obj = pData;
            try { service.send(msg); } catch (Exception e) { unbindService(connection); }

        }

    }//end of JobDisplayActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::connection
    //
    // Not really a function
    //
    // Creates a new ServiceConnection object and overrides its onServiceConnected()
    // and onServiceDisconnected() functions.
    //

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName pName, IBinder pService) {

            service = new Messenger(pService);

            registerWithService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            service = null;

        }

    };//end of JobDisplayActivity::connection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onClickListener
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

    };//end of JobDisplayActivity::onClickListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::calculateAndSetAdjustedValueOfRow
    //
    // Sets the value of the adjusted column of the passed in row to the adjusted
    // value calculated by using the passing in total length.
    //

    private void calculateAndSetAdjustedValueOfRow(TableRow pR, String pTotalLength) {

        float totalLength = Float.parseFloat(pTotalLength);
        float adjustedValue = totalLength - adjustmentValue;

        (getAdjustedColumnOfRow(pR)).setText(tallyFormat.format(adjustedValue));

    }//end of JobDisplayActivity::calculateAndSetAdjustedValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::checkTotal
    //
    // Check to see if the total of the adjusted values is equal to or greater
    // than the tally goal.
    //
    // If the total is equal or greater, set the background color of the totals
    // table to green.
    // If the total is below, set the background color of the totals table back
    // its original color.
    //

    private void checkTallyGoal() {

        Float totalOfAdjustedValues = Float.parseFloat(getTotalOfAdjustedValues());
        TableLayout totalsTable = (TableLayout) findViewById(R.id.totalsTable);

        //hss wip//

        if (totalOfAdjustedValues < tallyGoal) {
            Log.d(TAG, "Total of Adjusted Values is less than tally goal");
            totalsTable.setBackgroundColor(Color.parseColor("#000000"));
        }
        else if (totalOfAdjustedValues >= tallyGoal) {
            Log.d(TAG, "Total of Adjusted Values is greater than or equal to tally goal");
            totalsTable.setBackgroundColor(Color.parseColor("#33CC33"));
        }

    }//end of JobDisplayActivity::checkTotal
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createNewColumn
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

    }//end of JobDisplayActivity::createNewColumn
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createNewColumnDivider
    //
    // Creates and returns a View object with the properties of a column
    // divider used with the measurements table.
    //

    public View createNewColumnDivider() {

        View cd = new View(this);
        cd.setLayoutParams(new TableRow.LayoutParams(2, TableRow.LayoutParams.MATCH_PARENT));
        cd.setBackgroundColor(Color.parseColor("#505050"));

        return cd;

    }//end of JobDisplayActivity::createNewColumnDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createNewRow
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

    }//end of JobDisplayActivity::createNewRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createNewRowDivider
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

    }//end of JobDisplayActivity::createNewRowDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createNewSideBorder
    //
    // Creates and returns a View object with the properties of a side border used
    // with the measurements table.
    //

    public View createNewSideBorder() {

        View sb = new View(this);
        sb.setLayoutParams(new TableRow.LayoutParams(6, TableRow.LayoutParams.MATCH_PARENT));
        sb.setBackgroundColor(Color.parseColor("#000000"));

        return sb;

    }//end of JobDisplayActivity::createNewSideBorder
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::createUiChangeListener
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

    }//end of JobDisplayActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getAdjustedColumnOfRow
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
                break;
            }

        }

        return adjustedColumn;

    }//end of JobDisplayActivity::getAdjustedColumnOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getAdjustedColValueOfRow
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
                break;
            }

        }

        return adjusted;

    }//end of JobDisplayActivity::getAdjustedColValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getDividerCountAndPositions
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

    }//end of JobDisplayActivity::getDividerCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getNextPipeNumber
    //
    // Determines what the pipe number of the next row will be and returns that
    // value as a string
    //

    private String getNextPipeNumber() {

        // Get the row count and each of their positions
        SparseIntArray rowAndPos = getRowCountAndPositions();

        // Check to see if the row count is greater than 1
        // If it is greater than 1, then pipeNumInt is set
        // to the pipe number found in the last row. If
        // not, then the pipeNumInt remains equal to 1.
        int pipeNumInt = rowAndPos.size() + 1;
        if (pipeNumInt > 1) {
            TableRow tR = (TableRow) measurementsTable.getChildAt(rowAndPos.get(rowAndPos.size()));
            pipeNumInt = Integer.parseInt(getPipeNumberColValueOfRow(tR)) + 1;
        }

        return Integer.toString(pipeNumInt);

    }//end of JobDisplayActivity::getNextPipeNumber
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getPipeNumberColValueOfRow
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
                break;
            }

        }

        return pipeNum;

    }//end of JobDisplayActivity::getPipeNumberColValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getPositionOfRow
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

    }//end of JobDisplayActivity::getPositionOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getRowCountAndPositions
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

    }//end of JobDisplayActivity::getRowCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getTotalLengthColValueOfRow
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
                break;
            }

        }

        return actual;

    }//end of JobDisplayActivity::getTotalLengthColValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getTotalOfAdjustedValues
    //
    // Calculates and returns the total of the Adjusted values in the form of a
    // string.
    //

    private String getTotalOfAdjustedValues() {

        float totalOfAdjustedValues = 0;
        for (String val : adjustedValues) {
            if (val.equals(noValueString)) { continue; }
            totalOfAdjustedValues = totalOfAdjustedValues + Float.parseFloat(val);
        }
        return tallyFormat.format(totalOfAdjustedValues);

    }//end of JobDisplayActivity::getTotalOfAdjustedValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::getTotalOfTotalLengthValues
    //
    // Calculates and returns the total of the Total Length values in the form of a
    // string.
    //

    private String getTotalOfTotalLengthValues() {

        float totalOfTotalLengthValues = 0;
        for (String val : totalLengthValues) {
            totalOfTotalLengthValues = totalOfTotalLengthValues + Float.parseFloat(val);
        }
        return tallyFormat.format(totalOfTotalLengthValues);

    }//end of JobDisplayActivity::getTotalOfTotalLengthValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleConnectedState
    //
    // Sets the measureConnectButton to its "measuring" look and text and sets
    // the redo button visible.
    //

    public void handleConnectedState() {

        measureConnectButton.setBackground(getResources().getDrawable
                (R.drawable.blue_styled_button));
        measureConnectButton.setText(measureButtonText);
        measureConnectButton.setOnClickListener(onClickListener);
        measureConnectButton.setVisibility(View.VISIBLE);

        redoButton.setOnClickListener(onClickListener);
        redoButton.setVisibility(View.VISIBLE);

    }//end of JobDisplayActivity::handleConnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleDisconnectedState
    //
    // Sets the measureConnectButton to its "connecting" look and text and sets
    // the redo button invisible.
    //

    public void handleDisconnectedState() {

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        measureConnectButton.setBackground(getResources().getDrawable
                                                                (R.drawable.green_styled_button));
        measureConnectButton.setText(connectButtonText);
        measureConnectButton.setOnClickListener(onClickListener);
        measureConnectButton.setVisibility(View.VISIBLE);

        redoButton = (Button)findViewById(R.id.redoButton);
        redoButton.setVisibility(View.INVISIBLE);

    }//end of JobDisplayActivity::handleDisconnectedState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleIdleState
    //
    // Sets the measureConnectButton to its "connecting" look and text and sets
    // the redo button invisible.
    //

    public void handleIdleState() {

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        measureConnectButton.setBackground(getResources().getDrawable
                (R.drawable.green_styled_button));
        measureConnectButton.setText(connectButtonText);
        measureConnectButton.setOnClickListener(onClickListener);
        measureConnectButton.setVisibility(View.VISIBLE);

        redoButton = (Button)findViewById(R.id.redoButton);
        redoButton.setVisibility(View.INVISIBLE);

    }//end of JobDisplayActivity::handleIdleState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleJobInfoActivityResultOk
    //
    // Sets the job title to the passed in job title and sets the adjusted columns
    // using the passed in protector/makeup adjustment value.
    //

    private void handleJobInfoActivityResultOk(String pJob, String pAdjustmentValue,
                                                                                String pTallyGoal) {

        setJobName(pJob);
        setAdjustmentValue(pAdjustmentValue);
        setTallyGoal(pTallyGoal);
        setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns();

    }//end of JobDisplayActivity::handleJobInfoActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleJobInfoActivityResultCancel
    //
    // Currently does nothing.
    //

    private void handleJobInfoActivityResultCancel() {

        //Currently does nothing

    }//end of JobDisplayActivity::handleJobInfoActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleJobInfoButtonPressed
    //
    // Starts an activity for Job Info.
    // Should be called from the "Job Info" button onClick().
    //

    public void handleJobInfoButtonPressed(View pView) {

        Intent intent = new Intent(this, EditJobInfoActivity.class);
        intent.putExtra(Keys.JOB_NAME_KEY, jobName);
        intent.putExtra(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY,
                                        EditJobInfoActivity.EditJobInfoActivityMode.EDIT_JOB_INFO);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_JOB_INFO);

    }//end of JobDisplayActivity::handleJobInfoButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleMeasureConnectButtonPressed
    //
    // Calls functions depending on the text of the handleMeasureConnectButton
    // button.
    //

    public void handleMeasureConnectButtonPressed() {

        String btnText = measureConnectButton.getText().toString();

        if (btnText.equals(connectButtonText)) { startTallyDeviceScan(); }
        else if (btnText.equals(measureButtonText)) { sendMeasureCommandToTallyDevice(); }

    }//end of JobDisplayActivity::handleMeasureConnectButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleMoreButtonPressed
    //
    // Starts an activity for "More".
    // Should be called from the "More" button onClick().
    //
    // Currently prints. //hss wip//
    //

    public void handleMoreButtonPressed(View pView) {

        TallyReportHTMLPrintoutMaker tallyReportMaker = new TallyReportHTMLPrintoutMaker(
         sharedSettings, measurementsTable, companyName, jobName, "",  adjustmentValue, tallyGoal);
        tallyReportMaker.init();
        tallyReportMaker.printTallyReport();

        //use this code block to save a tally report to an HTML file -- mainly used for debugging
        TallyReportHTMLFileMaker tallyReportFileMaker = new TallyReportHTMLFileMaker(
         sharedSettings, measurementsTable, companyName, jobName, "",  adjustmentValue, tallyGoal);
        tallyReportFileMaker.init();
        tallyReportFileMaker.printTallyReport();


    }//end of JobDisplayActivity::handleMoreButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleNewDistanceValue
    //
    // Creates a new row in the measurementsTable and inserts the appropriate data
    // using the passed in value.
    //

    public void handleNewDistanceValue(String pDistanceValue) {

        //debug hss//
        Log.d(TAG, "handleNewDistanceValue");

        // Calculate the adjusted value
        float adjustedValue = Float.parseFloat(pDistanceValue) - adjustmentValue;
        String adjustedValueString = tallyFormat.format(adjustedValue);

        View measurementsTableBottomBorderLine = findViewById(R.id.measurementsTableBottomBorderLine);

        // Remove the bottom border line of the table
        // It should be put back after the next row has
        // has been added.
        measurementsTable.removeView(measurementsTableBottomBorderLine);

        measurementsTable.addView(createNewRow(getNextPipeNumber(), pDistanceValue, adjustedValueString));
        measurementsTable.addView(createNewRowDivider());

        measurementsTableBottomBorderLine.setVisibility(View.VISIBLE);
        measurementsTable.addView(measurementsTableBottomBorderLine);

        scrollToBottomOfMeasurementsTable();

        //Add the total length and adjusted values to their appropriate lists and
        //set the totals columns
        totalLengthValues.add(pDistanceValue);
        adjustedValues.add(adjustedValueString);
        setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns();

        //enable the measureConnect and redo buttons
        //so that the user can use them now that the
        //measuring process has been completed
        setMeasureConnectButtonEnabled(true);
        setRedoButtonEnabled(true);

    }//end of JobDisplayActivity::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleNoNewDistanceValueReceived
    //
    // Enables the Measure and Redo buttons.
    //
    // This function handles when a distance value is not received back from the
    // tally device after the measure command was sent to it. This is to
    // ensure that if a distance value was not received, the Measure and Redo buttons
    // are not permanently disabled.
    //

    private void handleNoNewDistanceValueReceived () {

        setMeasureConnectButtonEnabled(true);
        setRedoButtonEnabled(true);

    }//end of JobDisplayActivity::handleNoNewDistanceValueReceived
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleRedoButtonPressed
    //
    // Retrieves and removes the final row added to the measurementsTable.
    //

    private void handleRedoButtonPressed() {

        // HashMap is used to store the number of rows and the position of each
        // one is at in the measurementsTable <Integer, Integer> = <RowNumber, RowPosition>
        SparseIntArray rowAndPosition = getRowCountAndPositions();
        int rowCount = rowAndPosition.size();
        if (rowCount <= 0) { return; }
        // Get the position of the last row found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(rowAndPosition.get(rowCount)));

        // HashMap is used to store the number of row dividers and the position each
        // one is at in the measurementsTable <Integer, Integer> = <DivNumber, DivPosition>
        SparseIntArray dividerAndPosition = getDividerCountAndPositions();
        int divCount = dividerAndPosition.size();
        if (divCount <= 0) { return; }
        // Get the position of the last row divider found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(dividerAndPosition.get(divCount)));

        //Remove the last added Total Length and Adjusted values
        //from their lists and set the total columns
        adjustedValues.remove(adjustedValues.size() - 1);
        totalLengthValues.remove(totalLengthValues.size() - 1);
        setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns();

        //Check to see if the adjusted values total matches the tally goal
        checkTallyGoal();

        scrollToBottomOfMeasurementsTable();

    }//end of JobDisplayActivity::handleRedoButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleTableRowEditorActivityResultOk
    //
    // Sets the pipe number and total length of the last edited row to the passed
    // in values.
    // Also sets the background color of the last edited row back to its original
    // color.
    //

    private void handleTableRowEditorActivityResultOk(String pPipeNum, String pTotalLength,
                                                    boolean pRenumberAll) {

        lastRowEdited.setBackgroundColor(getResources().getColor(R.color.measurementsTableColor));
        setPipeNumberOfRow(lastRowEdited, pPipeNum);
        setTotalLengthOfRow(lastRowEdited, pTotalLength);
        calculateAndSetAdjustedValueOfRow(lastRowEdited, pTotalLength);

        if (!pRenumberAll) { return; }

        renumberAllAfterRow(lastRowEdited, Integer.parseInt(pPipeNum)+1);

    }//end of JobDisplayActivity::handleTableRowEditorActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleTableRowEditorActivityResultCancel
    //
    // Sets the background color of the last edited row back to its original color.
    //

    private void handleTableRowEditorActivityResultCancel() {

        lastRowEdited.setBackgroundColor(getResources().getColor(R.color.measurementsTableColor));

    }//end of JobDisplayActivity::handleTableRowEditorActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleTableRowPressed
    //
    // Gets the values in the Pipe #, Actual, and Adjusted columns and sends the
    // values to the EditPipeRowActivity to be displayed to and edited by the user.
    //

    public void handleTableRowPressed(TableRow pR) {

        lastRowEdited = pR;
        pR.setBackgroundColor(getResources().getColor(R.color.selectedTableRowColor));
        String pipeNum = getPipeNumberColValueOfRow(pR);
        String totalLength = getTotalLengthColValueOfRow(pR);

        Intent intent = new Intent(this, TableRowEditorActivity.class);
        intent.putExtra(TableRowEditorActivity.PIPE_NUMBER_KEY, pipeNum);
        intent.putExtra(TableRowEditorActivity.TOTAL_LENGTH_KEY, totalLength);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_TABLE_ROW_EDITOR);

    }//end of JobDisplayActivity::handleTableRowPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::registerWithService
    //
    // Sends a message to the TallyDeviceService to register.
    //

    private void registerWithService() {

        //debug hss//
        Log.d(TAG, "register with service");

        try {

            Message msg = Message.obtain(null, TallyDeviceService.MSG_REGISTER_JOB_DISPLAY_ACTIVITY);
            if (msg == null) { return; }
            msg.obj = this;
            msg.replyTo = messenger;
            service.send(msg);

        } catch (Exception e) { service = null; }

    }//end of JobDisplayActivity::registerWithService
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::renumberAllAfterRow
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

    }//end of JobDisplayActivity::renumberAllAfterRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::scrollToBottomOfMeasurementsTable
    //
    // Scrolls the ScrollView containing the measurements table all the way to the
    // bottom.
    //

    private void scrollToBottomOfMeasurementsTable() {

        final ScrollView sv = (ScrollView)findViewById(R.id.measurementsTableScrollView);

        handler.post(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        });

    }//end of JobDisplayActivity::scrollToBottomOfMeasurementsTable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::sendMeasureCommandToTallyDevice
    //
    // Sends a message to the tally device service to send the measuring command
    // to the connected tally device and disables the measure and redo button.
    //

    private void sendMeasureCommandToTallyDevice() {

        Message msg = Message.obtain(null,
                TallyDeviceService.MSG_SEND_MEASURE_COMMAND_TO_TALLY_DEVICE);
        if (msg == null) { return; }

        try { service.send(msg); } catch (RemoteException e) {

            Log.d(TAG, "send measure command to tally device failed"); unbindService(connection); }

        //disable the measureConnect and redo buttons
        //so that the user can't use them during the
        //measuring process
        setMeasureConnectButtonEnabled(false);
        setRedoButtonEnabled(false);

    }//end of JobDisplayActivity::sendMeasureCommandToTallyDevice
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setAdjustmentValue
    //
    // Sets the adjustment value to the passed in variable and sets the values of
    // the adjusted columns
    //
    // The adjustment value is set to 0 if the string is empty.
    //

    private void setAdjustmentValue(String pNewAdjustmentValue) {

        if (pNewAdjustmentValue.equals("")) { adjustmentValue = 0; }
        else {
            adjustmentValue = Float.parseFloat(pNewAdjustmentValue);
        }

        setValuesOfAdjustedColumns();

    }//end of JobDisplayActivity::setAdjustmentValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns
    //
    // Sets the total columns to the totals of the Adjusted and Total Length columns
    // and checks to see if the tally goal is connected.
    //

    private void setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns() {

        // Set the total of the Adjusted column
        String totalOfAdjustedValuesStrings = getTotalOfAdjustedValues();
        TextView adjustedTotalTextView = (TextView)findViewById(R.id.totalOfAdjustedColumnTextView);
        adjustedTotalTextView.setText(totalOfAdjustedValuesStrings);

        // Set the total of the Total Length column
        String totalOfTotalLengthsString = getTotalOfTotalLengthValues();
        TextView totalLengthTotalTextView = (TextView)findViewById(R.id.totalOfTotalLengthColumnTextView);
        totalLengthTotalTextView.setText(totalOfTotalLengthsString);

        checkTallyGoal();

    }//end of JobDisplayActivity::setAndCheckTotalColumnsOfTotalLengthAndAdjustedColumns
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setJobName
    //
    // If the passed in job name is not the same as the old one, the job name
    // is changed to the passed in string.
    //

    private void setJobName(String pNewJobName) {

        TextView jobTitleTextView = (TextView)findViewById(R.id.jobNameTextView);

        if (pNewJobName.equals(jobTitleTextView.getText().toString())) { return; }

        jobName = pNewJobName;

        jobTitleTextView.setText(jobName);

    }//end of JobDisplayActivity::setJobName
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setMeasureConnectButtonEnabled
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

    }//end of JobDisplayActivity::setMeasureConnectButtonEnabled
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setPipeNumberOfRow
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
                break;
            }

        }

    }//end of JobDisplayActivity::setPipeNumberOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setRedoButtonEnabled
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

    }//end of JobDisplayActivity::setRedoButtonEnabled
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setTallyGoal
    //
    // Sets the tally goal to the passed in variable.
    //
    // The tally goal is set to Float.MAX_VALUE if the string is empty.
    //

    private void setTallyGoal(String pNewTallyGoal) {

        if (pNewTallyGoal.equals("") || Float.parseFloat(pNewTallyGoal) == 0) {
            tallyGoal = Float.MAX_VALUE;
            return;
        }
        tallyGoal = Float.parseFloat(tallyFormat.format(Float.parseFloat(pNewTallyGoal)));

        //debug hss//
        Log.d(TAG, "tallyGoal float value: " + tallyGoal);

    }//end of JobDisplayActivity::setTallyGoal
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setTotalLengthOfRow
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
                break;
            }

        }

    }//end of JobDisplayActivity::setTotalLengthOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::setValuesOfAdjustedColumns
    //
    // Calculates and sets the values of all the rows in the Adjusted column.
    //
    // Each Adjusted column's value is determined by subtracting the passed in
    // float from the Total Length column in the corresponding row.
    //

    private void setValuesOfAdjustedColumns() {

        //Clear the old adjusted values
        adjustedValues.clear();

        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            View v = measurementsTable.getChildAt(i);

            if (!(v instanceof TableRow)) { continue; }

            float totalLength = Float.parseFloat(getTotalLengthColValueOfRow((TableRow)v));
            float adjustedValue = totalLength - adjustmentValue;

            String adjustedValueString = tallyFormat.format(adjustedValue);
            adjustedValues.add(adjustedValueString);
            (getAdjustedColumnOfRow((TableRow)v)).setText(adjustedValueString);

        }

    }//end of JobDisplayActivity::setValuesOfAdjustedColumns
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::startTallyDeviceScan
    //
    // Starts the TallyDeviceScanActivity.
    //

    private void startTallyDeviceScan() {

        Intent intent = new Intent(this, TallyDeviceScanActivity.class);
        startActivity(intent);

    }//end of JobDisplayActivity::startTallyDeviceScan
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::stateChanged
    //
    // Performs different operations depending on the passed in connection state.
    //

    private void stateChanged(TallyDeviceService.State pNewState) {

        state = pNewState;

        //debug hss//
        Log.d(TAG, "State changed: " + state);

        if (state == TallyDeviceService.State.CONNECTED) { handleConnectedState(); }
        else if (state == TallyDeviceService.State.DISCONNECTED) { handleDisconnectedState(); }
        else if (state == TallyDeviceService.State.IDLE) { handleIdleState(); }

    }//end of JobDisplayActivity::stateChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class JobDisplayActivity::IncomingHandler
    //
    // Purpose:
    //
    // This class handles incoming messages given to the messenger to which it
    // was passed.
    //

    private static class IncomingHandler extends Handler {

        private final WeakReference<JobDisplayActivity> activity;

        //-----------------------------------------------------------------------------
        // IncomingHandler::IncomingHandler (constructor)
        //

        public IncomingHandler(JobDisplayActivity pActivity) {

            activity = new WeakReference<JobDisplayActivity>(pActivity);

        }//end of IncomingHandler::IncomingHandler (constructor)
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // IncomingHandler::handleMessage
        //
        // Checks to see if the activity is null. Then calls functions if it isn't null. //hss wip//
        //

        @Override
        public void handleMessage(Message pMsg) {

            JobDisplayActivity tempActivity = activity.get();
            if (tempActivity != null) {

                Log.d(TAG, "message received: " + pMsg.what);

                switch (pMsg.what) {

                    case TallyDeviceService.MSG_CONNECTION_STATE:
                        tempActivity.stateChanged(TallyDeviceService.State.values()[pMsg.arg1]);
                        break;

                    case TallyDeviceService.MSG_NEW_DISTANCE_VALUE:
                        tempActivity.handleNewDistanceValue((String)pMsg.obj);
                        break;

                    case TallyDeviceService.MSG_N0_NEW_DISTANCE_VALUE_RECEIVED:
                        tempActivity.handleNoNewDistanceValueReceived();
                        break;

                }

            }

            super.handleMessage(pMsg);

        }//end of IncomingHandler::handleMessage
        //-----------------------------------------------------------------------------

    }//end of class JobDisplayActivity::IncomingHandler
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class JobDisplayActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
