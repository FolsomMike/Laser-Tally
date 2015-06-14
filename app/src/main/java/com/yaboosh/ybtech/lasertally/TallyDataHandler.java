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

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class TallyDataHandler {

    public static final String LOG_TAG = "TallyDataHandler";

    private JobDisplayActivity parentActivity;

    private SharedSettings sharedSettings;
    public void setSharedSettings(SharedSettings pSet) { sharedSettings = pSet; handleSharedSettingsChanged(); }

    private JobsHandler jobsHandler;
    public void setJobInfo(JobsHandler pJobsHandler) { jobsHandler = pJobsHandler; handleJobInfoChanged(); }

    String unitSystem = "";

    private TallyData tallyData;
    private TallyData imperialTallyData;
    private TallyData metricTallyData;

    ListViewAdapter adapter;

    View selectedView = null;

    //-----------------------------------------------------------------------------
    // TallyDataHandler::TallyDataHandler (constructor)
    //

    public TallyDataHandler(JobDisplayActivity pParentActivity, SharedSettings pSet,
                                JobsHandler pJobsHandler)
    {

        parentActivity = pParentActivity;
        sharedSettings = pSet;
        jobsHandler = pJobsHandler;

    }//end of TallyDataHandler::TallyDataHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::init
    //

    public void init()
    {

        imperialTallyData = new ImperialTallyData(sharedSettings, jobsHandler);
        imperialTallyData.init();

        metricTallyData = new MetricTallyData(sharedSettings, jobsHandler);
        metricTallyData.init();

        setUnitSystem(sharedSettings.getUnitSystem());

        adapter = new ListViewAdapter(parentActivity, tallyData.getPipeNumbers(),
                                        tallyData.getTotalLengthValues(),
                                        tallyData.getAdjustedValues());

        final ListView l = (ListView)parentActivity.findViewById(R.id.tallyDataListView);
        l.setAdapter(adapter);

        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelection(position, view, true);

            }
        });

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

        //store the data -- the pipe number and adjusted values
        //will be calculated in the addData functions
        //any conversions necessary will also be done there
        imperialTallyData.addData(pTotal);
        metricTallyData.addData(pTotal);

        displayTallyData();

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

        //store the data
        imperialTallyData.addData(pPipeNumber, pImperialAdjustedLength, pImperialTotalLength);
        metricTallyData.addData(pPipeNumber, pMetricAdjustedLength, pMetricTotalLength);

        displayTallyData();

    }//end of TallyDataHandler::addDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::changeValuesAtIndex
    //
    // Changes the values of the data at the passed in index.
    //
    // If the passed in boolean is true, then all pipe numbers of the rows after
    // the passed in row should be renumbered.
    //
    // NOTE:    If the unit system is set to Imperial, then the passed in
    //              value is assumed to be in Imperial format.
    //          If the unit system is set to Metric, then the passed in
    //              value is assumed to be in Metric format.
    //

    public void changeValuesAtIndex(int pIndex, String pPipeNum, String pTotalLength,
                                          boolean pRenumberAllAfterRow)
    {

        int pipeNumber = Integer.parseInt(pPipeNum);
        double newTotal = Double.parseDouble(pTotalLength);

        imperialTallyData.addData(pIndex, pipeNumber, newTotal, pRenumberAllAfterRow);
        metricTallyData.addData(pIndex, pipeNumber, newTotal, pRenumberAllAfterRow);

        displayTallyData();

    }//end of TallyDataHandler::changeValuesAtIndex
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::displayTallyData
    //
    // Displays the tally data to the user.
    //

    private void displayTallyData()
    {

        adapter.notifyDataSetChanged();

        //parentActivity.scrollMeasurementsTable();
        //DEBUG HSS//parentActivity.putTableRowsIntoFocusArray();

        //WIP HSS// -- SHOULD BE IN ITS OWN FUNCTION
        //scroll to bottom of listview
        final ListView l = (ListView)parentActivity.findViewById(R.id.tallyDataListView);
        l.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                l.smoothScrollToPosition(l.getCount() - 1);
            }
        });

        setAndCheckTotals();

    }//end of TallyDataHandler::displayTallyData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getPipeNumberAtIndex
    //
    // Get and return the pipe number at the passed in index.
    //

    public String getPipeNumberAtIndex(int pIndex)
    {

        return tallyData.getPipeNumber(pIndex);

    }//end of TallyDataHandler::getPipeNumberAtIndex
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getTotalLengthAtIndex
    //
    // Get and return the total length at the passed in index.
    //

    public String getTotalLengthAtIndex(int pIndex)
    {

        return tallyData.getTotalLengthValue(pIndex);

    }//end of TallyDataHandler::getTotalLengthAtIndex
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleJobInfoChanged
    //
    // Uses the passed in distance value to calculate the values needed to add a
    // new row to the measurements table.
    //

    private void handleJobInfoChanged()
    {

        imperialTallyData.setJobInfo(jobsHandler);
        metricTallyData.setJobInfo(jobsHandler);
        displayTallyData();

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

        double temp = pValue + imperialTallyData.getCalibrationValue();

        //return if the value is not within range
        if (!imperialTallyData.isValidLength(temp)) {
            //DEBUG HSS//Tools.playBadSound(parentActivity);
            return;
        }

        //DEBUG HSS//Tools.playGoodSound(parentActivity);

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
        displayTallyData();

    }//end of TallyDataHandler::handleSharedSettingsChanged
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
    // values from their lists.
    //

    public void removeLastDataEntry()
    {

        imperialTallyData.removeLastDataEntry();
        metricTallyData.removeLastDataEntry();

        displayTallyData();

    }//end of TallyDataHandler::removeLastDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::setAndCheckTotals
    //
    // Sets the columns for the totals of the Adjusted and Total Length values.
    //
    // The background color of the totals table is set to green if the tally goal
    // was reached; set to its normal color if not.
    //

    private void setAndCheckTotals()
    {

        ((TextView)parentActivity.findViewById(R.id.totalOfAdjustedColumnTextView))
                                                    .setText(tallyData.getAdjustedValuesTotal());

        ((TextView)parentActivity.findViewById(R.id.totalOfTotalLengthColumnTextView))
                                                    .setText(tallyData.getTotalLengthValuesTotal());

        View table = parentActivity.findViewById(R.id.totalsTable);
        if (tallyData.checkTallyGoal()) { table.setBackgroundColor(Color.parseColor("#33CC33")); }
        else {  table.setBackgroundColor(Color.parseColor("#000000")); }

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

    }//end of TallyDataHandler::setUnitSystem
    //-----------------------------------------------------------------------------

}//end of class TallyDataHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------