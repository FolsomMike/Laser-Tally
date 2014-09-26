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

public class OpenJobActivity extends Activity {

    public static final String TAG = "OpenJobActivity";

    private View decorView;
    private int uiOptions;

    public static final String JOB_INFO_INCLUDED = "JOB_INFO_INCLUDED";
    public static final String COMPANY_NAME_KEY = "COMPANY_NAME_KEY";
    public static final String DIAMETER_KEY = "DIAMETER_KEY";
    public static final String FACILITY_KEY = "FACILITY_KEY";
    public static final String GRADE_KEY = "GRADE_KEY";
    public static final String JOB_KEY =  "JOB_KEY";
    public static final String MAKEUP_ADJUSTMENT_KEY = "PROTECTOR_MAKE_UP_ADJUSTMENT_KEY";
    public static final String RACK_KEY = "RACK_KEY";
    public static final String RANGE_KEY = "RANGE_KEY";
    public static final String RIG_KEY = "RIG_KEY";
    public static final String WALL_KEY = "WALL_KEY";

    ArrayList<String> jobNames = new ArrayList<String>();

    ArrayList<String> fileLines = new ArrayList<String>();

    private String selectedJob;
    private String companyName;
    private String diameter;
    private String facility;
    private String grade;
    private String job;
    private String makeupAdjustment;
    private String rack;
    private String range;
    private String rig;
    private String wall;

    //-----------------------------------------------------------------------------
    // OpenJobActivity::OpenJobActivity (constructor)
    //

    public OpenJobActivity() {

        super();

    }//end of OpenJobActivity::OpenJobActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of onCreate :: " + TAG);

        setContentView(R.layout.activity_open_job);

        this.setFinishOnTouchOutside(false);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        createUiChangeListener();

    }//end of OpenJobActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of onDestroy :: " + TAG);

        super.onDestroy();

    }//end of OpenJobActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of onResume :: " + TAG);

        decorView.setSystemUiVisibility(uiOptions);

        getAndStoreJobs();
        addJobsToListView();

    }//end of OpenJobActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of onDestroy :: " + TAG);

    }//end of OpenJobActivity::onPause
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                handleJobSelected(((TextView)arg1).getText().toString());
                Log.d(TAG, "Job Selected: " + ((TextView)arg1).getText().toString());

            }

        });

    }//end of OpenJobActivity::addJobsToListView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::createUiChangeListener
    //
    // Listens for visibility changes in the ui.
    //
    // If the system bars are visible, the system visibility is set to the uiOptions.
    //
    //

    private void createUiChangeListener() {

        decorView.setOnSystemUiVisibilityChangeListener (
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int pVisibility) {

                        if ((pVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                    }

                });

    }//end of OpenJobActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::getAndStoreJobs
    //
    // Gets the directory names from the jobsDir directory and passes each name
    // into the storeJob() function to store the job name.
    //

    private void getAndStoreJobs() {

        try {

            // Retrieve/Create directory into internal memory;
            File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

            File[] files = jobsDir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    storeJob(f.getName());
                }
            }

        } catch (Exception e) {}


    }//end of OpenJobActivity::getAndStoreJobs
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleJobSelected
    //
    // Starts the JobDisplayActivity, putting the job information (gotten from the
    // jobInfo.txt file) of the selected job into the intent extras.
    //

    private void handleJobSelected(String pJobName) {

        selectedJob = pJobName;

        getJobInfoFromFile();

        Intent intent = new Intent(this, JobDisplayActivity.class);

        intent.putExtra(JOB_INFO_INCLUDED, true);
        intent.putExtra(COMPANY_NAME_KEY, companyName);
        intent.putExtra(DIAMETER_KEY, diameter);
        intent.putExtra(FACILITY_KEY, facility);
        intent.putExtra(GRADE_KEY,  grade);
        intent.putExtra(JOB_KEY, job);
        intent.putExtra(MAKEUP_ADJUSTMENT_KEY, makeupAdjustment);
        intent.putExtra(RACK_KEY, rack);
        intent.putExtra(RANGE_KEY, range);
        intent.putExtra(RIG_KEY, rig);
        intent.putExtra(WALL_KEY, wall);

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
    // OpenJobActivity::getJobInfoFromFile
    //
    // Gets and stores the job info by retrieving the values from the jobInfo.txt
    // file of the selected job.
    //

    private void getJobInfoFromFile() {

        try {
            fileLines.clear();

            // Retrieve/Create directory into internal memory;
            File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

            // Retrieve/Create sub-directory thisJobDir
            File thisJobDir = new File(jobsDir, "job=" + selectedJob);

            // Get a file jobInfoTextFile within the dir thisJobDir.
            File jobInfoTextFile = new File(thisJobDir, "jobInfo.txt");

            FileInputStream fStream = new FileInputStream(jobInfoTextFile);
            Scanner br = new Scanner(new InputStreamReader(fStream));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                Log.d(TAG, "New Line Found " + strLine);
                fileLines.add(strLine);
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "getJobInfoFromFile() FileNotFoundException " + e.toString());
        } catch (Exception e) {}

        // If there were no lines in the file,
        // this function is exited.
        if (fileLines.size() == 0) { return; }

        companyName = Tools.getValueFromList("Company Name", fileLines);
        diameter = Tools.getValueFromList("Diameter", fileLines);
        facility = Tools.getValueFromList("Facility", fileLines);
        grade = Tools.getValueFromList("Grade", fileLines);
        job = Tools.getValueFromList("Job", fileLines);
        makeupAdjustment = Tools.getValueFromList("Makeup Adjustment", fileLines);
        rack = Tools.getValueFromList("Rack", fileLines);
        range = Tools.getValueFromList("Range", fileLines);
        rig = Tools.getValueFromList("Rig", fileLines);
        wall = Tools.getValueFromList("Wall", fileLines);

    }//end of OpenJobActivity::getJobInfoFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::storeJob
    //
    // If the passed in directory name contains a job name, the job name is added
    // to the job names list.
    //

    private void storeJob(String pName) {

        if (!pName.contains("job=")) { return; }

        jobNames.add(Tools.extractValueFromString(pName));

        //Put jobNames in alphabetical order
        Collections.sort(jobNames);

    }//end of OpenJobActivity::storeJob
    //-----------------------------------------------------------------------------

}//end of class OpenJobActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------