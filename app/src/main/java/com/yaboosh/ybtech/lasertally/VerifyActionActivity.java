/******************************************************************************
 * Title: VerifyActionActivity.java
 * Author: Hunter Schoonover
 * Date: 09/27/14
 *
 * Purpose:
 *
 * This class is used as an activity dialog to display an action to the user
 * for verification. The user can click ok, cancel, or the red x button.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class VerifyActionActivity
//

public class VerifyActionActivity extends StandardActivity {

    public static final String TEXT_VIEW_TEXT_KEY = "TEXT_VIEW_TEXT_KEY";

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::VerifyActionActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public VerifyActionActivity()
    {

        layoutResID = R.layout.activity_verify_action;

        LOG_TAG = "VerifyActionActivity";

    }//end of VerifyActionActivity::VerifyActionActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        Button okButton = (Button)findViewById(R.id.okButton);
        if (okButton != null && okButton.isEnabled()) { okButton.performClick(); }

    }//end of VerifyActionActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //WIP HSS// -- add objects to focus array

        //get the text view text (activity to verify text) from the intent extras
        //and put it into the TextView to be displayed to users
        String textViewText = getIntent().getExtras().getString(TEXT_VIEW_TEXT_KEY);
        ((TextView)findViewById(R.id.verifyActionTextView)).setText(textViewText);

    }//end of VerifyActionActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of VerifyActionActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    // Sets the result to ok and finishes the activity.
    //

    private void exitActivityByOk() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }//end of VerifyActionActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of VerifyActionActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of VerifyActionActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // VerifyActionActivity::handleRedXButtonPressed
    //
    // Exits the activity by finish().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of VerifyActionActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class VerifyActionActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------