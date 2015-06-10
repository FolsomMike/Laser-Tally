/******************************************************************************
 * Title: QuickActionActivity.java
 * Author: Hunter Schoonover
 * Date: 06/10/15
 *
 * Purpose:
 *
 * This class is used as an activity to display some quick action buttons and
 * actions for the application.
 *
 * It is intended to be launched whenever the user presses the F1 key from
 * anywhere within the app.
 * .
 * The activity displays buttons:
 *      Open Job
 *      Create Quick Job
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.content.Intent;
import android.view.View;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class QuickActionActivity
//

public class QuickActionActivity extends StandardActivity {

    //-----------------------------------------------------------------------------
    // QuickActionActivity::QuickActionActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public QuickActionActivity()
    {

        layoutResID = R.layout.activity_quick_action;

        LOG_TAG = "QuickActionActivity";

    }//end of QuickActionActivity::QuickActionActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed()
    {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of QuickActionActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions()
    {

        //add buttons to focus array
        focusArray.add(findViewById(R.id.openJobButton));
        focusArray.add(findViewById(R.id.createQuickJobButton));

    }//end of QuickActionActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::handleCreateQuickJobButtonPressed
    //
    // Creates a quick job and sends it over to JobDisplayActivity.
    //

    public void handleCreateQuickJobButtonPressed(View pView)
    {

        jobsHandler.createQuickJob();
        Intent intent = new Intent(this, JobDisplayActivity.class);
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of QuickActionActivity::handleCreateQuickJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::handleOpenJobButtonPressed
    //
    // Starts the OpenJobActivity.
    //

    public void handleOpenJobButtonPressed(View pView)
    {

        Intent intent = new Intent(this, OpenJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of QuickActionActivity::handleOpenJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::handleRedXButtonPressed
    //
    // Exits the activity.
    //

    public void handleRedXButtonPressed(View pView)
    {

        finish();

    }//end of QuickActionActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class QuickActionActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------