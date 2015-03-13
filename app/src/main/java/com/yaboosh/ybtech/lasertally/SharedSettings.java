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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class SharedSettings implements Parcelable {

    private static final String LOG_TAG = "SharedSettings";

    public static Parcelable.Creator CREATOR;

    private Context context;
    public Context getContext() { return context; }
    public void setContext(Context pNewContext) { context = pNewContext; }

    private String dataFolderPath;
    public String getDataFolderPath() { return dataFolderPath; }
    public void setDataFolderPath(String pNewPath) { dataFolderPath = pNewPath; }

    private String reportsFolderPath = "";
    public String getReportsFolderPath() { return reportsFolderPath; }
    public void setReportsFolderPath(String pNewPath) { reportsFolderPath = pNewPath; }

    private String jobsFolderPath = "";
    public String getJobsFolderPath() { return jobsFolderPath; }
    public void setJobsFolderPath(String pNewPath) { jobsFolderPath = pNewPath; }

    //default general settings
    private String defaultImperialCalibrationValue = "0"; //hss wip// -- should be changed
    private String defaultMetricCalibrationValue = "0"; //hss wip// -- should be changed
    private String defaultMaximumImperialMeasurementAllowed = "60.00"; //hss wip// -- should be changed
    private String defaultMaximumMetricMeasurementAllowed = "18.283"; //hss wip// -- should be changed
    private String defaultMinimumImperialMeasurementAllowed = "1.00"; //hss wip// -- should be changed
    private String defaultMinimumMetricMeasurementAllowed = "0.3048"; //hss wip// -- should be changed
    private String defaultUnitSystem = Keys.IMPERIAL_MODE;
    //end of default general settings

    //general settings for ini file
    private String imperialCalibrationValue;
    public String getImperialCalibrationValue() { return imperialCalibrationValue; }
    public void setImperialCalibrationValue(String pValue) {
        imperialCalibrationValue = pValue; saveGeneralSettingsToFile();
    }

    private String metricCalibrationValue;
    public String getMetricCalibrationValue() { return metricCalibrationValue; }
    public void setMetricCalibrationValue(String pValue) {
        metricCalibrationValue = pValue; saveGeneralSettingsToFile();
    }

    private String maximumImperialMeasurementAllowed;
    public String getMaximumImperialMeasurementAllowed() {
        return maximumImperialMeasurementAllowed;
    }
    public void setMaximumImperialMeasurementAllowed(String pMax) {
        maximumImperialMeasurementAllowed = pMax; saveGeneralSettingsToFile();
    }

    private String minimumImperialMeasurementAllowed;
    public String getMinimumImperialMeasurementAllowed() {
        return minimumImperialMeasurementAllowed;
    }
    public void setMinimumImperialMeasurementAllowed(String pMin) {
        minimumImperialMeasurementAllowed = pMin; saveGeneralSettingsToFile();
    }

    private String maximumMetricMeasurementAllowed;
    public String getMaximumMetricMeasurementAllowed() {
        return maximumMetricMeasurementAllowed;
    }
    public void setMaximumMetricMeasurementAllowed(String pMax) {
        maximumMetricMeasurementAllowed = pMax; saveGeneralSettingsToFile();
    }

    private String minimumMetricMeasurementAllowed;
    public String getMinimumMetricMeasurementAllowed() {
        return minimumMetricMeasurementAllowed;
    }
    public void setMinimumMetricMeasurementAllowed(String pMin) {
        minimumMetricMeasurementAllowed = pMin; saveGeneralSettingsToFile();
    }

    private String unitSystem;
    public String getUnitSystem() { return unitSystem; }
    public void setUnitSystem(String pSystem) { unitSystem = pSystem; saveGeneralSettingsToFile(); }
    //end of general settings for ini file

    //-----------------------------------------------------------------------------
    // SharedSettings::SharedSettings (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public SharedSettings(Context pContext) {

        context = pContext;

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

        loadAppSettingsFromFile();

    }// end of SharedSettings::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::loadAppSettingsFromFile
    //
    // Loads the app settings from the ini file if it exists. If it doesn't exist,
    // default settings are used.
    //

    private void loadAppSettingsFromFile() {

        //reset settings
        resetSettings();

        ArrayList<String> fileLines = new ArrayList<String>();
        FileInputStream fStream = null;
        Scanner br = null;

        try {

            File file = new File(context.getFilesDir(), "general.ini");

            fStream = new FileInputStream(file);
            br = new Scanner(new InputStreamReader(fStream));
            while (br.hasNext()) { fileLines.add(br.nextLine()); }

        }
        catch (Exception e) {}
        finally {

            try {
                if (br != null) { br.close(); }
                if (fStream != null) { fStream.close(); }
            }
            catch (Exception e) {}

        }

        //if the file failed to load or there were no lines in the
        //file, use default settings and quit this function
        if (fileLines.isEmpty()) { useDefaultSettings(); return; }

        imperialCalibrationValue = Tools.getValueFromList("Imperial Calibration Value", fileLines);
        metricCalibrationValue = Tools.getValueFromList("Metric Calibration Value", fileLines);

        maximumImperialMeasurementAllowed =
                        Tools.getValueFromList("Maximum Imperial Measurement Allowed", fileLines);
        maximumMetricMeasurementAllowed =
                        Tools.getValueFromList("Maximum Metric Measurement Allowed", fileLines);

        minimumImperialMeasurementAllowed =
                        Tools.getValueFromList("Minimum Imperial Measurement Allowed", fileLines);
        minimumMetricMeasurementAllowed =
                        Tools.getValueFromList("Minimum Metric Measurement Allowed", fileLines);

        unitSystem = Tools.getValueFromList("Unit System", fileLines);

        //if any of the values weren't found in the list set them equal to their defaults
        useDefaultSettings();

    }// end of SharedSettings::loadAppSettingsFromFile
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

        //!!GET VARIABLES FROM PARCEL HERE!!
        imperialCalibrationValue = pParcel.readString();
        metricCalibrationValue = pParcel.readString();
        dataFolderPath = pParcel.readString();
        reportsFolderPath = pParcel.readString();
        jobsFolderPath = pParcel.readString();
        maximumImperialMeasurementAllowed = pParcel.readString();
        maximumMetricMeasurementAllowed = pParcel.readString();
        minimumImperialMeasurementAllowed = pParcel.readString();
        minimumMetricMeasurementAllowed = pParcel.readString();
        unitSystem = pParcel.readString();

    }// end of SharedSettings::readFromParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::resetSettings
    //
    // Resets all of the settings that are read from the ini file
    // back to blank values.
    //

    private void resetSettings() {

        imperialCalibrationValue = "";
        metricCalibrationValue = "";
        maximumImperialMeasurementAllowed = "";
        maximumMetricMeasurementAllowed ="";
        minimumImperialMeasurementAllowed = "";
        minimumMetricMeasurementAllowed ="";
        unitSystem = "";

    }// end of SharedSettings::resetSettings
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::saveGeneralSettingsToFile
    //
    // Saves the general settings to file.
    //

    private void saveGeneralSettingsToFile() {

        PrintWriter writer = null;

        try {

            File file = new File(context.getFilesDir(), "general.ini");
            if (!file.exists()) { file.createNewFile(); }

            // Use a PrintWriter to write to the file
            writer = new PrintWriter(file, "UTF-8");

            writer.println("Imperial Calibration Value=" + imperialCalibrationValue);
            writer.println("Metric Calibration Value=" + metricCalibrationValue);
            writer.println("Maximum Imperial Measurement Allowed="
                                                            + maximumImperialMeasurementAllowed);
            writer.println("Maximum Metric Measurement Allowed=" + maximumMetricMeasurementAllowed);
            writer.println("Minimum Imperial Measurement Allowed="
                                                            + minimumImperialMeasurementAllowed);
            writer.println("Minimum Metric Measurement Allowed=" + minimumMetricMeasurementAllowed);
            writer.println("Unit System=" + unitSystem);

        } catch (Exception e) {}
        finally {

            try { if (writer != null) { writer.close(); } } catch (Exception e) {}

        }

    }// end of SharedSettings::saveGeneralSettingsToFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setCalibrationValues
    //
    // Sets the Imperial and Metric calibration values using the passed in value.
    //

    private void setCalibrationValues(String pVal) {

        if (pVal.isEmpty()) {
            imperialCalibrationValue= "";
            metricCalibrationValue = "";
            return;
        }

        if (unitSystem.equals(Keys.IMPERIAL_MODE)
                && !pVal.equals(imperialCalibrationValue))
        {

            //unit system is Imperial
            //passed in values are
            //assumed to be Imperial
            imperialCalibrationValue = pVal;
            metricCalibrationValue = Tools.convertToMetricAndFormat(Double.parseDouble(pVal));

        }

        else if (unitSystem.equals(Keys.METRIC_MODE)
                && !pVal.equals(metricCalibrationValue))
        {

            //unit system is Metric
            //passed in values are
            //assumed to be Metric
            metricCalibrationValue = pVal;
            imperialCalibrationValue = Tools.convertToImperialAndFormat(Double.parseDouble(pVal));

        }

    }// end of SharedSettings::setCalibrationValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setGeneralSettings
    //
    // Sets all of the general settings using the passed in values.
    //

    public void setGeneralSettings(String pSystem, String pMinimum, String pMaximum, String pCal) {

        unitSystem = pSystem;

        setCalibrationValues(pCal);
        setMaximumsAllowed(pMaximum);
        setMinimumsAllowed(pMinimum);

        saveGeneralSettingsToFile();

    }// end of SharedSettings::setGeneralSettings
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setMaximumsAllowed
    //
    // Sets the maximum Imperial and Metric values allowed using the passed in
    // value.
    //

    private void setMaximumsAllowed(String pMax) {

        if (pMax.isEmpty()) {
            maximumImperialMeasurementAllowed = "";
            maximumMetricMeasurementAllowed = "";
            return;
        }

        if (unitSystem.equals(Keys.IMPERIAL_MODE)
                && !pMax.equals(maximumImperialMeasurementAllowed))
        {

            //unit system is Imperial
            //passed in values are
            //assumed to be Imperial
            maximumImperialMeasurementAllowed = pMax;
            maximumMetricMeasurementAllowed =
                                        Tools.convertToMetricAndFormat(Double.parseDouble(pMax));

        }

        else if (unitSystem.equals(Keys.METRIC_MODE)
                    && !pMax.equals(maximumMetricMeasurementAllowed))
        {

            //unit system is Metric
            //passed in values are
            //assumed to be Metric
            maximumMetricMeasurementAllowed = pMax;
                maximumImperialMeasurementAllowed =
                                        Tools.convertToImperialAndFormat(Double.parseDouble(pMax));

        }

    }// end of SharedSettings::setMaximumsAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::setMinimumsAllowed
    //
    // Sets the minimum Imperial and Metric values allowed using the passed in
    // value.
    //

    private void setMinimumsAllowed(String pMin) {

        if (pMin.isEmpty()) {
            minimumImperialMeasurementAllowed = "";
            minimumMetricMeasurementAllowed = "";
            return;
        }

        if (unitSystem.equals(Keys.IMPERIAL_MODE)
                && !pMin.equals(minimumImperialMeasurementAllowed))
        {

            //unit system is Imperial
            //passed in values are
            //assumed to be Imperial
            minimumImperialMeasurementAllowed = pMin;
            minimumMetricMeasurementAllowed =
                    Tools.convertToMetricAndFormat(Double.parseDouble(pMin));

        }

        else if (unitSystem.equals(Keys.METRIC_MODE)
                && !pMin.equals(minimumMetricMeasurementAllowed))
        {

            //unit system is Metric
            //passed in values are
            //assumed to be Metric
            minimumMetricMeasurementAllowed = pMin;
            minimumImperialMeasurementAllowed =
                    Tools.convertToImperialAndFormat(Double.parseDouble(pMin));

        }

    }// end of SharedSettings::setMinimumsAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // SharedSettings::useDefaultSettings
    //
    // Uses defaults for any of the general settings that are blank and saves
    // them to file.
    //

    private void useDefaultSettings() {

        if (imperialCalibrationValue.equals("")) {
            imperialCalibrationValue = defaultImperialCalibrationValue;
        }
        if (metricCalibrationValue.equals("")) {
            metricCalibrationValue = defaultMetricCalibrationValue;
        }
        if (maximumImperialMeasurementAllowed.equals("")) {
            maximumImperialMeasurementAllowed = defaultMaximumImperialMeasurementAllowed;
        }
        if (maximumMetricMeasurementAllowed.equals("")) {
            maximumMetricMeasurementAllowed = defaultMaximumMetricMeasurementAllowed;
        }
        if (minimumImperialMeasurementAllowed.equals("")) {
            minimumImperialMeasurementAllowed = defaultMinimumImperialMeasurementAllowed;
        }
        if (minimumMetricMeasurementAllowed.equals("")) {
            minimumMetricMeasurementAllowed = defaultMinimumMetricMeasurementAllowed;
        }
        if (unitSystem.equals("")) {
            unitSystem = defaultUnitSystem;
        }

        saveGeneralSettingsToFile();

    }// end of SharedSettings::useDefaultSettings
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

        //!!STORE VARIABLES IN PARCEL HERE!!
        pParcel.writeString(imperialCalibrationValue);
        pParcel.writeString(metricCalibrationValue);
        pParcel.writeString(dataFolderPath);
        pParcel.writeString(reportsFolderPath);
        pParcel.writeString(jobsFolderPath);
        pParcel.writeString(maximumImperialMeasurementAllowed);
        pParcel.writeString(maximumMetricMeasurementAllowed);
        pParcel.writeString(minimumImperialMeasurementAllowed);
        pParcel.writeString(minimumMetricMeasurementAllowed);
        pParcel.writeString(unitSystem);

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
