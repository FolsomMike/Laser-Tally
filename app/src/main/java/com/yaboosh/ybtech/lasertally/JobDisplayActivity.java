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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobDisplayActivity
//

public class JobDisplayActivity extends Activity {

    public static final String TAG = "JobDisplayActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;
    private JobInfo jobInfo;

    private Handler handler = new Handler();

    TallyDataHandler tallyDataHandler;

    Button measureConnectButton;
    Button redoButton;

    private final Messenger messenger;
    private Intent serviceIntent;
    private Messenger service = null;
    private TallyDeviceService.State state = TallyDeviceService.State.UNKNOWN;

    final String connectButtonText = "connect";
    final String measureButtonText = "measure";
    final String noValueString = "No Value";

    // Job Info Variables
    private float adjustmentValue = 0;
    private String jobName = "";
    private float tallyGoal;
    // End of Job Info Variables

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
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);

        setContentView(R.layout.activity_job_display);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        redoButton = (Button)findViewById(R.id.redoButton);

        // Check whether we're recreating a previously destroyed instance
        if (pSavedInstanceState != null) {
            // Restore values from saved state

            jobInfo = pSavedInstanceState.getParcelable(Keys.JOB_INFO_KEY);
            sharedSettings = pSavedInstanceState.getParcelable(Keys.SHARED_SETTINGS_KEY);

        } else {
            //initialize members with default values for a new instance

            //Get the extras from the intent
            Bundle bundle = getIntent().getExtras();

            sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
            sharedSettings.setContext(this);

            //if job info is included, then get it from the bundle
            if (bundle.getBoolean(Keys.JOB_INFO_INCLUDED_KEY, false)) {
                jobInfo = bundle.getParcelable(Keys.JOB_INFO_KEY);
            }

        }

        //set the job name
        setJobName(jobInfo.getJobName());

        //Create a TallyDataHandler and give it its own MeasurementsTableHandler and a reference
        //to jobInfo
        tallyDataHandler = new TallyDataHandler(sharedSettings, jobInfo,
                new MeasurementsTableHandler(
                        sharedSettings,
                        onClickListener,
                        (TableLayout)findViewById(R.id.measurementsTable),
                        findViewById(R.id.measurementsTableBottomBorderLine),
                        (TableLayout)findViewById(R.id.totalsTable),
                        (TextView)findViewById(R.id.totalOfAdjustedColumnTextView),
                        (TextView)findViewById(R.id.totalOfTotalLengthColumnTextView)),
                (TextView)findViewById(R.id.distanceLeftTextView),
                (TextView)findViewById(R.id.numberOfPipesLeftTextView));
        tallyDataHandler.init();

        //Start the TallyDeviceService
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

        sharedSettings.setContext(this);

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
    // JobDisplayActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        //store necessary data
        pSavedInstanceState.putParcelable(Keys.JOB_INFO_KEY, jobInfo);
        pSavedInstanceState.putParcelable(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(pSavedInstanceState);

    }//end of JobDisplayActivity::onSaveInstanceState
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
                handleJobInfoActivityResultOk((JobInfo)pData.getParcelableExtra(Keys.JOB_INFO_KEY));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleJobInfoActivityResultCancel();
            }

        }
        else if (pRequestCode == Keys.ACTIVITY_RESULT_MORE) {

            if (pResultCode == RESULT_OK) {
                handleMoreActivityResultOk((SharedSettings)pData.getParcelableExtra(Keys.SHARED_SETTINGS_KEY));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleMoreActivityResultCancel();
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
    // Uses the passed in JobInfo to set the job name, adjustment value, and
    // tally goal.
    //

    private void handleJobInfoActivityResultOk(JobInfo pJobInfo) {

        jobInfo = pJobInfo;
        tallyDataHandler.setJobInfo(jobInfo);

        setJobName(jobInfo.getJobName());

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

        Intent intent = new Intent(this, JobInfoActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOB_NAME_KEY, jobName);
        intent.putExtra(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY,
                                        JobInfoActivity.EditJobInfoActivityMode.EDIT_JOB_INFO);
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

    public void handleMoreButtonPressed(View pView) {

        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_MORE);

    }//end of JobDisplayActivity::handleMoreButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleMoreActivityResultCancel
    //
    // Currently does nothing.
    //

    private void handleMoreActivityResultCancel() {

    }//end of JobDisplayActivity::handleMoreActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleMoreActivityResultOk
    //
    // Stores the new instance of SharedSettings and passes a reference on to
    // TallyDataHandler
    //

    private void handleMoreActivityResultOk(SharedSettings pSettings) {

        sharedSettings = pSettings;
        tallyDataHandler.setSharedSettings(pSettings);

    }//end of JobDisplayActivity::handleMoreActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleNewDistanceValue
    //
    // Passes the new distance value on to the TallyDataHandler for handling and
    // enables the measure and redo buttons.
    //

    public void handleNewDistanceValue(String pDistanceValue) {

        tallyDataHandler.handleNewDistanceValue(Double.parseDouble(pDistanceValue));

        scrollToBottomOfMeasurementsTable();

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
    // Removes the last tally data entry.
    //

    private void handleRedoButtonPressed() {

        tallyDataHandler.removeLastDataEntry();

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

        tallyDataHandler.changeValuesOfExistingRow(lastRowEdited, pPipeNum, pTotalLength, pRenumberAll);

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
    // Gets the values in the Pipe # and Adjusted columns and sends the
    // values to the EditPipeRowActivity to be displayed to and edited by the user.
    //

    public void handleTableRowPressed(TableRow pR) {

        lastRowEdited = pR;
        pR.setBackgroundColor(getResources().getColor(R.color.selectedTableRowColor));
        String pipeNum = tallyDataHandler.getPipeNumberOfRow(pR);
        String totalLength = tallyDataHandler.getTotalLengthValueOfRow(pR);

        Intent intent = new Intent(this, TableRowEditorActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
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

        try { service.send(msg); } catch (RemoteException e) { unbindService(connection); return; }

        //disable the measureConnect and redo buttons
        //so that the user can't use them during the
        //measuring process
        setMeasureConnectButtonEnabled(false);
        setRedoButtonEnabled(false);

    }//end of JobDisplayActivity::sendMeasureCommandToTallyDevice
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
    // JobDisplayActivity::startTallyDeviceScan
    //
    // Starts the TallyDeviceScanActivity.
    //

    private void startTallyDeviceScan() {

        Intent intent = new Intent(this, TallyDeviceScanActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
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
