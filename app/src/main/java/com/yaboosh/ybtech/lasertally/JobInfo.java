/******************************************************************************
 * Title: JobInfo.java
 * Author: Hunter Schoonover
 * Date: 02/21/15
 *
 * Purpose:
 *
 * This class contains variables that pertain to job info.
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
//-----------------------------------------------------------------------------
// class JobInfo
//

import android.os.Parcel;
import android.os.Parcelable;

public class JobInfo implements Parcelable {

    public static Parcelable.Creator CREATOR;

    String currentJobDirectoryPath;
    public String getCurrentJobDirectoryPath() { return currentJobDirectoryPath; }
    public void setCurrentJobDirectoryPath(String pPath) { currentJobDirectoryPath = pPath; }

    String companyName;
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String pCompanyName) { companyName = pCompanyName; }

    String diameter;
    public String getDiameter() { return diameter; }
    public void setDiameter(String pDiameter) { diameter = pDiameter; }

    String facility;
    public String getFacility() { return facility; }
    public void setFacility(String pFacility) { facility = pFacility; }

    String grade;
    public String getGrade() { return grade; }
    public void setGrade(String pGrade) { grade = pGrade; }

    String imperialAdjustment;
    public String getImperialAdjustment() { return imperialAdjustment; }
    public void setImperialAdjustment(String pAdjustment) { imperialAdjustment = pAdjustment; }

    String jobName;
    public String getJobName() { return jobName; }
    public void setJobName(String pName) { jobName = pName; }

    String metricAdjustment;
    public String getMetricAdjustment() { return metricAdjustment; }
    public void setMetricAdjustment(String pAdjustment) { metricAdjustment = pAdjustment; }

    String rack;
    public String getRack() { return rack; }
    public void setRack(String pRack) { rack = pRack; }

    String range;
    public String getRange() { return range; }
    public void setRange(String pRange) { range = pRange; }

    String rig;
    public String getRig() { return rig; }
    public void setRig(String pRig) { rig = pRig; }

    String tallyGoal;
    public String getTallyGoal() { return tallyGoal; }
    public void setTallyGoal(String pTallyGoal) { tallyGoal = pTallyGoal; }

    String wall;
    public String getWall() { return wall; }
    public void setWall(String pWall) { wall = pWall; }

    //-----------------------------------------------------------------------------
    // JobInfo::JobInfo (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public JobInfo(String pPath, String pCompanyName, String pDiameter, String pFacility,
                       String pGrade, String pImperialAdjustment, String pJobName,
                       String pMetricAdjustment, String pRack, String pRange, String pRig,
                       String pTallyGoal, String pWall)
    {

        currentJobDirectoryPath = pPath;
        companyName = pCompanyName;
        diameter = pDiameter;
        facility = pFacility;
        grade = pGrade;
        imperialAdjustment = pImperialAdjustment;
        jobName = pJobName;
        metricAdjustment = pMetricAdjustment;
        rack = pRack;
        range = pRange;
        rig = pRig;
        tallyGoal = pTallyGoal;
        wall = pWall;

    }//end of JobInfo::JobInfo (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::JobInfo (constructor)
    //
    // Constructor to be used when creating the object from a parcel.
    //

    public JobInfo(Parcel pIn) {

        readFromParcel(pIn);

    }//end of JobInfo::JobInfo (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::init
    //

    public void init() {

        initializeCreatorVariable();

    }//end of JobInfo::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::initializeCreatorVariable
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
            public JobInfo createFromParcel(Parcel pParcel) {
                return new JobInfo(pParcel);
            }

            //Create a new array of the SharedSettings class
            @Override
            public JobInfo[] newArray(int pSize) {
                return new JobInfo[pSize];
            }
        };

    }//end of JobInfo::initializeCreatorVariable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::readFromParcel
    //
    // Reads and stores the variables from the passed in parcel in the same order
    // that the variables were written to the parcel.
    //
    // Called from the constructor to create this object from a parcel.
    //

    private void readFromParcel(Parcel pParcel) {

        //!!STORE VARIABLES IN PARCEL HERE!!
        companyName = pParcel.readString();
        currentJobDirectoryPath = pParcel.readString();
        diameter = pParcel.readString();
        facility = pParcel.readString();
        grade = pParcel.readString();
        imperialAdjustment = pParcel.readString();
        jobName = pParcel.readString();
        metricAdjustment = pParcel.readString();
        rack = pParcel.readString();
        range = pParcel.readString();
        rig = pParcel.readString();
        tallyGoal = pParcel.readString();
        wall = pParcel.readString();

    }// end of JobInfo::readFromParcel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::describeContents
    //
    // Required to override because of Parcelable
    //

    @Override
    public int describeContents() {

        return 0;

    }// end of JobInfo::describeContents
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfo::writeToParcel
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
        pParcel.writeString(companyName);
        pParcel.writeString(currentJobDirectoryPath);
        pParcel.writeString(diameter);
        pParcel.writeString(facility);
        pParcel.writeString(grade);
        pParcel.writeString(imperialAdjustment);
        pParcel.writeString(jobName);
        pParcel.writeString(metricAdjustment);
        pParcel.writeString(rack);
        pParcel.writeString(range);
        pParcel.writeString(rig);
        pParcel.writeString(tallyGoal);
        pParcel.writeString(wall);

    }// end of JobInfo::writeToParcel
    //-----------------------------------------------------------------------------

}//end of class JobInfo
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
