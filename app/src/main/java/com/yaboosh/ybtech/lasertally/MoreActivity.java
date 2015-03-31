/******************************************************************************
 * Title: MenuActivity.java
 * Author: Hunter Schoonover
 * Date: 02/20/15
 *
 * Purpose:
 *
 * This class is used as an activity to display a menu for the application
 * .
 * The menu displays buttons:
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
// class MenuActivity
//

public class MoreActivity extends Activity {

    public static final String LOG_TAG = "MoreActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;
    private JobInfo jobInfo;

    //-----------------------------------------------------------------------------
    // MenuActivity::MenuActivity (constructor)
    //

    public MoreActivity() {

        super();

    }//end of MenuActivity::MenuActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);

        setContentView(R.layout.activity_more);

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

        // Check whether we're recreating a previously destroyed instance
        if (pSavedInstanceState != null) {
            // Restore values from saved state

            jobInfo = pSavedInstanceState.getParcelable(Keys.JOB_INFO_KEY);
            sharedSettings = pSavedInstanceState.getParcelable(Keys.SHARED_SETTINGS_KEY);

        } else {
            //initialize members with default values for a new instance

            //Get the extras from the intent
            Bundle bundle = getIntent().getExtras();

            sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
            sharedSettings.setContext(this);

            jobInfo = bundle.getParcelable(Keys.JOB_INFO_KEY);

        }

    }//end of MenuActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

    }//end of MenuActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

    }//end of MenuActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

    }//end of MenuActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        //store necessary data
        pSavedInstanceState.putParcelable(Keys.JOB_INFO_KEY, jobInfo);
        pSavedInstanceState.putParcelable(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(pSavedInstanceState);

    }//end of MoreActivity::onSaveInstanceState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobDisplayActivity::onActivityResult
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

    }//end of JobDisplayActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::createUiChangeListener
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

    }//end of MenuActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::handleOptionsButtonPressed
    //
    // Starts the MoreOptionsActivity.
    //

    public void handleOptionsButtonPressed(View pView) {

        Intent intent = new Intent(this, MoreOptionsActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);
        startActivityForResult(intent, Keys.ACTIVITY_RESULT_MORE_OPTIONS);

    }//end of MenuActivity::handleOptionsButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::handlePrintButtonPressed
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

    }//end of MenuActivity::handlePrintButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuActivity::handleRedXButtonPressed
    //
    // Exits the activity with a RESULT_OK.
    //

    public void handleRedXButtonPressed(View pView) {

        Intent intent = new Intent();

        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        setResult(Activity.RESULT_OK, intent);

        finish();

    }//end of MenuActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MenuActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------