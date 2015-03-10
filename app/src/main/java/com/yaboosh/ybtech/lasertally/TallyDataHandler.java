/******************************************************************************
 * Title: TallyDataHandler.java
 * Author: Hunter Schoonover
 * Date: 02/21/15
 *
 * Purpose:
 *
 * This class handles the saving and reading of the tally data to file
 * and displaying the tally data in the measurements table.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyDataHandler
//

import android.util.Log;
import android.widget.TableRow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TallyDataHandler {

    public static final String LOG_TAG = "TallyDataHandler";

    private MeasurementsTableHandler measurementsTableHandler;

    private SharedSettings sharedSettings;
    public void setSharedSettings(SharedSettings pSet) { sharedSettings = pSet; handleSharedSettingsChanged(); }

    private JobInfo jobInfo;
    public void setJobInfo(JobInfo pJobInfo) { jobInfo = pJobInfo; handleJobInfoChanged(); }

    String unitSystem = "";

    private TallyData tallyData;
    private TallyData imperialTallyData;
    private TallyData metricTallyData;

    //-----------------------------------------------------------------------------
    // TallyDataHandler::TallyDataHandler (constructor)
    //

    public TallyDataHandler(SharedSettings pSet, JobInfo pJobInfo, MeasurementsTableHandler pHandler)
    {

        sharedSettings = pSet;
        jobInfo = pJobInfo;
        measurementsTableHandler = pHandler;

    }//end of TallyDataHandler::TallyDataHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::init
    //

    public void init()
    {

        imperialTallyData = new ImperialTallyData(sharedSettings, jobInfo);
        imperialTallyData.init();

        metricTallyData = new MetricTallyData(sharedSettings, jobInfo);
        metricTallyData.init();

        setUnitSystem(sharedSettings.getUnitSystem());

        readDataFromLists();

    }//end of TallyDataHandler::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::addDataEntry
    //
    // Adds the passed in data to the appropriate lists and the measurements table.
    //
    // This version of the function is used when a new measurement has been
    // received from the tally device.
    //

    private void addDataEntry(double pTotal)
    {

        //add a new row to measurements table
        TableRow tR = measurementsTableHandler.addNewRowToTable();

        //store the data -- the pipe number and adjusted values
        //will be calculated in the addData functions
        //any conversions necessary will also be done there
        imperialTallyData.addData(tR, pTotal);
        metricTallyData.addData(tR, pTotal);

        putTallyDataIntoTable();

    }//end of TallyDataHandler::addDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::addDataEntry
    //
    // Adds the passed in data to the appropriate lists and the measurements table.
    //
    // This version of the function is used when reading the data from the lists
    // that were used to store the data read from the tally data files.
    //

    private void addDataEntry(String pPipeNumber, String pImperialAdjustedLength,
                                String pImperialTotalLength, String pMetricAdjustedLength,
                                String pMetricTotalLength)
    {

        //add a new row to measurements table
        TableRow tR = measurementsTableHandler.addNewRowToTable();

        //store the data
        imperialTallyData.addData(tR, pPipeNumber, pImperialAdjustedLength, pImperialTotalLength);
        metricTallyData.addData(tR, pPipeNumber, pMetricAdjustedLength, pMetricTotalLength);

        putTallyDataIntoTable();

    }//end of TallyDataHandler::addDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::changeValuesOfExistingRow
    //
    // Changes the values of the passed in row using the passed in values.
    //
    // If the passed in boolean is true, then all pipe numbers of the rows after
    // the passed in row should be renumbered.
    //
    // NOTE:    If the unit system is set to Imperial, then the passed in
    //              value is assumed to be in Imperial format.
    //          If the unit system is set to Metric, then the passed in
    //              value is assumed to be in Metric format.
    //

    public void changeValuesOfExistingRow(TableRow pRow, String pPipeNum, String pTotalLength,
                                          boolean pRenumberAllAfterRow)
    {

        int pipeNumber = Integer.parseInt(pPipeNum);
        double newTotal = Double.parseDouble(pTotalLength);

        imperialTallyData.addData(pRow, pipeNumber, newTotal, pRenumberAllAfterRow);
        metricTallyData.addData(pRow, pipeNumber, newTotal, pRenumberAllAfterRow);

        putTallyDataIntoTable();
        setAndCheckTotals();

    }//end of TallyDataHandler::changeValuesOfExistingRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getPipeNumberOfRow
    //
    // Get and return the pipe number associated with the passed in TableRow.
    //

    public String getPipeNumberOfRow(TableRow pR)
    {

        return tallyData.getPipeNumberOfRow(pR);

    }//end of TallyDataHandler::getPipeNumberOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getTotalLengthValueOfRow
    //
    // Get and return the total length associated with the passed in TableRow.
    //

    public String getTotalLengthValueOfRow(TableRow pR)
    {

        return tallyData.getTotalLengthValueOfRow(pR);

    }//end of TallyDataHandler::getTotalLengthValueOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleJobInfoChanged
    //
    // Uses the passed in distance value to calculate the values needed to add a
    // new row to the measurements table.
    //

    private void handleJobInfoChanged()
    {

        imperialTallyData.setJobInfo(jobInfo);
        metricTallyData.setJobInfo(jobInfo);
        putTallyDataIntoTable();
        setAndCheckTotals();

    }//end of TallyDataHandler::handleJobInfoChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleNewDistanceValue
    //
    // Uses the passed in value to add a data entry.
    //
    // All new distances received from the tally device are Imperial.
    //

    public void handleNewDistanceValue(Double pValue)
    {

        if (tallyData.isValidLength(pValue)) { return; }

        addDataEntry(pValue);

    }//end of TallyDataHandler::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleSharedSettingsChanged
    //

    private void handleSharedSettingsChanged()
    {

        imperialTallyData.setSharedSettings(sharedSettings);
        metricTallyData.setSharedSettings(sharedSettings);
        setUnitSystem(sharedSettings.getUnitSystem());

    }//end of TallyDataHandler::handleSharedSettingsChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::putTallyDataIntoTable
    //
    // Puts the tally data into the measurements table.
    //

    private void putTallyDataIntoTable()
    {

        measurementsTableHandler.setValues(tallyData.getAdjustedValues(),
                                                tallyData.getPipeNumbers(),
                                                tallyData.getTotalLengthValues());

        setAndCheckTotals();

    }//end of TallyDataHandler::putTallyDataIntoTable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::readDataFromLists
    //
    // Reads the tally data from the lists that were used to store the data read
    // from file and stores the data appropriately.
    //

    private void readDataFromLists()
    {

        ArrayList<String> pipeNumbers = tallyData.getPipeNumbersFromFile();
        ArrayList<String> imperialAdjustedValues = imperialTallyData.getAdjustedValuesFromFile();
        ArrayList<String> imperialTotalLengthValues = imperialTallyData.getTotalLengthValuesFromFile();
        ArrayList<String> metricAdjustedValues = metricTallyData.getAdjustedValuesFromFile();
        ArrayList<String> metricTotalLengthValues = metricTallyData.getTotalLengthValuesFromFile();


        for (int i=0; i<pipeNumbers.size(); i++) {

            addDataEntry(pipeNumbers.get(i), imperialAdjustedValues.get(i),
                            imperialTotalLengthValues.get(i), metricAdjustedValues.get(i),
                            metricTotalLengthValues.get(i));

        }

    }//end of TallyDataHandler::readDataFromLists
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::removeLastDataEntry
    //
    // Removes the most recently entered Adjusted, Total Length, and Pipe Number
    // values from their lists. Also removes the last row in the measurements table.
    //

    public void removeLastDataEntry()
    {

        TableRow lastAddedRow = measurementsTableHandler.getLastAddedRow();
        imperialTallyData.removeData(lastAddedRow);
        metricTallyData.removeData(lastAddedRow);

        measurementsTableHandler.removeLastAddedRow();

        setAndCheckTotals();

    }//end of TallyDataHandler::removeLastDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::setAndCheckTotals
    //
    // Calculates the Adjustment and Total Length totals, checks to see if the tally
    // goal has been reached, and then passes the information on to the
    // MeasurementsTableHandler.
    //

    private void setAndCheckTotals()
    {

        measurementsTableHandler.setTotals(tallyData.getAdjustedValuesTotal(),
                                                tallyData.getTotalLengthValuesTotal(),
                                                tallyData.checkTallyGoal());


    }//end of TallyDataHandler::setAndCheckTotals
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::setUnitSystem
    //
    // Sets the unit system to passed in value.
    //

    private void setUnitSystem(String pSystem)
    {

        //No need to do anything else if the unit system
        //hasn't changed
        if (unitSystem.equals(pSystem)) { return; }

        unitSystem = pSystem;

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) { tallyData = imperialTallyData; }
        else if (unitSystem.equals(Keys.METRIC_MODE)) { tallyData = metricTallyData; }

        putTallyDataIntoTable();

    }//end of TallyDataHandler::setUnitSystem
    //-----------------------------------------------------------------------------

}//end of class TallyDataHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------