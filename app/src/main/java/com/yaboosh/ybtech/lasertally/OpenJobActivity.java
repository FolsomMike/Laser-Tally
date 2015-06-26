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
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class OpenJobActivity
//

public class OpenJobActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    private MultiColumnListView listView;

    private ArrayList<String> jobNames = new ArrayList<String>();

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

        listView = (MultiColumnListView)findViewById(R.id.tallyDataListView);

        //load a list with ids to be used for each column
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(R.id.COLUMN_1);
        listView.init(this, R.layout.layout_single_column_list_view_row, 1, ids);
        listView.setSelectionStartingPosition(MultiColumnListView.STARTING_POSITION_FIRST_ROW);

        //assign a click listener to the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pParent, View pView, int pPos, long pId) {
                handleJobSelected(pPos);
            }
        });

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

        displayJobNames(jobsHandler.getAllJobs());

    }//end of OpenJobActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleArrowDownKeyPressed
    //
    // Selects the next row in the list view.
    //

    @Override
    protected void handleArrowDownKeyPressed() {

        listView.selectNextRow();

    }//end of OpenJobActivity::handleArrowDownKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleArrowUpKeyPressed
    //
    // Selects the previous row in the list view.
    //

    @Override
    protected void handleArrowUpKeyPressed() {

        listView.selectPreviousRow();

    }//end of OpenJobActivity::handleArrowUpKeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::handleF3KeyPressed
    //
    // Performs a click on the selected ListView row.
    //

    @Override
    protected void handleF3KeyPressed() {

        listView.clickSelectedRow();

    }//end of JobDisplayActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::displayJobNames
    //
    // Adds the job names in the passed in list to the job names list view.
    //
    // If there are no names in the list, a message is displayed to the user.
    //

    private void displayJobNames(final ArrayList<String> pNames) {

        TextView noJobs =  (TextView)findViewById(R.id.noJobsTextView);
        noJobs.setVisibility(View.GONE);

        jobNames = pNames;

        //if there are no jobs,
        //display a message to the user
        if (jobNames.isEmpty()) {
            listView.setVisibility(View.GONE);
            noJobs.setVisibility(View.VISIBLE);
            return;
        }

        readDataFromList(jobNames);

    }//end of OpenJobActivity::displayJobNames
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleJobSelected
    //
    // Starts the JobDisplayActivity, putting the job information (gotten from the
    // jobInfo.txt file) of the selected job into the intent extras.
    //

    private void handleJobSelected(int pPos) {

        listView.handleRowClicked(pPos);

        Intent intent = new Intent(this, JobDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        jobsHandler.loadJobFromFile(jobNames.get(pPos));
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

    //-----------------------------------------------------------------------------
    // OpenJobActivity::readDataFromList
    //
    // Reads the data from the passed in list and sends it to the list view to be
    // displayed.
    //

    private void readDataFromList(final ArrayList<String> pList)
    {

        ArrayList<SparseArray<String>> list = new ArrayList<SparseArray<String>>();

        for (int i=0; i<pList.size(); i++) {
            SparseArray<String> map = new SparseArray<String>();
            map.put(R.id.COLUMN_1, pList.get(i));
            list.add(map);
        }

        listView.setList(list);

    }//end of OpenJobActivity::readDataFromList
    //-----------------------------------------------------------------------------

}//end of class OpenJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------