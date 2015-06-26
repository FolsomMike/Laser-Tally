/******************************************************************************
* Title: MainActivity.java
* Author: Hunter Schoonover
* Date: 7/22/14
*
* Purpose:
*
* This class creates the main activity for the application. It is created and
* used upon app startup.
*
* Displays two buttons:
*       open existing job
*       create new job
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

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
    // MainActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

        //add buttons to focus array
        focusArray.add(findViewById(R.id.createNewJobButton));
        focusArray.add(findViewById(R.id.openJobButton));

    }//end of MainActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onDestroy
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

    }//end of MainActivity::onDestroy
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

        if (viewInFocus != null) { performClickOnView(viewInFocus); }

    }//end of MainActivity::handleF3KeyPressed
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
