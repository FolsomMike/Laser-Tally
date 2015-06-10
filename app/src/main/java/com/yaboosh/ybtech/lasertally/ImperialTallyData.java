/******************************************************************************
 * Title: ImperialTallyData.java
 * Author: Hunter Schoonover
 * Date: 03/09/15
 *
 * Purpose:
 *
 * This class is used for storing, comparing, and calculating the imperial
 * tally data.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ImperialTallyData
//

import java.io.File;
import java.text.DecimalFormat;

public class ImperialTallyData extends TallyData {

    //-----------------------------------------------------------------------------
    // ImperialTallyData::ImperialTallyData (constructor)
    //

    public ImperialTallyData(SharedSettings pSet, JobsHandler pJobsHandler)
    {

        super(pSet, pJobsHandler);

        LOG_TAG = "ImperialTallyData";

        decFormat = new DecimalFormat("#.00");

        thisClassUnitSystem = Keys.IMPERIAL_MODE;

        conversionFactor = 1;

    }//end of ImperialTallyData::ImperialTallyData(constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // ImperialTallyData::setJobInfoVariables
    //
    // Sets variables to values stored in JobsHandler.
    //
    // Should be called every time JobsHandler changes.
    //

    void setJobInfoVariables()
    {

        filePath = jobsHandler.getCurrentJobDirectoryPath() + File.separator
                                                        + jobsHandler.getJobName()
                                                        + " ~ TallyData ~ Imperial.csv";

        setAdjustmentValue(jobsHandler.getImperialAdjustment());

        setTallyGoal(jobsHandler.getImperialTallyGoal());

    }//end of ImperialTallyData::setJobInfoVariables
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // ImperialTallyData::setSharedSettingsVariables
    //
    // Sets variables to values stored in SharedSettings.
    //
    // Should be called every time SharedSettings changes.
    //

    void setSharedSettingsVariables()
    {

        setCalibrationValue(sharedSettings.getImperialCalibrationValue());
        setMaxAllowed(sharedSettings.getMaximumImperialMeasurementAllowed());
        setMinAllowed(sharedSettings.getMinimumImperialMeasurementAllowed());

    }//end of ImperialTallyData::setSharedSettingsVariables
    //-----------------------------------------------------------------------------

}//end of class ImperialTallyData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------