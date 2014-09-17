/******************************************************************************
 * Title: JobInfoActivity.java
 * Author: Hunter Schoonover
 * Date: 09/15/14
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to edit job info.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobInfoActivity
//

public class JobInfoActivity extends Activity {

    public static final String TAG = "TableRowEditorActivity";

    public static final int TABLE_ROW_EDITOR = 1234;
    public static final String PIPE_NUMBER_KEY =  "PIPE_NUMBER_KEY";
    public static final String RENUMBER_ALL_CHECKBOX_KEY = "RENUMBER_ALL_CHECKBOX_KEY";
    public static final String TOTAL_LENGTH_KEY = "TOTAL_LENGTH_KEY";

    private String pipeNumber = "";
    private String totalLength = "";
    private boolean renumberAll;

    //-----------------------------------------------------------------------------
    // JobInfoActivity::JobInfoActivity (constructor)
    //

    public JobInfoActivity() {

        super();

    }//end of JobInfoActivity::JobInfoActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of TableRowEditorActivity onCreate");

        setContentView(R.layout.activity_job_info);

        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setFinishOnTouchOutside(false);

        createUiChangeListener();

    }//end of JobInfoActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of TableRowEditorActivity onDestroy");

        super.onDestroy();

    }//end of JobInfoActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of TableRowEditorActivity onResume");

    }//end of JobInfoActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of TableRowEditorActivity onPause");

    }//end of JobInfoActivity::onPause
    //-----------------------------------------------------------------------------

    @Override
    public void onWindowFocusChanged(boolean pHasFocus) {
        super.onWindowFocusChanged(pHasFocus);

        if(pHasFocus) {
            final View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    //-----------------------------------------------------------------------------
    // JobInfoActivity::createUiChangeListener
    //
    // zzz
    //

    private void createUiChangeListener() {

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int pVisibility) {

                        if ((pVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            //debug hss//
                            Log.d(TAG, "system visibility change");
                            decorView.setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }

                    }

                });

    }//end of JobInfoActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of JobInfoActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    // Puts the pipe number and total length into the intent extras, sets the
    // result to ok, and finishes the activity.
    //

    private void exitActivityByOk() {

        TextView pPN = (TextView)findViewById(R.id.editTextPipeNumber);
        pipeNumber = pPN.getText().toString();
        TextView pTL = (TextView)findViewById(R.id.editTextTotalLength);
        totalLength = pTL.getText().toString();
        CheckBox cBRAB = (CheckBox)findViewById(R.id.checkBoxRenumberAllBelow);
        renumberAll = cBRAB.isChecked();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(PIPE_NUMBER_KEY, pipeNumber);
        resultIntent.putExtra(TOTAL_LENGTH_KEY, totalLength);
        resultIntent.putExtra(RENUMBER_ALL_CHECKBOX_KEY, renumberAll);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }//end of JobInfoActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of JobInfoActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of JobInfoActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of JobInfoActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::setPipeNumberAndTotalLengthValues
    //
    // Sets the pipe number and total length edit text values to pipeNumber and
    // and totalLength.
    //

    private void setPipeNumberAndTotalLengthValues() {

        TextView pPN = (TextView)findViewById(R.id.editTextPipeNumber);
        pPN.setText(pipeNumber);
        TextView pTL = (TextView)findViewById(R.id.editTextTotalLength);
        pTL.setText(totalLength);

    }//end of JobInfoActivity::setPipeNumberAndTotalLengthValues
    //-----------------------------------------------------------------------------

}//end of class JobInfoActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------