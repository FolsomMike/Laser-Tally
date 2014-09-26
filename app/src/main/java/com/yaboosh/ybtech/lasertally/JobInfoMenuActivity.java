/******************************************************************************
 * Title: JobInfoMenuActivity.java
 * Author: Hunter Schoonover
 * Date: 09/26/14
 *
 * Purpose:
 *
 * This class is used as an activity to display the menu for the Job Info
 * activity.
 * The menu displays buttons:
 *      Open Existing Job
 *      Create New Job
 *      Close this Job
 *      Delete this Job
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

import java.io.File;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobInfoMenuActivity
//

public class JobInfoMenuActivity extends Activity {

    public static final String TAG = "JobInfoMenuActivity";

    private View decorView;
    private int uiOptions;

    private String job;

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::JobInfoMenuActivity (constructor)
    //

    public JobInfoMenuActivity() {

        super();

    }//end of JobInfoMenuActivity::JobInfoMenuActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of onCreate :: " + TAG);

        setContentView(R.layout.activity_job_info_menu);

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

        Bundle bundle = getIntent().getExtras();
        job = bundle.getString(JobInfoActivity.JOB_KEY);

    }//end of JobInfoMenuActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of onDestroy :: " + TAG);

        super.onDestroy();

    }//end of JobInfoMenuActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::onResume
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

    }//end of JobInfoMenuActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of onPause :: " + TAG);

    }//end of JobInfoMenuActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::createUiChangeListener
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

    }//end of JobInfoMenuActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleCloseThisJobButtonPressed
    //
    // Starts the MainActivity.
    // Should be called from the "Close this job." button onClick().
    //

    public void handleCloseThisJobButtonPressed(View pView) {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleCloseThisJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleCreateANewJobButtonPressed
    //
    // Starts an activity for Job Info.
    // Should be called from the "Create a new job." button onClick().
    //

    public void handleCreateANewJobButtonPressed(View pView) {

        Intent intent = new Intent(this, CreateJobActivity.class);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleCreateANewJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleDeleteThisJobButtonPressed
    //
    // Deletes the directory for the current job and launches the MainActivity.
    // Should be called from the "Delete this job." button onClick().
    //

    public void handleDeleteThisJobButtonPressed(View pView) {

        try {

            File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

            File thisJobDir = new File(jobsDir, job);
            Tools.deleteDirectory(thisJobDir);

        } catch (Exception e) {}

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleDeleteThisJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleOpenJobButtonPressed
    //
    // Starts the OpenJobActivity.
    // Should be called from the "Open existing job." button onClick().
    //

    public void handleOpenJobButtonPressed(View pView) {

        Intent intent = new Intent(this, OpenJobActivity.class);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleOpenJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        finish();

    }//end of JobInfoMenuActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class JobInfoMenuActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------