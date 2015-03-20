/******************************************************************************
 * Title: MetricTallyData.java
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
// class MetricTallyData
//

import java.io.File;
import java.text.DecimalFormat;

public class MetricTallyData extends TallyData {

    //-----------------------------------------------------------------------------
    // MetricTallyData::MetricTallyData (constructor)
    //

    public MetricTallyData(SharedSettings pSet, JobInfo pJobInfo)
    {

        super(pSet, pJobInfo);

        LOG_TAG = "MetricTallyData";

        decFormat = new DecimalFormat("#.000");

        thisClassUnitSystem = Keys.METRIC_MODE;

        conversionFactor = 0.3048;

    }//end of MetricTallyData::MetricTallyData(constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MetricTallyData::setJobInfoVariables
    //
    // Sets variables to values stored in JobInfo.
    //
    // Should be called every time JobInfo changes.
    //

    void setJobInfoVariables()
    {

        filePath = jobInfo.getCurrentJobDirectoryPath() + File.separator
                                                        + jobInfo.getJobName()
                                                        + " ~ TallyData ~ Metric.csv";

        setAdjustmentValue(jobInfo.getMetricAdjustment());

        setTallyGoal(jobInfo.getMetricTallyGoal());

    }//end of MetricTallyData::setJobInfoVariables
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MetricTallyData::setSharedSettingsVariables
    //
    // Sets variables to values stored in SharedSettings.
    //
    // Should be called every time SharedSettings changes.
    //

    void setSharedSettingsVariables()
    {

        setCalibrationValue(sharedSettings.getMetricCalibrationValue());
        setMaxAllowed(sharedSettings.getMaximumMetricMeasurementAllowed());
        setMinAllowed(sharedSettings.getMinimumMetricMeasurementAllowed());

    }//end of MetricTallyData::setSharedSettingsVariables
    //-----------------------------------------------------------------------------

}//end of class MetricTallyData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------