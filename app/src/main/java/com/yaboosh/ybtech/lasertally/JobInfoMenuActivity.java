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
    // JobInfoMenuActivity::handleEscapeKeyPressed
    //
    // This function is overridden so that pressing the Escape key will have the
    // same effect as pressing the red X button.
    //

    @Override
    protected void handleEscapeKeyPressed() {

        handleRedXButtonPressed(null);

    }//end of JobInfoMenuActivity::handleEscapeKeyPressed
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
    // JobInfoMenuActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request and result codes.
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

        else if (pRequestCode == Keys.ACTIVITY_RESULT_RENAME_JOB) {
            handleRenameJobActivityResult(pData);
        }

        else {
            super.onActivityResult(pRequestCode, pResultCode, pData);
        }

    }//end of JobInfoMenuActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //add buttons to focus array
        focusArray.add(findViewById(R.id.openJobButton));
        focusArray.add(findViewById(R.id.createNewJobButton));
        focusArray.add(findViewById(R.id.renameJobButton));
        focusArray.add(findViewById(R.id.deleteJobButton));

    }//end of JobInfoMenuActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleRenameThisJobButtonPressed
    //
    // Launches the RenameJobActivity.
    //
    // Should be called from the rename job button onClick().
    //

    public void handleRenameThisJobButtonPressed(View pView) {

        Intent intent = new Intent(this, RenameJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_RENAME_JOB);

    }//end of JobInfoMenuActivity::handleRenameThisJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleCreateANewJobButtonPressed
    //
    // Launches CreateJobActivity.
    //
    // Should be called from the create a new job button onClick().
    //

    public void handleCreateANewJobButtonPressed(View pView) {

        Intent intent = new Intent(this, CreateJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
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
                            "Are you sure that you want to delete the job " +
                                "\"" + jobsHandler.getJobName() + "\"?  This cannot be undone.");
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
    // Exits the activity.
    //

    public void handleRedXButtonPressed(View pView) {

        Intent intent = new Intent();
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        setResult(Activity.RESULT_OK, intent);
        finish();

    }//end of JobInfoMenuActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoMenuActivity::handleRenameJobActivityResult
    //
    // Extracts the JobsHandler object from the passed in intent.
    //

    public void handleRenameJobActivityResult(Intent pData) {

        jobsHandler = pData.getParcelableExtra(Keys.JOBS_HANDLER_KEY);

    }//end of JobInfoMenuActivity::handleRenameJobActivityResult
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

        jobsHandler.deleteCurrentJob();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of OpenJobActivity::handleVerifyActionResultOk
    //-----------------------------------------------------------------------------

}//end of class JobInfoMenuActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------