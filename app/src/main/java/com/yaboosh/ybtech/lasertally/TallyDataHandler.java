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

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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

    //Variables used for the tally data ListView
    private MultiColumnListView listView;
    private int pipeNumberColumnId;
    private int totalLengthColumnId;
    private int adjustedColumnId;
    private int editedRowPos;

    //-----------------------------------------------------------------------------
    // TallyDataHandler::TallyDataHandler (constructor)
    //

    public TallyDataHandler(JobDisplayActivity pParentActivity, SharedSettings pSet,
                                JobsHandler pJobsHandler, MultiColumnListView pListView)
    {

        parentActivity = pParentActivity;
        sharedSettings = pSet;
        jobsHandler = pJobsHandler;
        listView = pListView;

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

        //WIP HSS// -- TEST TO SEE IF THESE CAN BE INITIALIZED OUTSIDE OF THIS FUNCTION
        pipeNumberColumnId = R.id.COLUMN_PIPE_NUMBER;
        totalLengthColumnId = R.id.COLUMN_TOTAL_LENGTH;
        adjustedColumnId = R.id.COLUMN_ADJUSTED;

        //load a list with ids to be used for each column
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(pipeNumberColumnId);
        ids.add(totalLengthColumnId);
        ids.add(adjustedColumnId);
        listView.init(parentActivity, R.layout.layout_list_view_row, 3, ids);

        //assign a click listener to the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pParent, View pView, int pPos, long pId) {
                handleListViewRowClicked(pPos);
            }
        });

        displayTallyData();

        listView.restoreSelection();

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

        listView.post(new Runnable() { @Override public void run() { listView.selectLastRow(); } });

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

        readDataFromLists();

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
    // TallyDataHandler::handleListViewRowClicked
    //
    // Calls a handler function for the ListView, and launches an activity to
    // edit the data associated with the clicked row.
    //

    private void handleListViewRowClicked(int pPos)
    {

        editedRowPos = pPos;

        listView.handleRowClicked(pPos);

        //extract data from the clicked row
        String pipeNum = getPipeNumberAtIndex(pPos);
        String totalLength = getTotalLengthAtIndex(pPos);

        //start an activity to edit the data in the row
        Intent intent = new Intent(parentActivity, TableRowEditorActivity.class);
        intent.putExtra(TableRowEditorActivity.PIPE_NUMBER_KEY, pipeNum);
        intent.putExtra(TableRowEditorActivity.TOTAL_LENGTH_KEY, totalLength);
        parentActivity.startActivityForResult(intent, Keys.ACTIVITY_RESULT_TABLE_ROW_EDITOR);

    }//end of TallyDataHandler::handleListViewRowClicked
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
    // TallyDataHandler:handleTableRowEditorActivityResultOk
    //
    // Sets the pipe number and total length of the last edited row to the passed
    // in values.
    //

    public void handleTableRowEditorActivityResultOk(String pPipeNum, String pTotalLength,
                                                      boolean pRenumberAll)
    {

        changeValuesAtIndex(editedRowPos, pPipeNum, pTotalLength, pRenumberAll);

    }//end of TallyDataHandler::handleTableRowEditorActivityResultOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::readDataFromLists
    //
    // Reads the tally data from the lists and sends it to the list view to be
    // displayed.
    //

    private void readDataFromLists()
    {

        ArrayList<SparseArray<String>> list = new ArrayList<SparseArray<String>>();

        for (int i=0; i<tallyData.getPipeNumbers().size(); i++) {

            SparseArray<String> map = new SparseArray<String>();
            map.put(pipeNumberColumnId, tallyData.getPipeNumber(i));
            map.put(totalLengthColumnId, tallyData.getTotalLengthValue(i));
            map.put(adjustedColumnId, tallyData.getAdjustedValue(i));
            list.add(map);
        }

        listView.setList(list);

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

        listView.post(new Runnable() { @Override public void run() { listView.selectLastRow(); } });

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