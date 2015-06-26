/******************************************************************************
 * Title: EditJobActivity.java
 * Author: Hunter Schoonover
 * Date: 06/11/15
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to edit a job
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class EditJobActivity
//

public class EditJobActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    private Handler handler = new Handler();

    private Button okButton;

    //Keys to be used for saving an instance of the activity
    private final String COMPANY_NAME_KEY = "COMPANY_NAME_KEY";
    private final String DATE_KEY = "DATE_KEY";
    private final String DIAMETER_KEY = "DIAMETER_KEY";
    private final String FACILITY_KEY = "FACILITY_KEY";
    private final String GRADE_KEY = "GRADE_KEY";
    private final String IMPERIAL_ADJUSTMENT_KEY = "IMPERIAL_ADJUSTMENT_KEY";
    private final String IMPERIAL_TALLY_GOAL_KEY = "IMPERIAL_TALLY_GOAL_KEY";
    private final String METRIC_ADJUSTMENT_KEY = "METRIC_ADJUSTMENT_KEY";
    private final String METRIC_TALLY_GOAL_KEY = "METRIC_TALLY_GOAL_KEY";
    private final String RACK_KEY = "RACK_KEY";
    private final String RANGE_KEY = "RANGE_KEY";
    private final String RIG_KEY = "RIG_KEY";
    private final String WALL_KEY = "WALL_KEY";

    private TextView textViewJobName;

    private EditText adjustmentEditText;
    private EditText companyNameEditText;
    private EditText dateEditText;
    private EditText diameterEditText;
    private EditText facilityEditText;
    private EditText gradeEditText;
    private EditText rackEditText;
    private EditText rangeEditText;
    private EditText rigEditText;
    private EditText tallyGoalEditText;
    private EditText wallEditText;

    private String companyName = "";
    private String date = "";
    private String diameter = "";
    private String facility = "";
    private String grade = "";
    private String imperialAdjustment = "";
    private String imperialTallyGoal = "";
    private String metricAdjustment = "";
    private String metricTallyGoal = "";
    private String rack = "";
    private String range = "";
    private String rig = "";
    private String wall = "";

    //-----------------------------------------------------------------------------
    // EditJobActivity::EditJobActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public EditJobActivity()
    {

        layoutResID = R.layout.activity_edit_job;

        LOG_TAG = "EditJobActivity";

    }//end of EditJobActivity::EditJobActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

        getViewsFromLayout();

        addEditTextsToFocusArray();

    }//end of EditJobActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::onResume
    //
    // Automatically called upon activity resume.
    //
    // All functions that must be done upon activity resume should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        putJobInfoIntoLayout();

    }//end of EditJobActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    //
    // All functions that must be done upon activity destruction should be
    // called here.
    //

    @Override
    protected void onDestroy() {

        activitiesLaunched.getAndDecrement();

        super.onDestroy();

    }//end of EditJobActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::focusChanged
    //
    // Called when the focus changes from one view to another.
    //
    // Scrolls to the top of the scrollview if the view in focus is the first
    // EditText.
    //
    // Scrolls to the bottom of the scrollview if the view in focus is the last
    // EditText.
    //
    // This is done because when the user is using the keyboard for navigation, the
    // last and first EditTexts are are not fully brought into view.
    //

    @Override
    protected void focusChanged() {

        final ScrollView sv = (ScrollView)findViewById(R.id.jobInfoScrollView);

        int index = focusArray.indexOf(viewInFocus);
        if (index == 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_UP);
                }
            });
        }
        else if (index == (focusArray.size()-1)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

    }//end of EditJobActivity::focusChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleEscapeKeyPressed
    //
    // This function is overridden so that pressing the Escape key will have the
    // same effect as pressing the red X button.
    //

    @Override
    protected void handleEscapeKeyPressed() {

        handleRedXButtonPressed(null);

    }//end of EditJobActivity::handleEscapeKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleF3KeyPressed
    //
    // Perform a click on the ok button.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (okButton != null && okButton.isEnabled()) { performClickOnView(okButton); }

    }//end of EditJobActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request and result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

        if (pRequestCode == Keys.ACTIVITY_RESULT_JOB_INFO_MENU) {
            handleJobInfoMenuActivityResult(pData);
        }

    }//end of EditJobActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        super.onSaveInstanceState(pSavedInstanceState);

        getAndStoreJobInfoFromUserInput();

        //store necessary data
        pSavedInstanceState.putString(COMPANY_NAME_KEY, companyName);
        pSavedInstanceState.putString(DATE_KEY, date);
        pSavedInstanceState.putString(DIAMETER_KEY, diameter);
        pSavedInstanceState.putString(FACILITY_KEY, facility);
        pSavedInstanceState.putString(GRADE_KEY, grade);
        pSavedInstanceState.putString(IMPERIAL_ADJUSTMENT_KEY, imperialAdjustment);
        pSavedInstanceState.putString(IMPERIAL_TALLY_GOAL_KEY, imperialTallyGoal);
        pSavedInstanceState.putString(METRIC_ADJUSTMENT_KEY, metricAdjustment);
        pSavedInstanceState.putString(METRIC_TALLY_GOAL_KEY, metricTallyGoal);
        pSavedInstanceState.putString(RACK_KEY, rack);
        pSavedInstanceState.putString(RANGE_KEY, range);
        pSavedInstanceState.putString(RIG_KEY, rig);
        pSavedInstanceState.putString(WALL_KEY, wall);

    }//end of EditJobActivity::onSaveInstanceState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::restoreValuesFromSavedInstance
    //
    // Restores values using the passed in saved instance.
    //

    @Override
    protected void restoreValuesFromSavedInstance(Bundle pSavedInstanceState) {

        super.restoreValuesFromSavedInstance(pSavedInstanceState);

        companyName = pSavedInstanceState.getString(COMPANY_NAME_KEY);
        date = pSavedInstanceState.getString(DATE_KEY);
        diameter = pSavedInstanceState.getString(DIAMETER_KEY);
        facility = pSavedInstanceState.getString(FACILITY_KEY);
        grade = pSavedInstanceState.getString(GRADE_KEY);
        imperialAdjustment = pSavedInstanceState.getString(IMPERIAL_ADJUSTMENT_KEY);
        imperialTallyGoal = pSavedInstanceState.getString(IMPERIAL_TALLY_GOAL_KEY);
        metricAdjustment = pSavedInstanceState.getString(METRIC_ADJUSTMENT_KEY);
        metricTallyGoal = pSavedInstanceState.getString(METRIC_TALLY_GOAL_KEY);
        rack = pSavedInstanceState.getString(RACK_KEY);
        range = pSavedInstanceState.getString(RANGE_KEY);
        rig = pSavedInstanceState.getString(RIG_KEY);
        wall = pSavedInstanceState.getString(WALL_KEY);

    }//end of EditJobActivity::restoreValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::useActivityStartUpValues
    //
    // Uses activity start up values for variables.
    //
    // Activity dependent.
    //

    @Override
    protected void useActivityStartUpValues() {

        super.useActivityStartUpValues();

        getJobInfoFromHandler();

    }//end of EditJobActivity::useActivityStartUpValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::addEditTextsToFocusArray
    //
    // Add all of the EditText views to the focus array.
    //

    private void addEditTextsToFocusArray() {

        //should be entered in order they appear in layout
        focusArray.add(dateEditText);
        focusArray.add(tallyGoalEditText);
        focusArray.add(companyNameEditText);
        focusArray.add(adjustmentEditText);
        focusArray.add(diameterEditText);
        focusArray.add(wallEditText);
        focusArray.add(gradeEditText);
        focusArray.add(rangeEditText);
        focusArray.add(facilityEditText);
        focusArray.add(rackEditText);
        focusArray.add(rigEditText);

    }//end of EditJobActivity::addEditTextsToFocusArray
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent intent = new Intent();
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        setResult(Activity.RESULT_OK, intent);
        finish();

    }//end of EditJobActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::exitActivityByOk
    //
    // Saves the job and launches JobDisplayActivity.
    //
    // Used when the user closes the activity using the ok button.
    //

    private void exitActivityByOk() {

        getAndStoreJobInfoFromUserInput();

        jobsHandler.saveJob(companyName, date, diameter, facility, grade, imperialAdjustment,
                imperialTallyGoal, jobsHandler.getJobName(), metricAdjustment,
                metricTallyGoal, rack, range, rig, wall, false);

        Intent intent = new Intent();
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        setResult(Activity.RESULT_OK, intent);
        finish();

    }//end of EditJobActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::getAndStoreJobInfoFromUserInput
    //
    // Gets and stores the job info by retrieving the values entered by the user.
    //

    private void getAndStoreJobInfoFromUserInput() {

        companyName = companyNameEditText.getText().toString();
        date = dateEditText.getText().toString();
        diameter = diameterEditText.getText().toString();
        facility = facilityEditText.getText().toString();
        grade = gradeEditText.getText().toString();
        setAdjustmentValues(adjustmentEditText.getText().toString());
        rack = rackEditText.getText().toString();
        range = rangeEditText.getText().toString();
        rig = rigEditText.getText().toString();
        setTallyGoals(tallyGoalEditText.getText().toString());
        wall = wallEditText.getText().toString();

    }//end of EditJobActivity::getAndStoreJobInfoFromUserInput
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::getJobInfoFromHandler
    //
    // Gets and stores the job info from the jobs handler.
    //

    private void getJobInfoFromHandler() {

        companyName = jobsHandler.getCompanyName();
        date = jobsHandler.getDate(false);
        diameter = jobsHandler.getDiameter();
        facility = jobsHandler.getFacility();
        grade = jobsHandler.getGrade();
        imperialAdjustment = jobsHandler.getImperialAdjustment();
        imperialTallyGoal = jobsHandler.getImperialTallyGoal();
        metricAdjustment = jobsHandler.getMetricAdjustment();
        metricTallyGoal = jobsHandler.getMetricTallyGoal();
        rack = jobsHandler.getRack();
        range = jobsHandler.getRange();
        rig = jobsHandler.getRig();
        wall = jobsHandler.getWall();

    }//end of EditJobActivity::getJobInfoFromHandler
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::getViewsFromLayout
    //
    // Get all of the Views necessary from the xml layout using their ids and
    // assign pointers to them.
    //

    private void getViewsFromLayout() {

        okButton = (Button)findViewById(R.id.okButton);
        textViewJobName = (TextView)findViewById(R.id.textViewJobName);
        adjustmentEditText = (EditText)findViewById(R.id.editTextProtectorMakeupAdjustment);
        companyNameEditText = (EditText)findViewById(R.id.editTextCompanyName);
        dateEditText = (EditText)findViewById(R.id.editTextDate);
        diameterEditText = (EditText)findViewById(R.id.editTextDiameter);
        facilityEditText = (EditText)findViewById(R.id.editTextFacility);
        gradeEditText = (EditText)findViewById(R.id.editTextGrade);
        rackEditText = (EditText)findViewById(R.id.editTextRack);
        rangeEditText = (EditText)findViewById(R.id.editTextRange);
        rigEditText = (EditText)findViewById(R.id.editTextRig);
        tallyGoalEditText = (EditText)findViewById(R.id.editTextTallyGoal);
        wallEditText = (EditText)findViewById(R.id.editTextWall);

    }//end of EditJobActivity::getViewsFromLayout
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of EditJobActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleJobInfoMenuActivityResult
    //
    // Extracts the JobsHandler object from the passed in intent.
    //

    public void handleJobInfoMenuActivityResult(Intent pData) {

        jobsHandler = pData.getParcelableExtra(Keys.JOBS_HANDLER_KEY);

    }//end of EditJobActivity::handleJobInfoMenuActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleMenuButtonPressed
    //
    // Starts the JobInfoMenu activity.
    //

    public void handleMenuButtonPressed(View pView) {

        Intent intent = new Intent(this, JobInfoMenuActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_JOB_INFO_MENU);

    }//end of EditJobActivity::handleMenuButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of EditJobActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of EditJobActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::putJobInfoIntoLayout
    //
    // Puts all of the job info into the layout to be displayed by the user.
    //

    private void putJobInfoIntoLayout() {

        textViewJobName.setText(jobsHandler.getJobName());
        companyNameEditText.setText(companyName);
        dateEditText.setText(date);
        diameterEditText.setText(diameter);
        facilityEditText.setText(facility);
        gradeEditText.setText(grade);
        rackEditText.setText(rack);
        rangeEditText.setText(range);
        rigEditText.setText(rig);
        wallEditText.setText(wall);

        // use the metric or the imperial values
        // depending on the unit system
        String adjustment = "";
        String goal = "";
        if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
            adjustment = imperialAdjustment;
            goal = imperialTallyGoal;
        }
        else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
            adjustment = metricAdjustment;
            goal = metricTallyGoal;
        }
        adjustmentEditText.setText(adjustment);
        tallyGoalEditText.setText(goal);

    }//end of EditJobActivity::putJobInfoIntoLayout
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::setAdjustmentValues
    //
    // Sets the imperial and metric adjustment values using the passed in value.
    //
    // If the unit system is set to Imperial, then the passed in value is assumed
    // to be Imperial and is converted to Metric for the metric adjustment value.
    //
    // If the unit system is set to Metric, then the passed in value is assumed
    // to be Metric and is converted to Imperial for the imperial adjustment value.
    //

    private void setAdjustmentValues(String pValue) {

        //if the adjustment value hasn't changed,
        //then no action needs to be taken
        if ((sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)
                && pValue.equals(imperialAdjustment))
            || (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)
                && pValue.equals(metricAdjustment)))
        {
            return;
        }

        if (!(pValue.equals(""))) {

            Double adjDouble = Double.parseDouble(((EditText) findViewById
                                    (R.id.editTextProtectorMakeupAdjustment)).getText().toString());

            //Take different actions depending on the unit system
            if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
                //passed in value is assumed to be Imperial
                imperialAdjustment = Tools.IMPERIAL_TALLY_FORMAT.format(adjDouble);
                metricAdjustment = Tools.convertToMetricAndFormat(adjDouble);
            }
            else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
                //passed in value is assumed to be Metric
                metricAdjustment = Tools.METRIC_TALLY_FORMAT.format(adjDouble);
                imperialAdjustment = Tools.convertToImperialAndFormat(adjDouble);
            }

        }
        else {
            imperialAdjustment = "";
            metricAdjustment = "";
        }

    }//end of EditJobActivity::setAdjustmentValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // EditJobActivity::setTallyGoals
    //
    // Sets the imperial and metric tally goals using the passed in value.
    //
    // If the unit system is set to Imperial, then the passed in value is assumed
    // to be Imperial and is converted to Metric for the metric adjustment value.
    //
    // If the unit system is set to Metric, then the passed in value is assumed
    // to be Metric and is converted to Imperial for the imperial adjustment value.
    //

    private void setTallyGoals(String pValue) {

        //if the adjustment value hasn't changed,
        //then no action needs to be taken
        if ((sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)
                && pValue.equals(imperialTallyGoal))
                || (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)
                && pValue.equals(metricTallyGoal)))
        {
            return;
        }

        if (!(pValue.equals(""))) {

            Double goalDouble = Double.parseDouble(
                            ((EditText) findViewById(R.id.editTextTallyGoal)).getText().toString());

            //Take different actions depending on the unit system
            if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
                //passed in value is assumed to be Imperial
                imperialTallyGoal = Tools.IMPERIAL_TALLY_FORMAT.format(goalDouble);
                metricTallyGoal = Tools.convertToMetricAndFormat(goalDouble);
            }
            else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
                //passed in value is assumed to be Metric
                metricTallyGoal = Tools.METRIC_TALLY_FORMAT.format(goalDouble);
                imperialTallyGoal = Tools.convertToImperialAndFormat(goalDouble);
            }

        }

        else {
            imperialTallyGoal = "";
            metricTallyGoal = "";
        }

    }//end of EditJobActivity::setTallyGoals
    //-----------------------------------------------------------------------------

}//end of class EditJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------