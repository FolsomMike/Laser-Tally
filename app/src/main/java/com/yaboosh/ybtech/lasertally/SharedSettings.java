/******************************************************************************
 * Title: SharedSettings.java
 * Author: Mike Schoonover
 * Date: 2/17/15
 *
 * Purpose:
 *
 * This class contains variables shared among various objects.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SharedSettings
//

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class SharedSettings {

    private Context context;
    // Getter and setter functions
    public Context getContext() { return context; }
    public void setContext(Context pNewContext) { context = pNewContext; }

    private String dataFolderPath;
    // Getter and setter functions
    public String getDataFolderPath() { return dataFolderPath; }
    public void setDataFolderPath(String pNewPath) { dataFolderPath = pNewPath; }

    private String reportsFolderPath = "";
    // Getter and setter functions
    public String getReportsFolderPath() { return reportsFolderPath; }
    public void setReportsFolderPath(String pNewPath) { reportsFolderPath = pNewPath; }

    private String jobsFolderPath = "";
    // Getter and setter functions
    public String getJobsFolderPath() { return jobsFolderPath; }
    public void setJobsFolderPath(String pNewPath) { jobsFolderPath = pNewPath; }

    private static final String LOG_TAG = "SharedSettings";

    //-----------------------------------------------------------------------------
    // SharedSettings::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init() {

        preparesDataStoragePath("Tally Zap");

        prepareJobsFolderPath();

        prepareReportsFolderPath();

    }// end of SharedSettings::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::preparesDataStoragePath
    //
    // Prepares the path to folder pFolderName in external storage in Android folder
    // DIRECTORY_DOCUMENTS, creating the folder if it does not exist.
    //

    private void preparesDataStoragePath(String pFolderName) {

        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOCUMENTS), pFolderName);

        if (!file.exists()) {
            if (!file.mkdirs()) { Log.e(LOG_TAG, "Data folder not created in Documents folder."); }
        }

        dataFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::preparesDataStoragePath
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::prepareJobsFolderPath
    //
    // Prepares the path to the jobs folder in the dataPath, creating the folder
    // if it does not exist.
    //

    private void prepareJobsFolderPath() {

        File file = new File(dataFolderPath, "Tally Jobs");

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Tally Reports folder not created in data folder.");
            }
        }

        jobsFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::prepareJobsFolderPath
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::prepareReportsFolderPath
    //
    // Prepares the path to the reports folder in the dataFolderPath, creating the
    // folder if it does not exist.
    //

    private void prepareReportsFolderPath() {

        File file = new File(dataFolderPath, "Tally Reports");

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Tally Reports folder not created in data folder.");
            }
        }

        reportsFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::prepareReportsFolderPath
    //-----------------------------------------------------------------------------

}//end of class SharedSettings
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
