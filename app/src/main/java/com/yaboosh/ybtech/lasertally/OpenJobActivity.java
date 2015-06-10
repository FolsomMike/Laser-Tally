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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class OpenJobActivity
//

public class OpenJobActivity extends StandardActivity {

    private String companyName;
    private String date;
    private String diameter;
    private String facility;
    private String grade;
    private String imperialAdjustment;
    private String imperialTallyGoal;
    private String job;
    private String metricAdjustment;
    private String metricTallyGoal;
    private String rack;
    private String range;
    private String rig;
    private String wall;

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
    // OpenJobActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of OpenJobActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

    }//end of OpenJobActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::performOnResumeActivitySpecificActions
    //
    // All actions that must be done upon activity resume should be done here.
    //

    @Override
    protected void performOnResumeActivitySpecificActions() {

        addJobsToListView(jobsHandler.getAllJobs());

    }//end of OpenJobActivity::performOnResumeActivitySpecificActions
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

        Intent intent = new Intent(this, JobDisplayActivity.class);

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