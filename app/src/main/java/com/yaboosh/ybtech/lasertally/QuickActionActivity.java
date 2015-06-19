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
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class QuickActionActivity
//

public class QuickActionActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

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
    // QuickActionActivity::onCreate
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
        focusArray.add(findViewById(R.id.openJobButton));
        focusArray.add(findViewById(R.id.createQuickJobButton));

    }//end of QuickActionActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::onDestroy
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

    }//end of QuickActionActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // QuickActionActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed()
    {

        if (viewInFocus != null) { performClickOnView(viewInFocus); }

    }//end of QuickActionActivity::handleF3KeyPressed
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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