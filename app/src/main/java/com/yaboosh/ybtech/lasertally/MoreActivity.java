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
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MoreActivity
//

public class MoreActivity extends StandardActivity {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

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
    // MoreActivity::onCreate
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
        focusArray.add(findViewById(R.id.printButton));
        focusArray.add(findViewById(R.id.optionsButton));

    }//end of MoreActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::onDestroy
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

    }//end of MoreActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::handleF3KeyPressed
    //
    // If a view is in focus, perform a click on that view.
    //

    @Override
    protected void handleF3KeyPressed() {

        if (viewInFocus != null) { performClickOnView(viewInFocus); }

    }//end of MoreActivity::handleF3KeyPressed
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

        if (pRequestCode == Keys.ACTIVITY_RESULT_MORE_OPTIONS && pResultCode == RESULT_OK) {
            sharedSettings = pData.getParcelableExtra(Keys.SHARED_SETTINGS_KEY);
        }
        else { super.onActivityResult(pRequestCode, pResultCode, pData); }

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
        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
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
                                            TallyReportHTMLPrintoutMaker(sharedSettings, jobsHandler);
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