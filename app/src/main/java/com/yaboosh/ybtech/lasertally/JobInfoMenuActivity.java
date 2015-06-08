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

public class JobInfoMenuActivity extends StandardActivity {

    private String jobName;

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::JobInfoMenuActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public JobInfoMenuActivity()
    {

        layoutResID = R.layout.activity_job_info_menu;

        LOG_TAG = "JobInfoMenuActivity";

    }//end of JobInfoMenuActivity::JobInfoMenuActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of JobInfoMenuActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //WIP HSS// -- add objects to focus array

        jobName = getIntent().getExtras().getString(Keys.JOB_NAME_KEY);

    }//end of JobInfoMenuActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

        if (pRequestCode == Keys.ACTIVITY_RESULT_VERIFY_ACTION) {

            if (pResultCode == RESULT_OK) {
                handleVerifyActionResultOk();
            }
            else if (pResultCode == RESULT_CANCELED) {
                handleVerifyActionResultCancel();
            }

        }

        else {
            super.onActivityResult(pRequestCode, pResultCode, pData);
        }

    }//end of JobInfoMenuActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::deleteJob
    //
    // Deletes the passed in job by deleting its directory.
    //

    private void deleteJob(String pJobName) {

        try {

            Tools.deleteDirectory(new File(sharedSettings.getJobsFolderPath() + File.separator + pJobName));

        } catch (Exception e) {}

    }//end of JobInfoMenuActivity::deleteJob
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleCloseThisJobButtonPressed
    //
    // Starts the MainActivity.
    // Should be called from the "Close this job." button onClick().
    //

    public void handleCloseThisJobButtonPressed(View pView) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleCloseThisJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleCreateANewJobButtonPressed
    //
    // Starts the editJobInfoActivity in the CREATE_JOB mode.
    // Should be called from the "Create a new job." button onClick().
    //

    public void handleCreateANewJobButtonPressed(View pView) {

        Intent intent = new Intent(this, JobInfoActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY,
                                            JobInfoActivity.EditJobInfoActivityMode.CREATE_JOB);
        startActivity(intent);

    }//end of JobInfoMenuActivity::handleCreateANewJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleDeleteThisJobButtonPressed
    //
    // Deletes the directory for the current job and launches the MainActivity.
    // Should be called from the delete this job button onClick().
    //

    public void handleDeleteThisJobButtonPressed(View pView) {

        Intent intent = new Intent(this, VerifyActionActivity.class);
        intent.putExtra(VerifyActionActivity.TEXT_VIEW_TEXT_KEY,
                            "Are you sure that you want to delete " + "the job \""
                                            + jobName + "\"?  This cannot be undone.");
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_VERIFY_ACTION);

    }//end of JobInfoMenuActivity::handleDeleteThisJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleOpenJobButtonPressed
    //
    // Starts the OpenJobActivity.
    // Should be called from the open existing job button onClick().
    //

    public void handleOpenJobButtonPressed(View pView) {

        Intent intent = new Intent(this, OpenJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
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

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleVerifyActionResultCancel
    //
    // Does nothing.
    //

    public void handleVerifyActionResultCancel() {

    }//end of OpenJobActivity::handleVerifyActionResultCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // OpenJobActivity::handleVerifyActionResultOk
    //
    // Deletes the current job.
    //

    public void handleVerifyActionResultOk() {

        deleteJob(jobName);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of OpenJobActivity::handleVerifyActionResultOk
    //-----------------------------------------------------------------------------

}//end of class JobInfoMenuActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------