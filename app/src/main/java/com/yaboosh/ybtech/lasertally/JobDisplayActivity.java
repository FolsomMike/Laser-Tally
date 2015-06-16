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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobDisplayActivity
//

public class JobDisplayActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

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

    // Job Info Variables
    private String jobName = "";
    // End of Job Info Variables

    private int lastClickedRowPos;

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::JobDisplayActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public JobDisplayActivity()
    {
        LOG_TAG = "JobDisplayActivity";

        layoutResID = R.layout.activity_job_display;

        messenger = new Messenger(new IncomingHandler(this));

    }//end of JobDisplayActivity::JobDisplayActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

        measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
        redoButton = (Button)findViewById(R.id.redoButton);

        //add a footer to the Tally Data ListView
        View foot = this.getLayoutInflater().inflate(R.layout.layout_list_view_footer, null, false);
        ((ListView)findViewById(R.id.tallyDataListView)).addFooterView(foot);

        //DEBUG HSS//
        //initialize list view and add an onClickListener
        //ListView tallyDataListView = (ListView) findViewById(R.id.tallyDataListView);
        /*tallyDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> pParent, final View pView, int pPos, long pId)
            {

                handleTableRowPressed(pPos, pView);

            }

        });*/
        //DEBUG HSS//

        //set the job name
        setJobName(jobsHandler.getJobName());

        //Create a TallyDataHandler and give it its own MeasurementsTableHandler and a reference
        //to jobsHandler
        tallyDataHandler = new TallyDataHandler(this, sharedSettings, jobsHandler);
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

        activitiesLaunched.getAndDecrement();

        stopService(serviceIntent);

        super.onDestroy();

    }//end of JobDisplayActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onResume
    //
    // Automatically called upon activity resume.
    //
    // All functions that must be done upon activity resume should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        scrollMeasurementsTable();

    }//end of JobDisplayActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    //
    // All functions that must be done upon activity pause should be called here.
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
    // CreateJobActivity::focusChanged
    //
    // Called when the focus changes from one view to another.
    //
    // Changes the background color of all the non-focused TableRows in the focus
    // array to measurementsTableColor. The focused TableRow's background
    // color is changed to selectedTableRowColor.
    //
    // We have to manually handle the changing of backgrounds because Android
    // has issues the state options ("state_focused", etc.) has issues when
    // it comes to focusing; it doesn't always work.
    //
    // Also scrolls to the bottom of the measurements table if the last row has
    // been focused.
    //

    @Override
    protected void focusChanged() {

        for (View v : focusArray) {
            int c = getResources().getColor(R.color.measurementsTableColor);
            if (v == viewInFocus) { c = getResources().getColor(R.color.selectedTableRowColor); }
            v.setBackgroundColor(c);
        }

        //scroll to the bottom of the table if the
        //view in focus is the last row.
        //This is done because when the user is using
        //only the keyboard, navigating to the last row
        //doesn't make the bottom border line visible
        //which makes it difficult to distinguish the
        //last row between any other rows
        if (focusArray.indexOf(viewInFocus) == startingIndexOfFocusArray) {
            //DEBUG HSS//scrollToBottomOfMeasurementsTable();
        }

    }//end of CreateJobActivity::focusChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleEscapeKeyPressed
    //
    // Performs a click on the redo button.
    //

    @Override
    protected void handleEscapeKeyPressed() {

        performClickOnView(redoButton);

    }//end of JobDisplayActivity::handleEscapeKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleF2KeyPressed
    //
    // Performs a click on the measure/connect button.
    //

    @Override
    protected void handleF2KeyPressed() {

        performClickOnView(measureConnectButton);

    }//end of JobDisplayActivity::handleF2KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleF3KeyPressed
    //
    // Performs a click on the focused table row.
    //

    @Override
    protected void handleF3KeyPressed() {

        performClickOnView(viewInFocus);

    }//end of JobDisplayActivity::handleF3KeyPressed
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
                if (pResultCode == Activity.RESULT_OK) { handleEditJobActivityResult(pData); }
        }
        else if (pRequestCode == Keys.ACTIVITY_RESULT_MORE) {

            if (pResultCode == RESULT_OK) {
                handleMoreActivityResultOk((SharedSettings) pData.getParcelableExtra(Keys.SHARED_SETTINGS_KEY));
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleMoreActivityResultCancel();
            }

        }
        else if (pRequestCode == Keys.ACTIVITY_RESULT_TABLE_ROW_EDITOR) {

            if (pResultCode == RESULT_OK) {
                tallyDataHandler.handleTableRowEditorActivityResultOk(
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

                case R.id.redoButton:
                    handleRedoButtonPressed();
                    break;

            }

        }

    };//end of JobDisplayActivity::onClickListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleConnectedState
    //
    // Sets the measureConnectButton to its "measuring" look and text and sets
    // the redo button visible.
    //

    public void handleConnectedState() {

        measureConnectButton.setBackground(getResources().getDrawable(R.drawable.blue_styled_button));
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
    // JobDisplayActivity::handleEditJobActivityResult
    //
    // Extracts the JobsHandler from the passed in intent and uses the passed it
    // to set the job name, adjustment value, and tally goal.
    //

    private void handleEditJobActivityResult(Intent pData) {

        jobsHandler = pData.getParcelableExtra(Keys.JOBS_HANDLER_KEY);
        tallyDataHandler.setJobInfo(jobsHandler);

        setJobName(jobsHandler.getJobName());

    }//end of JobDisplayActivity::handleEditJobActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleJobInfoButtonPressed
    //
    // Starts the EditJobActivity.
    //
    // Should be called from the job info button onClick().
    //

    public void handleJobInfoButtonPressed(View pView) {

        Intent intent = new Intent(this, EditJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
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
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
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

        if (viewInFocus != null) { viewInFocus.clearFocus(); }

        tallyDataHandler.handleNewDistanceValue(Double.parseDouble(pDistanceValue));

        //enable the measureConnect and redo buttons
        //so that the user can use them now that the
        //measuring process has been completed
        setMeasureConnectButtonEnabled(true);
        setRedoButtonEnabled(true);

        //set the focus to the last row in the table
        if (!focusArray.isEmpty()) { focusView(focusArray.get(focusArray.size()-1)); }

    }//end of JobDisplayActivity::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleNoNewDistanceValueReceived
    //
    // Plays the bad sound and enables the Measure and Redo buttons.
    //
    // This function handles when a distance value is not received back from the
    // tally device after the measure command was sent to it. This is to
    // ensure that if a distance value was not received, the Measure and Redo buttons
    // are not permanently disabled.
    //

    private void handleNoNewDistanceValueReceived () {

        Tools.playBadSound(this);

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

        if (!focusArray.isEmpty()) { focusView(focusArray.get(focusArray.size() - 1)); }

    }//end of JobDisplayActivity::handleRedoButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleTableRowEditorActivityResultCancel
    //
    // Currently does nothing.
    //

    private void handleTableRowEditorActivityResultCancel() {

    }//end of JobDisplayActivity::handleTableRowEditorActivityResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleTableRowPressed
    //
    // Gets the values in the Pipe # and Adjusted columns and sends the
    // values to the EditPipeRowActivity to be displayed to and edited by the user.
    //

    public void handleTableRowPressed(int pIndex, View pView) {

        lastClickedRowPos = pIndex;
        focusView(pView);
        scrollMeasurementsTable();

        //DEBUG HSS// -- REMOVE THIS FUNCTION

    }//end of JobDisplayActivity::handleTableRowPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::putTableRowsIntoFocusArray
    //
    // Retrieves all of the TableRows inside of the measurements table and puts
    // them into the focus array, bottom to top.
    //

    void putTableRowsIntoFocusArray() {

        focusArray.clear();

        //WIP HSS// -- SHOULD PUT LISTVIEW CHILDREN INTO FOCUS ARRAY

        // For each child in the table, check to see if it is
        // a TableRow. TableRows are added to the focus array.
        /*for (int i=0; i<table.getChildCount(); i++) {

            View child = table.getChildAt(i);

            if (child.getId() == R.id.measurementsTableRow) { focusArray.add(child); }

        }*/

        //start the focus array at the bottom of the table
        startingIndexOfFocusArray = focusArray.size()-1;

    }//end of JobDisplayActivity::putTableRowsIntoFocusArray
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::registerWithService
    //
    // Sends a message to the TallyDeviceService to register.
    //

    private void registerWithService() {

        //DEBUG HSS//
        Log.d(LOG_TAG, "register with service");

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
    // JobDisplayActivity::scrollMeasurementsTable
    //
    // Scrolls the ScrollView containing the measurements table to either the view
    // in focus or to the bottom, if no view is focused.
    //

    void scrollMeasurementsTable() {

        //WIP HSS// -- should scroll listview
        /*final ScrollView sv = (ScrollView)findViewById(R.id.measurementsTableScrollView);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (viewInFocus != null) {
                    sv.scrollTo(0, viewInFocus.getBottom() - sv.getHeight() / 2);
                } else {
                    sv.fullScroll(View.FOCUS_DOWN);
                }
            }
        });*/

    }//end of JobDisplayActivity::scrollMeasurementsTable
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

        putTableRowsIntoFocusArray();

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
        Log.d(LOG_TAG, "State changed: " + state);

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

                Log.d(LOG_TAG, "message received: " + pMsg.what);

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
