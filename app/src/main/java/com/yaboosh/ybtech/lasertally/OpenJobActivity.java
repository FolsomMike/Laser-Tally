/******************************************************************************
 * Title: OpenJobActivity.java
 * Author: Hunter Schoonover
 * Date: 09/26/14
 *
 * Purpose:
 *
 * This class is used as an activity to display jobs for the user to select
 * and open.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class OpenJobActivity
//

public class OpenJobActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    //-----------------------------------------------------------------------------
    // OpenJobActivity::OpenJobActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public OpenJobActivity()
    {

        layoutResID = R.layout.activity_open_job;

        LOG_TAG = "OpenJobActivity";

    }//end of OpenJobActivity::OpenJobActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onCreate
    //
    // Automatically called when the activity is created.
    //
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        if (activitiesLaunched.incrementAndGet() > 1) { finish(); }

        super.onCreate(pSavedInstanceState);

    }//end of OpenJobActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onDestroy
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

    }//end of OpenJobActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onResume
    //
    // Automatically called upon activity resume.
    //
    // All functions that must be done upon activity resume should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        addJobsToListView(jobsHandler.getAllJobs());

    }//end of OpenJobActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { performClickOnView(viewInFocus); }

    }//end of OpenJobActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onClickListener
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

        @Override
        public void onClick(View pV) {

            int id = pV.getId();

            if (id == R.id.jobNameTextView) {
                handleJobSelected(((TextView) pV).getText().toString());
            }

        }

    };//end of OpenJobActivity::onClickListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::addJobsToListView
    //
    // Adds the job names in the passed in list to the job names list view.
    //
    // If there are no names in the list, a message is displayed to the user.
    //

    private void addJobsToListView(ArrayList<String> pNames) {

        TextView textView =  (TextView)findViewById(R.id.noJobsTextView);
        textView.setVisibility(View.GONE);

        //if there are no jobs,
        //display a message to the user
        if (pNames.isEmpty()) { textView.setVisibility(View.VISIBLE); return; }

        LinearLayout layout = (LinearLayout)findViewById(R.id.jobNamesLayout);
        for (String j : pNames) { layout.addView(createJobNameTextView(j)); }

    }//end of OpenJobActivity::addJobsToListView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::createJobNameTextView
    //
    // Returns a selectable text view containing the passed in string.
    //
    // A pointer to the created TextView is added to the focus array.
    //

    private TextView createJobNameTextView(String pString) {

        TextView t = (TextView)getLayoutInflater().inflate
                                                    (R.layout.selectable_text_view_template, null);
        t.setId(R.id.jobNameTextView);
        t.setClickable(true);
        t.setFocusable(true);
        t.setFocusableInTouchMode(false);
        t.setOnClickListener(onClickListener);
        t.setText(pString);

        focusArray.add(t);

        return t;

    }//end of OpenJobActivity::createJobNameTextView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleJobSelected
    //
    // Starts the JobDisplayActivity, putting the job information (gotten from the
    // jobInfo.txt file) of the selected job into the intent extras.
    //

    private void handleJobSelected(String pJobName) {

        MultiColumnListView.clearSelectionValues();

        Intent intent = new Intent(this, JobDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        jobsHandler.loadJobFromFile(pJobName);
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        startActivity(intent);

    }//end of OpenJobActivity::handleJobSelected
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleRedXButtonPressed
    //
    // Exits the activity by finish().
    //

    public void handleRedXButtonPressed(View pView) {

        finish();

    }//end of OpenJobActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class OpenJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------