/******************************************************************************
 * Title: RenameJobActivity.java
 * Author: Hunter Schoonover
 * Date: 06/11/15
 *
 * Purpose:
 *
 * This class is used as an activity to display an edit text field for the
 * user to change the job name.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class RenameJobActivity
//

public class RenameJobActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    public static final String NEW_JOB_NAME_KEY =  "NEW_JOB_NAME_KEY";

    private EditText editTextJobName;

    private String jobName = "";

    //-----------------------------------------------------------------------------
    // RenameJobActivity::RenameJobActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public RenameJobActivity()
    {

        layoutResID = R.layout.activity_rename_job;

        LOG_TAG = "RenameJobActivity";

    }//end of RenameJobActivity::RenameJobActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

        //assign pointers to Views
        editTextJobName = (EditText)findViewById(R.id.editTextJobName);

        //add objects to focus array
        focusArray.add(editTextJobName);

        //put the current job name into the edit text field
        editTextJobName.setText(jobName);

        //Add a listener to the job name edit text field to listen for changes
        editTextJobName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable pE) {

                handleEditTextJobTextChanged(pE.toString(), pE.length());

            }

            public void beforeTextChanged(CharSequence pS, int pStart, int pCount, int pAfter) {
            }

            public void onTextChanged(CharSequence pS, int pStart, int pBefore, int pCount) {
            }
        });

    }//end of RenameJobActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::onDestroy
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

    }//end of RenameJobActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::handleF3KeyPressed
    //
    // Perform a click on the ok button.
    //

    @Override
    protected void handleF3KeyPressed() {

        Button okButton = (Button) findViewById(R.id.okButton);
        if (okButton != null && okButton.isEnabled()) { performClickOnView(viewInFocus); }

    }//end of RenameJobActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        super.onSaveInstanceState(pSavedInstanceState);

        jobName = editTextJobName.getText().toString();

        //store necessary data
        pSavedInstanceState.putString(NEW_JOB_NAME_KEY, jobName);

    }//end of RenameJobActivity::onSaveInstanceState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::restoreValuesFromSavedInstance
    //
    // Restores values using the passed in saved instance.
    //

    @Override
    protected void restoreValuesFromSavedInstance(Bundle pSavedInstanceState) {

        super.restoreValuesFromSavedInstance(pSavedInstanceState);

        jobName = pSavedInstanceState.getString(NEW_JOB_NAME_KEY);

    }//end of RenameJobActivity::restoreValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::useActivityStartUpValues
    //
    // Uses activity start up values for variables.
    //
    // Activity dependent.
    //

    @Override
    protected void useActivityStartUpValues() {

        super.useActivityStartUpValues();

        jobName = jobsHandler.getJobName();

    }//end of RenameJobActivity::useActivityStartUpValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::enableOkButton
    //
    // Sets the ok button to enabled or disabled depending on the passed in
    // boolean.
    //

    private void enableOkButton(boolean pBool) {

        Button b = (Button)findViewById(R.id.okButton);

        if (pBool) {
            b.setEnabled(true);
            b.setTextAppearance(getApplicationContext(), R.style.whiteStyledButton);
        } else {
            b.setEnabled(false);
            b.setTextAppearance(getApplicationContext(), R.style.disabledStyledButton);
        }

    }//end of RenameJobActivity::enableOkButton
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::exitActivity
    //
    // Puts the JobsHandler into the result intent and finishes the activity.
    //

    private void exitActivity() {

        jobsHandler.renameJob(jobName);

        Intent intent = new Intent();
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        setResult(Activity.RESULT_OK, intent);
        finish();

    }//end of RenameJobActivity::exitActivity
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivity();

    }//end of RenameJobActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::handleEditTextJobTextChanged
    //
    // Determines whether or not the ok button should be enabled and whether or
    // not the jobNameAlreadyTextView should be set visible.
    //
    // Called when the text in the EditText used for the Job name is changed.
    //

    private void handleEditTextJobTextChanged(String pJobName, int pLength) {

        jobName = pJobName;

        Boolean enableOkButton = false;
        Boolean jobExistsBool = false;

        // Check to see if the job name already exists and to see if the
        // user did not just retype the original name of the job.
        if (!jobsHandler.getJobName().equals(jobName)
                && jobsHandler.checkIfJobExists(jobName))
        {
            jobExistsBool = true;
        }

        // Check to see if the length of the edit text is greater than
        // 0 and to see if the job does not already exist.
        if (pLength > 0 && !jobExistsBool) { enableOkButton = true; }

        enableOkButton(enableOkButton);

        TextView exists = (TextView) findViewById(R.id.jobNameAlreadyExistsTextView);
        if (jobExistsBool) { exists.setVisibility(View.VISIBLE); }
        else { exists.setVisibility(View.GONE); }

        TextView blank = (TextView) findViewById(R.id.jobNameCannotBeBlankTextView);
        if (pLength <= 0) { blank.setVisibility(View.VISIBLE); }
        else { blank.setVisibility(View.GONE); }

    }//end of RenameJobActivity::handleEditTextJobTextChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivity();

    }//end of RenameJobActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // RenameJobActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivity();

    }//end of RenameJobActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class RenameJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------