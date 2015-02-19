/******************************************************************************
* Title: MainActivity.java
* Author: Hunter Schoonover
* Date: 7/22/14
*
* Purpose:
*
* This class creates the main activity for the application.
* It is created and used upon app startup.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;

    private ArrayList<String> jobNames = new ArrayList<String>();

    //-----------------------------------------------------------------------------
    // MainActivity::MainActivity (constructor)
    //

    public MainActivity() {

        super();

    }//end of MainActivity::MainActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of MainActivity onCreate");

        setContentView(R.layout.activity_main);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        //If the sharedSettings object is included
        //in the bundle, set the sharedSettings
        //object to the one in the bundle
        //If it's not, initialize the sharedSettings
        //object
        if (getIntent().hasExtra(Keys.SHARED_SETTINGS_KEY)) {
            sharedSettings = getIntent().getExtras().getParcelable(Keys.SHARED_SETTINGS_KEY);
        }
        else {
            sharedSettings = new SharedSettings();
            sharedSettings.init();
        }

    }//end of MainActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of MainActivity onDestroy");

        super.onDestroy();

    }//end of MainActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of MainActivity onResume");

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

    }//end of MainActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of MainActivity onPause");

    }//end of MainActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::createUiChangeListener
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

    }//end of MainActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleCreateNewJobButtonPressed
    //
    // Starts an activity for Job Info.
    // Should be called from the "Create new job." button onClick().
    //

    public void handleCreateNewJobButtonPressed(View pView) {

        Intent intent = new Intent(this, EditJobInfoActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        intent.putExtra(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY,
                                            EditJobInfoActivity.EditJobInfoActivityMode.CREATE_JOB);
        startActivity(intent);

    }//end of MainActivity::handleCreateNewJobButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleOpenAnExistingJobButtonPressed
    //
    // Starts the OpenJobActivity.
    // Should be called from the "Open existing job." button onClick().
    //

    public void handleOpenAnExistingJobButtonPressed(View pView) {

        Intent intent = new Intent(this, OpenJobActivity.class);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
        startActivity(intent);

    }//end of MainActivity::handleOpenAnExistingJobButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
