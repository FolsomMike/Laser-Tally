/******************************************************************************
 * Title: JobsHandler.java
 * Author: Hunter Schoonover
 * Date: 06/10/15
 *
 * Purpose:
 *
 * This class handles everything to do with jobs, including storing information
 * about the current job, creating new jobs, etc.
 *
 * Implements Parcelable so that an instance of this class can be passed from
 * one activity to another using intent extras.
 *
 * For variables that need to remain intact when the class is passed between classes:
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
//-----------------------------------------------------------------------------
// class JobsHandler
//

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class JobsHandler implements Parcelable {

    public static final String LOG_TAG = "JobsHandler";

    String fileFormat = "UTF-8";
    String nL = System.lineSeparator();

    public static Parcelable.Creator CREATOR;

    private SharedSettings sharedSettings;

    private String oldJobDirectoryPath = "";
    private String oldJobName = "";

    //VALUES FOR CURRENT JOB
    private String currentJobDirectoryPath = "";
    public String getCurrentJobDirectoryPath() { return currentJobDirectoryPath; }
    public void setCurrentJobDirectoryPath(String pPath) { currentJobDirectoryPath = pPath; }

    private String currentJobJobInfoPath = "";
    public String getCurrentJobJobInfoPath() { return currentJobJobInfoPath; }
    public void setCurrentJobJobInfoPath(String pPath) { currentJobJobInfoPath = pPath; }

    private String companyName = "";
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String pCompanyName) { companyName = pCompanyName; }

    private String date = "";
    public String getDate() { return date; }
    public void setDate(String pDate) { date = pDate; }

    private String diameter = "";
    public String getDiameter() { return diameter; }
    public void setDiameter(String pDiameter) { diameter = pDiameter; }

    private String facility = "";
    public String getFacility() { return facility; }
    public void setFacility(String pFacility) { facility = pFacility; }

    private String grade = "";
    public String getGrade() { return grade; }
    public void setGrade(String pGrade) { grade = pGrade; }

    private String imperialAdjustment = "";
    public String getImperialAdjustment() { return imperialAdjustment; }
    public void setImperialAdjustment(String pAdjustment) { imperialAdjustment = pAdjustment; }

    private String imperialTallyGoal = "";
    public String getImperialTallyGoal() { return imperialTallyGoal; }
    public void setImperialTallyGoal(String pTallyGoal) { imperialTallyGoal = pTallyGoal; }

    private String jobName = "";
    public String getJobName() { return jobName; }
    public void setJobName(String pName) { jobName = pName; }

    private String metricAdjustment = "";
    public String getMetricAdjustment() { return metricAdjustment; }
    public void setMetricAdjustment(String pAdjustment) { metricAdjustment = pAdjustment; }

    private String metricTallyGoal = "";
    public String getMetricTallyGoal() { return metricTallyGoal; }
    public void setMetricTallyGoal(String pTallyGoal) { metricTallyGoal = pTallyGoal; }

    private String rack = "";
    public String getRack() { return rack; }
    public void setRack(String pRack) { rack = pRack; }

    private String range = "";
    public String getRange() { return range; }
    public void setRange(String pRange) { range = pRange; }

    private String rig = "";
    public String getRig() { return rig; }
    public void setRig(String pRig) { rig = pRig; }

    private String wall = "";
    public String getWall() { return wall; }
    public void setWall(String pWall) { wall = pWall; }
    //End of VALUES FOR CURRENT JOB

    //-----------------------------------------------------------------------------
    // JobsHandler::JobsHandler (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public JobsHandler(SharedSettings pSharedSettings)
    {

        sharedSettings = pSharedSettings;

    }//end of JobsHandler::JobsHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::JobsHandler (constructor)
    //
    // Constructor to be used when creating the object from a parcel.
    //

    public JobsHandler(Parcel pIn)
    {

        readFromParcel(pIn);

    }//end of JobsHandler::JobsHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::init
    //

    public void init()
    {

        initializeCreatorVariable();

    }//end of JobsHandler::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::describeContents
    //
    // Required to override because of Parcelable
    //

    @Override
    public int describeContents()
    {

        return 0;

    }// end of JobsHandler::describeContents
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::writeToParcel
    //
    // Writes each variable into the parcel.
    //
    // Context is not written to the parcel because its value will be set after
    // the SharedSettings object is given to another activity, so preserving its
    // value is not important.
    //

    @Override
    public void writeToParcel(Parcel pParcel, int pFlags)
    {

        //!!STORE VARIABLES IN PARCEL HERE!!
        pParcel.writeString(companyName);
        pParcel.writeString(currentJobDirectoryPath);
        pParcel.writeString(currentJobJobInfoPath);
        pParcel.writeString(date);
        pParcel.writeString(diameter);
        pParcel.writeString(facility);
        pParcel.writeString(grade);
        pParcel.writeString(imperialAdjustment);
        pParcel.writeString(imperialTallyGoal);
        pParcel.writeString(jobName);
        pParcel.writeString(metricAdjustment);
        pParcel.writeString(metricTallyGoal);
        pParcel.writeString(oldJobDirectoryPath);
        pParcel.writeString(oldJobName);
        pParcel.writeString(rack);
        pParcel.writeString(range);
        pParcel.writeString(rig);
        pParcel.writeString(wall);

    }// end of JobsHandler::writeToParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::checkIfJobNameAlreadyExists
    //
    // Searches through the jobs in the jobs directory to see if a job already
    // has the passed in name.
    //
    // Returns true if name already exists. False if it doesn't.
    //
    //

    public Boolean checkIfJobNameAlreadyExists(String pJobName) {

        Boolean exists = false;

        try {

            // Retrieve the jobs directory
            File jobsDir = new File (sharedSettings.getJobsFolderPath());

            // All of the names of the directories in the
            // jobs directory are job names. If one of the
            // directory names is equal to the passed
            // in job, then the job already exists
            File[] dirs = jobsDir.listFiles();
            for (File f : dirs) {
                if (f.isDirectory() && pJobName.equals(f.getName())) { exists = true; }
            }

        } catch (Exception e) {}

        return exists;

    }//end of JobsHandler::checkIfJobNameAlreadyExists
    //----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::createQuickJob
    //
    // Creates and stores a job with a programmatically determined job name and
    // the date the job was created. Everything else is left blank.
    //

    public void createQuickJob()
    {

        //WIP HSS// -- need to get date

        String name = "";
        int i = 1;
        do { name = "job" + i++; }  while (checkIfJobNameAlreadyExists(name));

        jobName = name;

        setFilePaths(jobName);

        saveJobInfoToFile(true);

    }//end of JobsHandler::createQuickJob
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::deleteCurrentJob
    //
    // Deletes the current job.
    //

    public void deleteCurrentJob() {

        try { Tools.deleteDirectory(new File(currentJobDirectoryPath)); } catch (Exception e) {}

    }//end of JobsHandler::deleteCurrentJob
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::generateFileText
    //
    // Generates the file text used for saving the job info to file.
    //
    // Comment lines are began with "#"
    //

    private String generateFileText()
    {

        String fileText = ("Company Name=" + companyName)
                + nL
                + ("Date=" + date)
                + nL
                + ("Diameter=" + diameter)
                + nL
                + ("Facility=" + facility)
                + nL
                + ("Grade=" + grade)
                + nL
                + ("Imperial Adjustment=" + imperialAdjustment)
                + nL
                + ("Imperial Tally Goal=" + imperialTallyGoal)
                + nL
                + ("Job Name=" + jobName)
                + nL
                + ("Metric Adjustment=" + metricAdjustment)
                + nL
                + ("Metric Tally Goal=" + metricTallyGoal)
                + nL
                + ("Rack=" + rack)
                + nL
                + ("Range=" + range)
                + nL
                + ("Rig=" + rig)
                + nL
                + ("Wall=" + wall);

        return fileText;

    }//end of JobsHandler::generateFileText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::getAllJobs
    //
    // Returns all of the jobsHandler stored on the Android  device in an ArrayList.
    //

    public ArrayList<String> getAllJobs()
    {

        ArrayList<String> jobs = new ArrayList<String>();

        try {

            // Retrieve the jobsHandler directory
            File jobsDir = new File (sharedSettings.getJobsFolderPath());

            // All of the directories in the jobsHandler directory
            // are jobsHandler, so they are stored as such
            File[] files = jobsDir.listFiles();
            for (File f : files) { if (f.isDirectory()) { jobs.add(f.getName()); } }

        } catch (Exception e) { Log.e(LOG_TAG, e.toString()); }

        return jobs;


    }//end of JobsHandler::getAllJobs
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::initializeCreatorVariable
    //
    // Initializes the CREATOR variable, overriding class functions as necessary.
    //
    // "The Parcelable.Creator interface must be implemented and provided as a
    // public CREATOR field that generates instances of your Parcelable class
    // from a Parcel." This function does just that.
    //

    private void initializeCreatorVariable()
    {

        CREATOR = new Parcelable.Creator() {

            //Create a new instance of the SharedSettings class,
            //instantiating it from the given Parcel
            @Override
            public JobsHandler createFromParcel(Parcel pParcel) {
                return new JobsHandler(pParcel);
            }

            //Create a new array of the SharedSettings class
            @Override
            public JobsHandler[] newArray(int pSize) {
                return new JobsHandler[pSize];
            }
        };

    }//end of JobsHandler::initializeCreatorVariable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::loadJobFromFile
    //
    // Uses the passed in job name to get and store job info from file.
    //

    public void loadJobFromFile(String pJobName)
    {

        storeOldJobDirectoryAndName();

        setFilePaths(pJobName);

        loadJobInfoFromFile(currentJobJobInfoPath);

    }//end of JobsHandler::loadJobFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::loadJobInfoFromFile
    //
    // Gets and stores the job info by retrieving the values from the jobInfo.txt
    // file located at the passed in file path.
    //

    private void loadJobInfoFromFile(String pPath)
    {

        if (pPath == null || pPath.isEmpty()) { return; }

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader in = null;
        ArrayList<String> fileLines = new ArrayList<String>();

        try {

            File file = new File(pPath);

            fileInputStream = new FileInputStream(file);

            inputStreamReader = new InputStreamReader(fileInputStream, fileFormat);

            in = new BufferedReader(inputStreamReader);

            //read until end of file reached
            String line;
            while ((line = in.readLine()) != null){ fileLines.add(line); }

        }
        catch (FileNotFoundException e){ Log.e(LOG_TAG, "Line 284 :: " + e.getMessage()); }
        catch(IOException e){ Log.e(LOG_TAG, "Line 285 :: " + e.getMessage()); }
        finally{
            try { if (in != null) { in.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 288 :: " + e.getMessage()); }

            try { if (inputStreamReader != null) { inputStreamReader.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 291 :: " + e.getMessage()); }

            try { if (fileInputStream != null) { fileInputStream.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 294 :: " + e.getMessage()); }
        }

        // If there were no lines in the file,
        // this function is exited.
        if (fileLines.isEmpty()) { return; }

        companyName = Tools.getValueFromList("Company Name", fileLines);
        date = Tools.getValueFromList("Date", fileLines);
        diameter = Tools.getValueFromList("Diameter", fileLines);
        facility = Tools.getValueFromList("Facility", fileLines);
        grade = Tools.getValueFromList("Grade", fileLines);
        imperialAdjustment = Tools.getValueFromList("Imperial Adjustment", fileLines);
        imperialTallyGoal = Tools.getValueFromList("Imperial Tally Goal", fileLines);
        jobName = Tools.getValueFromList("Job Name", fileLines);
        metricAdjustment = Tools.getValueFromList("Metric Adjustment", fileLines);
        metricTallyGoal = Tools.getValueFromList("Metric Tally Goal", fileLines);
        rack = Tools.getValueFromList("Rack", fileLines);
        range = Tools.getValueFromList("Range", fileLines);
        rig = Tools.getValueFromList("Rig", fileLines);
        wall = Tools.getValueFromList("Wall", fileLines);

    }//end of JobsHandler::loadJobInfoFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::readFromParcel
    //
    // Reads and stores the variables from the passed in parcel in the same order
    // that the variables were written to the parcel.
    //
    // Called from the constructor to create this object from a parcel.
    //

    private void readFromParcel(Parcel pParcel)
    {

        //!!STORE VARIABLES IN PARCEL HERE!!
        companyName = pParcel.readString();
        currentJobDirectoryPath = pParcel.readString();
        currentJobJobInfoPath = pParcel.readString();
        date = pParcel.readString();
        diameter = pParcel.readString();
        facility = pParcel.readString();
        grade = pParcel.readString();
        imperialAdjustment = pParcel.readString();
        imperialTallyGoal = pParcel.readString();
        jobName = pParcel.readString();
        metricAdjustment = pParcel.readString();
        metricTallyGoal = pParcel.readString();
        oldJobDirectoryPath = pParcel.readString();
        oldJobName = pParcel.readString();
        rack = pParcel.readString();
        range = pParcel.readString();
        rig = pParcel.readString();
        wall = pParcel.readString();

    }// end of JobsHandler::readFromParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::saveJobInfoToFile
    //
    // Saves the job info to file.
    //
    // The passed in boolean indicates whether we are creating the job for the
    // first time. Different actions are taken depending on whether or not we are
    // creating the job for the first time, or editing a previously created job.
    //

    private void saveJobInfoToFile(boolean pCreatingJobForFirstTime) {

        //create a buffered writer stream

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            // Create the directory for this job
            // or rename the old directory for the
            // new job name
            File jobDir;
            if (pCreatingJobForFirstTime) {
                jobDir = new File(currentJobDirectoryPath);
                if (!jobDir.exists()) { jobDir.mkdir(); }
            }
            else {
                jobDir = new File(oldJobDirectoryPath);

                //if the job name has changed, the directory
                //needs to be renamed
                if (!jobName.equals(oldJobName)) {
                    jobDir.renameTo(new File(currentJobDirectoryPath));
                }
            }
            // end of Create the directory for this job
            // or rename the old directory for the
            // new job name

            //Put job info into file
            File file = new File(currentJobJobInfoPath);
            if (!file.exists()) { file.createNewFile(); }
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, fileFormat);
            out = new BufferedWriter(outputStreamWriter);

            out.write(generateFileText());

            //Note! You MUST flush to make sure everything is written.
            out.flush();

        }
        catch(IOException e){
            Log.e(LOG_TAG, "Line 426 :: " + e.getMessage());
        }
        finally{
            try{ if (out != null) {out.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 430 :: " + e.getMessage());}

            try{ if (outputStreamWriter != null) {outputStreamWriter.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 433 :: " + e.getMessage());}

            try{ if (fileOutputStream != null) {fileOutputStream.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 436 :: " + e.getMessage()); }
        }

    }// end of JobsHandler::saveJobInfoToFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::saveJob
    //
    // Saves and stores the passed in job info.
    //

    public void saveJob(String pCompanyName, String pDate, String pDiameter, String pFacility,
                        String pGrade, String pImperialAdjustment, String pImperialTallyGoal,
                        String pJobName, String pMetricAdjustment, String pMetricTallyGoal,
                        String pRack, String pRange, String pRig, String pWall,
                        boolean pSavingJobForFirstTime)
    {

        storeOldJobDirectoryAndName();

        companyName = pCompanyName;
        date = pDate;
        diameter = pDiameter;
        facility = pFacility;
        grade = pGrade;
        imperialAdjustment = pImperialAdjustment;
        imperialTallyGoal = pImperialTallyGoal;
        jobName = pJobName;
        metricAdjustment = pMetricAdjustment;
        metricTallyGoal = pMetricTallyGoal;
        rack = pRack;
        range = pRange;
        rig = pRig;
        wall = pWall;

        setFilePaths(jobName);

        saveJobInfoToFile(pSavingJobForFirstTime);

    }//end of JobsHandler::saveJob
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::setFilePaths
    //
    // Sets important file paths involving the location of the job files using
    // the passed in job name.
    //

    private void setFilePaths(String pJobName) {

        currentJobDirectoryPath = sharedSettings.getJobsFolderPath() + File.separator + pJobName;
        currentJobJobInfoPath = currentJobDirectoryPath + File.separator + pJobName + " ~ JobInfo.txt";

    }//end of JobsHandler::setFilePaths
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobsHandler::storeOldJobDirectoryAndName
    //
    // Stores the current job name and directory into "old" variables so that they
    // may be used later.
    //
    // They are currently used in the process of changing the job name.
    //

    public void storeOldJobDirectoryAndName()
    {

        oldJobDirectoryPath = currentJobDirectoryPath;
        oldJobName = jobName;

    }//end of JobsHandler::storeOldJobDirectoryAndName
    //-----------------------------------------------------------------------------

}//end of class JobsHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
