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

    public Context context;

    public String dataFolderPath;

    public String reportsFolderPath = "";

    public String jobsFolderPath = "";

    private static final String LOG_TAG = "SharedSettings";

    //-----------------------------------------------------------------------------
    // SharedSettings::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        setDataStoragePath("Tally Zap");

        setJobsFolderPath();

        setReportsFolderPath();

    }// end of SharedSettings::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setDataStoragePath
    //
    // Sets the path to folder pFolderName in external storage in Android folder
    // DIRECTORY_DOCUMENTS, creating the folder if it does not exist.
    //

    public void setDataStoragePath(String pFolderName) {

        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), pFolderName);

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Data folder not created in Documents folder.");
            }
        }

        dataFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::setDataStoragePath
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setJobsFolderPath
    //
    // Sets the path to jobs folder in the dataPath, creating the folder if it
    // does not exist.
    //

    public void setJobsFolderPath() {

        File file = new File(dataFolderPath, "Tally Jobs");

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Tally Reports folder not created in data folder.");
            }
        }

        jobsFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::setJobsFolderPath
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setReportsFolderPath
    //
    // Sets the path to reports folder in the dataPath, creating the folder if it
    // does not exist.
    //

    public void setReportsFolderPath() {

        File file = new File(dataFolderPath, "Tally Reports");

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Tally Reports folder not created in data folder.");
            }
        }

        reportsFolderPath = file.toString() + File.separator;

    }// end of SharedSettings::setReportsFolderPath
    //-----------------------------------------------------------------------------

}//end of class SharedSettings
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
