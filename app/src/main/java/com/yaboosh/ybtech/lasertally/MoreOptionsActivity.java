/******************************************************************************
 * Title: MenuOptionsActivity.java
 * Author: Hunter Schoonover
 * Date: 02/22/15
 *
 * Purpose:
 *
 * This class is used as an activity to display the menu options for the
 * application.
 *
 * The activity displays:
 *      Switch to Metric/Switch to Imperial button (shows one or the other)
 *      Minimum Measurement Allowed entry (double value entry...any measurement less than this will be ignored)
 *      Maximum Measurement Allowed entry (double value entry...any measurement greater than this will be ignored)
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MenuOptionsActivity
//

public class MoreOptionsActivity extends Activity {

    public static final String LOG_TAG = "MoreOptionsActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;
    private JobInfo jobInfo;

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::MenuOptionsActivity (constructor)
    //

    public MoreOptionsActivity() {

        super();

    }//end of MenuOptionsActivity::MenuOptionsActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more_options);

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
        sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
        jobInfo = bundle.getParcelable(Keys.JOB_INFO_KEY);

    }//end of MenuOptionsActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

    }//end of MenuOptionsActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onResume
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

    }//end of MenuOptionsActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

    }//end of MenuOptionsActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::createUiChangeListener
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

    }//end of MenuOptionsActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleOptionsButtonPressed
    //
    // Starts the MainActivity.
    // Should be called from the "Close this job." button onClick().
    //

    public void handleOptionsButtonPressed(View pView) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of MenuOptionsActivity::handleOptionsButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handlePrintButtonPressed
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

    }//end of MenuOptionsActivity::handlePrintButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        finish();

    }//end of MenuOptionsActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MenuOptionsActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------