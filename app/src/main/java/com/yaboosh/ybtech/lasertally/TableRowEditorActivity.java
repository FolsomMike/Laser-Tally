/******************************************************************************
 * Title: TableRowEditorActivity.java
 * Author: Hunter Schoonover
 * Date: 09/14/14
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to edit the Pipe # and Total Length of the row selected in the
 * MainActivity.
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TableRowEditorActivity
//

public class TableRowEditorActivity extends StandardActivity {

    public static final String PIPE_NUMBER_KEY =  "PIPE_NUMBER_KEY";
    public static final String RENUMBER_ALL_CHECKBOX_KEY = "RENUMBER_ALL_CHECKBOX_KEY";
    public static final String TOTAL_LENGTH_KEY = "TOTAL_LENGTH_KEY";

    private String pipeNumber = "";
    private String totalLength = "";
    private boolean renumberAll = false;

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::TableRowEditorActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public TableRowEditorActivity()
    {

        layoutResID = R.layout.activity_table_row_editor;

        LOG_TAG = "TableRowEditorActivity";

    }//end of TableRowEditorActivity::TableRowEditorActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleF3KeyPressed
    //
    // Perform a click on the ok button.
    //

    @Override
    protected void handleF3KeyPressed() {

        Button okButton = (Button) findViewById(R.id.okButton);
        if (okButton != null) { okButton.performClick(); }

    }//end of TableRowEditorActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //WIP HSS// -- add objects to focus array

        setPipeNumberAndTotalLengthValues();

    }//end of TableRowEditorActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::restoreActivitySpecificValuesFromSavedInstance
    //
    // Restores values using the passed in saved instance.
    //

    @Override
    protected void restoreActivitySpecificValuesFromSavedInstance(Bundle pSavedInstanceState) {

        pipeNumber = pSavedInstanceState.getString(PIPE_NUMBER_KEY);
        totalLength = pSavedInstanceState.getString(TOTAL_LENGTH_KEY);

    }//end of TableRowEditorActivity::restoreActivitySpecificValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::storeActivitySpecificValuesFromSavedInstance
    //
    // Stores activity specific values in the passed in saved instance.
    //

    @Override
    protected void storeActivitySpecificValuesToSavedInstance(Bundle pSavedInstanceState) {

        getAndStoreInfoFromUserInput();

        //store necessary data
        pSavedInstanceState.putString(PIPE_NUMBER_KEY, pipeNumber);
        pSavedInstanceState.putString(TOTAL_LENGTH_KEY, totalLength);

    }//end of TableRowEditorActivity::storeActivitySpecificValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::useActivitySpecificActivityStartUpValues
    //
    // Uses activity start up values for variables.
    //
    // Activity dependent.
    //

    @Override
    protected void useActivitySpecificActivityStartUpValues() {

        //Get the pipe number and total length
        //sent to this activity from its parent
        Bundle bundle = getIntent().getExtras();
        pipeNumber = bundle.getString(PIPE_NUMBER_KEY);
        totalLength = bundle.getString(TOTAL_LENGTH_KEY);

    }//end of TableRowEditorActivity::useActivitySpecificActivityStartUpValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of TableRowEditorActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    // Puts the pipe number and total length into the intent extras, sets the
    // result to ok, and finishes the activity.
    //

    private void exitActivityByOk() {

        getAndStoreInfoFromUserInput();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(PIPE_NUMBER_KEY, pipeNumber);
        resultIntent.putExtra(TOTAL_LENGTH_KEY, totalLength);
        resultIntent.putExtra(RENUMBER_ALL_CHECKBOX_KEY, renumberAll);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }//end of TableRowEditorActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::getAndStoreInfoFromUserInput
    //
    // Gets and stores all of the values entered by the user.
    //

    private void getAndStoreInfoFromUserInput() {

        TextView pPN = (TextView)findViewById(R.id.editTextPipeNumber);
        pipeNumber = pPN.getText().toString();
        TextView pTL = (TextView)findViewById(R.id.editTextTotalLength);
        totalLength = pTL.getText().toString();
        CheckBox checkBox = (CheckBox)findViewById(R.id.checkBoxRenumberAllBelow);
        renumberAll = checkBox.isChecked();

    }//end of TableRowEditorActivity::getAndStoreInfoFromUserInput
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of TableRowEditorActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of TableRowEditorActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of TableRowEditorActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::setPipeNumberAndTotalLengthValues
    //
    // Sets the pipe number and total length edit text values to pipeNumber and
    // and totalLength.
    //

    private void setPipeNumberAndTotalLengthValues() {

        TextView pPN = (TextView)findViewById(R.id.editTextPipeNumber);
        pPN.setText(pipeNumber);
        TextView pTL = (TextView)findViewById(R.id.editTextTotalLength);
        pTL.setText(totalLength);

    }//end of TableRowEditorActivity::setPipeNumberAndTotalLengthValues
    //-----------------------------------------------------------------------------

}//end of class TableRowEditorActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------