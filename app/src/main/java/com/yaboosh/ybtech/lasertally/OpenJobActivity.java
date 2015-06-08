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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yaboosh.ybtech.lasertally.util.SystemUiHider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class OpenJobActivity
//

public class OpenJobActivity extends StandardActivity {

    ArrayList<String> jobNames = new ArrayList<String>();

    private String selectedJobDirectoryPath;
    private String selectedJobInfoFilePath;

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

        //WIP HSS// -- add objects to focus array

    }//end of OpenJobActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::performOnResumeActivitySpecificActions
    //
    // All actions that must be done upon activity resume should be done here.
    //

    @Override
    protected void performOnResumeActivitySpecificActions() {

        getAndStoreJobs();
        addJobsToListView();

    }//end of OpenJobActivity::performOnResumeActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::addJobsToListView
    //
    // Adds the job names to the job names list view.
    // If there are no names in the jobNames list, the ListView height is set
    // to "fill_parent" and a Tex
    //

    private void addJobsToListView() {

        ListView listView = (ListView)findViewById(R.id.jobNamesListView);
        TextView textView =  (TextView)findViewById(R.id.noJobsTextView);

        if (jobNames.size() == 0) {
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            return;
        }

        listView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                                            R.layout.text_view_template,
                                                            jobNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                handleJobSelected(((TextView) arg1).getText().toString());
                Log.d(LOG_TAG, "Job Selected: " + ((TextView) arg1).getText().toString());

            }

        });

    }//end of OpenJobActivity::addJobsToListView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::getAndStoreJobs
    //
    // Gets the directory names from the jobsDir directory and passes each name
    // into the storeJob() function to store the job name.
    //

    private void getAndStoreJobs() {

        jobNames.clear();

        try {

            // Retrieve the jobs directory
            File jobsDir = new File (sharedSettings.getJobsFolderPath());

            // All of the directories in the jobs directory
            // are jobs, so they are stored as such
            File[] files = jobsDir.listFiles();
            for (File f : files) { if (f.isDirectory()) { storeJob(f.getName()); } }

        } catch (Exception e) {}


    }//end of OpenJobActivity::getAndStoreJobs
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::getJobInfoFromFile
    //
    // Gets and stores the job info by retrieving the values from the jobInfo.txt
    // file of the selected job.
    //

    private void getJobInfoFromFile() {

        ArrayList<String> fileLines = new ArrayList<String>();

        try {

            //Retrieve the job info file for the selected job
            File file = new File(selectedJobInfoFilePath);

            // read the data from the file into an ArrayList
            FileInputStream fStream = new FileInputStream(file);
            Scanner br = new Scanner(new InputStreamReader(fStream));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                Log.d(LOG_TAG, "New Line Found " + strLine); //debug hss//
                fileLines.add(strLine);
            }

        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "getJobInfoFromFile() FileNotFoundException " + e.toString());
        } catch (Exception e) {}

        // If there were no lines in the file,
        // this function is exited.
        if (fileLines.isEmpty()) { return; }

        companyName = Tools.getValueFromList("Company Name", fileLines);
        date = Tools.getValueFromList("Date", fileLines);
        diameter = Tools.getValueFromList("Diameter", fileLines);
        facility = Tools.getValueFromList("Facility", fileLines);
        grade = Tools.getValueFromList("Grade", fileLines);
        imperialAdjustment = Tools.getValueFromList("Imperial Adjustment", fileLines);
        imperialTallyGoal = Tools.getValueFromList("Imperial Tally Goal", fileLines);
        job = Tools.getValueFromList("Job", fileLines);
        metricAdjustment = Tools.getValueFromList("Metric Adjustment", fileLines);
        metricTallyGoal = Tools.getValueFromList("Metric Tally Goal", fileLines);
        rack = Tools.getValueFromList("Rack", fileLines);
        range = Tools.getValueFromList("Range", fileLines);
        rig = Tools.getValueFromList("Rig", fileLines);
        wall = Tools.getValueFromList("Wall", fileLines);

    }//end of OpenJobActivity::getJobInfoFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleJobSelected
    //
    // Starts the JobDisplayActivity, putting the job information (gotten from the
    // jobInfo.txt file) of the selected job into the intent extras.
    //

    private void handleJobSelected(String pJobName) {

        selectedJobDirectoryPath = sharedSettings.getJobsFolderPath() + File.separator + pJobName;
        selectedJobInfoFilePath = selectedJobDirectoryPath + File.separator + pJobName + " ~ JobInfo.txt";

        getJobInfoFromFile();

        Intent intent = new Intent(this, JobDisplayActivity.class);

        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        JobInfo jobInfo = new JobInfo(selectedJobDirectoryPath, companyName, date, diameter,
                                        facility, grade, imperialAdjustment, imperialTallyGoal, job,
                                        metricAdjustment, metricTallyGoal, rack, range, rig, wall);
        jobInfo.init();
        intent.putExtra(Keys.JOB_INFO_INCLUDED_KEY, true);
        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);

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
    // OpenJobActivity::storeJob
    //
    // Store the passed in job name to the list.
    //

    private void storeJob(String pName) {

        jobNames.add(Tools.extractValueFromString(pName));

        //Put jobNames in alphabetical order
        Collections.sort(jobNames);

    }//end of OpenJobActivity::storeJob
    //-----------------------------------------------------------------------------

}//end of class OpenJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------