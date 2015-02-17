/******************************************************************************
 * Title: SharedSettings.java
 * Author: Mike Schoonover
 * Date: 2/17/15
 *
 * Purpose:
 *
 * This class contains variables shared among various objects.
 *
 * Implements Parcelable so that an instance of this class can be passed from
 * one activity to another using intent extras.
 *
 * For each non-static variable that is added to the class:
 *      search for !!STORE VARIABLES IN PARCEL HERE!! and add the variable
 *          to the parcel, using the others as examples
 *
 *      search for !!GET VARIABLES FROM PARCEL HERE!! and get the variable
 *          from the parcel, using the others as examples
 *
 * IMPORTANT: The orders the variables are stored and retrieved from the
 *              parcel must match!
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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;

public class SharedSettings implements Parcelable {

    private static final String LOG_TAG = "SharedSettings";

    public static Parcelable.Creator CREATOR;

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

    //-----------------------------------------------------------------------------
    // SharedSettings::SharedSettings (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public SharedSettings() {

    }//end of SharedSettings::SharedSettings (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::SharedSettings (constructor)
    //
    // Constructor to be used when creating the object from a parcel.
    //

    public SharedSettings(Parcel pIn) {

        readFromParcel(pIn);

    }//end of SharedSettings::SharedSettings (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init() {

        initializeCreatorVariable();

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

    //-----------------------------------------------------------------------------
    // SharedSettings::readFromParcel
    //
    // Reads and stores the variables from the passed in parcel in the same order
    // that the variables were written to the parcel.
    //
    // Called from the constructor to create this object from a parcel.
    //

    private void readFromParcel(Parcel pParcel) {

        //!!STORE VARIABLES IN PARCEL HERE!!
        dataFolderPath = pParcel.readString();
        reportsFolderPath = pParcel.readString();
        jobsFolderPath = pParcel.readString();

    }// end of SharedSettings::readFromParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::describeContents
    //
    // Required to override because of Parcelable
    //

    @Override
    public int describeContents() {

        return 0;

    }// end of SharedSettings::describeContents
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::writeToParcel
    //
    // Writes each variable into the parcel.
    //
    // Context is not written to the parcel because its value will be set after
    // the SharedSettings object is given to another activity, so preserving its
    // value is not important.
    //

    @Override
    public void writeToParcel(Parcel pParcel, int pFlags) {

        //!!GET VARIABLES FROM PARCEL HERE!!
        pParcel.writeString(dataFolderPath);
        pParcel.writeString(reportsFolderPath);
        pParcel.writeString(jobsFolderPath);

    }// end of SharedSettings::writeToParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::initializeCreatorVariable
    //
    // Initializes the CREATOR variable, overriding class functions as necessary.
    //
    // "The Parcelable.Creator interface must be implemented and provided as a
    // public CREATOR field that generates instances of your Parcelable class
    // from a Parcel." This function does just that.
    //

    private void initializeCreatorVariable() {

        CREATOR = new Parcelable.Creator() {

            //Create a new instance of the SharedSettings class,
            //instantiating it from the given Parcel
            @Override
            public SharedSettings createFromParcel(Parcel pParcel) {
                return new SharedSettings(pParcel);
            }

            //Create a new array of the SharedSettings class
            @Override
            public SharedSettings[] newArray(int pSize) {
                return new SharedSettings[pSize];
            }
        };

    }//end of SharedSettings::initializeCreatorVariable
    //-----------------------------------------------------------------------------

}//end of class SharedSettings
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
