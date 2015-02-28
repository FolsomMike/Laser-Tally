/******************************************************************************
 * Title: TableRowEditorActivity.java
 * Author: Hunter Schoonover
 * Date: 09/14/14
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to edit the Pipe # and Total Length of the row selected in the
 * MainActivity.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TableRowEditorActivity
//

public class TableRowEditorActivity extends Activity {

    public static final String TAG = "TableRowEditorActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;

    public static final String PIPE_NUMBER_KEY =  "PIPE_NUMBER_KEY";
    public static final String RENUMBER_ALL_CHECKBOX_KEY = "RENUMBER_ALL_CHECKBOX_KEY";
    public static final String TOTAL_LENGTH_KEY = "TOTAL_LENGTH_KEY";

    private String pipeNumber = "";
    private String totalLength = "";

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::TableRowEditorActivity (constructor)
    //

    public TableRowEditorActivity() {

        super();

    }//end of TableRowEditorActivity::TableRowEditorActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of TableRowEditorActivity onCreate");

        setContentView(R.layout.activity_table_row_editor);

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

        //Get the pipe number and total length
        //sent to this activity from its parent
        Bundle bundle = getIntent().getExtras();
        sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
        pipeNumber = bundle.getString(PIPE_NUMBER_KEY);
        totalLength = bundle.getString(TOTAL_LENGTH_KEY);

        setPipeNumberAndTotalLengthValues();

    }//end of TableRowEditorActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of TableRowEditorActivity onDestroy");

        super.onDestroy();

    }//end of TableRowEditorActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of TableRowEditorActivity onResume");

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

    }//end of TableRowEditorActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of TableRowEditorActivity onPause");

    }//end of TableRowEditorActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::createUiChangeListener
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

    }//end of TableRowEditor::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of TableRowEditorActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::exitActivityByOk
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
        boolean renumberAll = cBRAB.isChecked();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(PIPE_NUMBER_KEY, pipeNumber);
        resultIntent.putExtra(TOTAL_LENGTH_KEY, totalLength);
        resultIntent.putExtra(RENUMBER_ALL_CHECKBOX_KEY, renumberAll);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }//end of TableRowEditorActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of TableRowEditorActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of TableRowEditorActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of TableRowEditorActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TableRowEditorActivity::setPipeNumberAndTotalLengthValues
    //
    // Sets the pipe number and total length edit text values to pipeNumber and
    // and totalLength.
    //

    private void setPipeNumberAndTotalLengthValues() {

        TextView pPN = (TextView)findViewById(R.id.editTextPipeNumber);
        pPN.setText(pipeNumber);
        TextView pTL = (TextView)findViewById(R.id.editTextTotalLength);
        pTL.setText(totalLength);

    }//end of TableRowEditorActivity::setPipeNumberAndTotalLengthValues
    //-----------------------------------------------------------------------------

}//end of class TableRowEditorActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------