/******************************************************************************
 * Title: MoreActivity.java
 * Author: Hunter Schoonover
 * Date: 02/20/15
 *
 * Purpose:
 *
 * This class is used as an activity to display more buttons and actions
 * for the application.
 * .
 * The activity displays buttons:
 *      Print
 *      Options
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MoreActivity
//

public class MoreActivity extends StandardActivity {

    //-----------------------------------------------------------------------------
    // MoreActivity::MoreActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public MoreActivity()
    {

        layoutResID = R.layout.activity_more;

        LOG_TAG = "MoreActivity";

    }//end of MoreActivity::MoreActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { viewInFocus.performClick(); }

    }//end of MoreActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //add buttons to focus array
        focusArray.add(findViewById(R.id.printButton));
        focusArray.add(findViewById(R.id.optionsButton));

    }//end of MoreActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {

        if (pRequestCode == Keys.ACTIVITY_RESULT_MORE_OPTIONS) {

            if (pResultCode == RESULT_OK) {
                sharedSettings = pData.getParcelableExtra(Keys.SHARED_SETTINGS_KEY);
            }
            else if (pResultCode == RESULT_CANCELED) {}

        }
        else {

            super.onActivityResult(pRequestCode, pResultCode, pData);

        }

    }//end of MoreActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::handleOptionsButtonPressed
    //
    // Starts the MoreOptionsActivity.
    //

    public void handleOptionsButtonPressed(View pView) {

        Intent intent = new Intent(this, MoreOptionsActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_MORE_OPTIONS);

    }//end of MoreActivity::handleOptionsButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::handlePrintButtonPressed
    //
    // Prints the tally data saved to file.
    //

    public void handlePrintButtonPressed(View pView) {

        TallyReportHTMLPrintoutMaker tallyReportMaker = new
                                            TallyReportHTMLPrintoutMaker(sharedSettings, jobInfo);
        tallyReportMaker.init();
        tallyReportMaker.printTallyReport();

        //use this code block to save a tally report to an HTML file -- mainly used for debugging
        /*TallyReportHTMLFileMaker tallyReportFileMaker = new TallyReportHTMLFileMaker(
                sharedSettings, measurementsTable, companyName, jobName, "",  adjustmentValue, tallyGoal);
        tallyReportFileMaker.init();
        tallyReportFileMaker.printTallyReport();*/

    }//end of MoreActivity::handlePrintButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::handleRedXButtonPressed
    //
    // Exits the activity with a RESULT_OK.
    //

    public void handleRedXButtonPressed(View pView) {

        Intent intent = new Intent();

        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        setResult(Activity.RESULT_OK, intent);

        finish();

    }//end of MoreActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MoreActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------