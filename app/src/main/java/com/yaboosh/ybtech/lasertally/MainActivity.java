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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends StandardActivity {

    //-----------------------------------------------------------------------------
    // MainActivity::MainActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public MainActivity()
    {

        layoutResID = R.layout.activity_main;

        LOG_TAG = "MainActivity";

    }//end of MainActivity::MainActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleEscapeKeyPressed
    //
    // This functions is overridden and left blank so that the user cannot use
    // the escape key to exit the activity.
    //

    protected void handleEscapeKeyPressed() {

    }//end of MainActivity::handleEscapeKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of MainActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //add buttons to focus array
        focusArray.add(findViewById(R.id.createNewJobButton));
        focusArray.add(findViewById(R.id.openJobButton));

    }//end of MainActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleCreateNewJobButtonPressed
    //
    // Starts CreateJobActivity.
    //
    // Should be called from the "Create new job." button onClick().
    //

    public void handleCreateNewJobButtonPressed(View pView) {

        Intent intent = new Intent(this, CreateJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of MainActivity::handleCreateNewJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleOpenAnExistingJobButtonPressed
    //
    // Starts the OpenJobActivity.
    // Should be called from the "Open existing job." button onClick().
    //

    public void handleOpenAnExistingJobButtonPressed(View pView) {

        Intent intent = new Intent(this, OpenJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of MainActivity::handleOpenAnExistingJobButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
