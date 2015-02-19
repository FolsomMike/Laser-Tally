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

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");

    private SharedSettings sharedSettings;

    private JobInfo jobInfo;
    public void setJobInfo(JobInfo pJobInfo) { jobInfo = pJobInfo; handleJobInfoChanged(); }

    //These lists are used to store the data originally read from file.
    //They are needed because the other lists need a TableRow to insert
    //data, but the Activity cannot be accessed while the file is being
    //accessed.
    private ArrayList<String> adjustedValuesFromFile = new ArrayList<String>();
    private ArrayList<String> pipeNumbersFromFile = new ArrayList<String>();
    private ArrayList<String> totalLengthValuesFromFile = new ArrayList<String>();

    //Important data variables
    private LinkedHashMap<TableRow, String> adjustedValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getAdjustedValues() { return adjustedValues; }

    private String adjustedValuesTotal;
    public String getAdjustedValuesTotal() { return adjustedValuesTotal; }

    private LinkedHashMap<TableRow, String> totalLengthValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getTotalLengthValues() { return totalLengthValues; }
    public String getTotalLengthValueOfRow (TableRow pRow) { return totalLengthValues.get(pRow);}

    private String totalLengthValuesTotal;
    public String getTotalLengthValuesTotal() { return totalLengthValuesTotal; }

    private LinkedHashMap<TableRow, String> pipeNumbers = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getPipeNumbers() { return pipeNumbers; }
    public String getPipeNumberOfRow (TableRow pRow) { return pipeNumbers.get(pRow);}
    //End of Important data variables

    double adjustmentValue = 0;

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

        loadDataFromFile();
        readDataFromLists();

    }//end of TallyDataHandler::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::addDataEntry
    //
    // Adds the passed in data to the appropriate lists and the measurements table.
    //

    private void addDataEntry(String pPipeNumber, String pTotalLength, String pAdjusted)
    {

        //insert the data into the measurements table
        TableRow tR = measurementsTableHandler.addValuesToTable(pPipeNumber, pTotalLength, pAdjusted);

        //store the data
        adjustedValues.put(tR, pAdjusted);
        totalLengthValues.put(tR, pTotalLength);
        pipeNumbers.put(tR, pPipeNumber);

        setAndCheckTotals();

        saveTallyDataToFile();

    }//end of TallyDataHandler::addDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::calculateAdjustmentValues
    //
    // Calculates the adjustment values, stores them, and sends them to the
    // MeasurementsTableHandler.
    //

    private void calculateAdjustmentValues() {

        //Clear the adjustment values
        adjustedValues.clear();

        //For each of the total length values, subtract the
        //adjustmentValue and store the result in adjustedValues
        //along with the proper TableRow

        for (Map.Entry<TableRow, String> entry : totalLengthValues.entrySet()) {

            String value = tallyFormat.format((Double.parseDouble(entry.getValue()) - adjustmentValue));
            adjustedValues.put(entry.getKey(), value);

        }

        measurementsTableHandler.setAdjustedColumns(adjustedValues);

    }//end of TallyDataHandler::calculateAdjustmentValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::calculateTotals
    //
    // Calculate the totals of the adjusted and total length values.
    //

    private void calculateTotals() {

        double adjustedTotal = 0;
        double totalLengthTotal = 0;

        //calculate the total of the adjusted values
        for (String value : adjustedValues.values()) {
            adjustedTotal += Double.parseDouble(value);
        }

        adjustedValuesTotal = tallyFormat.format(adjustedTotal);

        //calculate the total of the total length values
        for (String value : totalLengthValues.values()) {
            totalLengthTotal += Double.parseDouble(value);
        }

        totalLengthValuesTotal = tallyFormat.format(totalLengthTotal);

    }//end of TallyDataHandler::calculateTotals
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::changeValuesOfExistingRow
    //
    // Changes the values of the passed in row using the passed in values.
    //
    // If the passed in boolean is true, then all pipe numbers of the rows after
    // the passed in row should be renumbered.
    //

    public void changeValuesOfExistingRow(TableRow pRow, String pPipeNum, String pTotalLength,
                                          boolean pRenumberAllAfterRow)
    {

        String newAdjusted = tallyFormat.format((Double.parseDouble(pTotalLength) - adjustmentValue));

        //Replace existing values in lists
        pipeNumbers.put(pRow, pPipeNum);
        totalLengthValues.put(pRow, pTotalLength);
        adjustedValues.put(pRow, newAdjusted);

        measurementsTableHandler.changeValuesOfExistingRow(pRow, pPipeNum, pTotalLength,
                                                            newAdjusted, pRenumberAllAfterRow);

        setAndCheckTotals();

        saveTallyDataToFile();

    }//end of TallyDataHandler::changeValuesOfExistingRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::checkTallyGoal
    //
    // Check to see if the tally goal has been reached.
    //
    // If the adjustment values total is equal to or greater than the tally goal:
    //      returns true
    //
    // If the adjustment values total is less than the tally goal:
    //      returns false
    //

    private boolean checkTallyGoal() {

        boolean tallyGoalReached = false;
        double totalOfAdjustedValues = Double.parseDouble(adjustedValuesTotal);

        //Retrieve tally goal from jobInfo
        double tallyGoal;
        if (jobInfo.getTallyGoal().equals("") || Double.parseDouble(jobInfo.getTallyGoal()) == 0) {
            tallyGoal = Double.MAX_VALUE;
        }
        else { tallyGoal = Double.parseDouble(jobInfo.getTallyGoal()); }

        //Check to see if the tally goal has been reached
        if (totalOfAdjustedValues >= tallyGoal) {
            tallyGoalReached = true;
        }
        else if (totalOfAdjustedValues < tallyGoal) {
            tallyGoalReached = false;
        }

        return tallyGoalReached;

    }//end of TallyDataHandler::checkTallyGoal
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::determineNextPipeNumber
    //
    // Determines and returns what should be the next pipe number.
    //
    // The pipe number is based on the pipe number LinkedHashMap. If there are no
    // entries in the LinkedHashMap, then the next pipe number is 1. If the
    // LinkedHashMap does have entries, then the pipe number is set to one more
    // than the last entry.
    //

    private String determineNextPipeNumber()
    {

        String pipeNumber = "1";

        //Return the pipe number as 1 if pipeNumbers is empty
        if (pipeNumbers.isEmpty()) { return pipeNumber; }

        //Iterate through the pipe numbers until the last one is reached
        for (String value : pipeNumbers.values()) {
            pipeNumber = value;
        }

        //Return the previous pipe number plus 1
        return Integer.toString(Integer.parseInt(pipeNumber) + 1);

    }//end of TallyDataHandler::determineNextPipeNumber
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::generateFileText
    //
    // Generates the file text used for saving the tally data.
    //
    // Comment lines are began with "#"
    //

    private String generateFileText()
    {

        String fileText = "# Pipe Number, Total Length, Adjusted";

        for (Map.Entry<TableRow, String> entry : pipeNumbers.entrySet()) {

            String pipeNumber = pipeNumbers.get(entry.getKey());
            String totalLength = totalLengthValues.get(entry.getKey());
            String adjusted = adjustedValues.get(entry.getKey());

            String line = "\r\n" + pipeNumber + "," + totalLength + "," + adjusted;

            fileText += line;

        }

        return fileText;

    }//end of TallyDataHandler::generateFileText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getAdjustedValueFromFileLine
    //
    // Strips and returns the adjusted value within the passed in file line.
    //

    private String getAdjustedValueFromFileLine(String pLine)
    {

        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pSecondComma+1);

    }//end of TallyDataHandler::getAdjustedValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getPipeNumberFromFileLine
    //
    // Strips and returns the pipe number within the passed in file line.
    //

    private String getPipeNumberFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");

        return pLine.substring(0, pFirstComma);

    }//end of TallyDataHandler::getPipeNumberFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::getTotalLengthValueFromFileLine
    //
    // Strips and returns the total length value within the passed in file line.
    //

    private String getTotalLengthValueFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");
        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pFirstComma+1, pSecondComma);

    }//end of TallyDataHandler::getTotalLengthValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleFileLine
    //
    // Either stores the data contained in the file line or skips over it if it
    // is a comment.
    //

    private void handleFileLine(String pLine)
    {

        //Skip over this file line if it is a comment
        if (pLine.startsWith("#")) { return; }

        pipeNumbersFromFile.add(getPipeNumberFromFileLine(pLine));
        totalLengthValuesFromFile.add(getTotalLengthValueFromFileLine(pLine));
        adjustedValuesFromFile.add(getAdjustedValueFromFileLine(pLine));

    }//end of TallyDataHandler::handleFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleJobInfoChanged
    //
    // Uses the passed in distance value to calculate the values needed to add a
    // new row to the measurements table.
    //

    private void handleJobInfoChanged()
    {

        setAdjustmentValue(jobInfo.getMakeupAdjustment());
        setAndCheckTotals();
        saveTallyDataToFile();

    }//end of TallyDataHandler::handleJobInfoChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleNewDistanceValue
    //
    // Uses the passed in distance value to calculate the values needed to add a
    // new row to the measurements table.
    //

    public void handleNewDistanceValue(Double pValue)
    {

        String pipeNumber = determineNextPipeNumber();
        String totalLength = tallyFormat.format(pValue);
        String adjusted = tallyFormat.format(pValue - adjustmentValue);

        addDataEntry(pipeNumber, totalLength, adjusted);

    }//end of TallyDataHandler::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::loadDataFromFile
    //
    // Load the tally data from file.
    //

    private void loadDataFromFile()
    {

        FileReader fileReader = null;
        BufferedReader bufferedReader;

        try {

            fileReader = new FileReader(sharedSettings.getJobsFolderPath() + jobInfo.getJobName()
                                            + " ~ TallyData.csv");
            bufferedReader = new BufferedReader(fileReader);

            //Read all the lines from the file
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                handleFileLine(s);
            }

        }
        catch(Exception e){}
        finally{
            try { if (fileReader != null) { fileReader.close(); } } catch (Exception e) { }
        }

    }//end of TallyDataHandler::loadDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::readDataFromLists
    //
    // Reads the tally data from the lists that were used to store the data read
    // from file.
    //

    private void readDataFromLists()
    {

        for (int i=0; i<pipeNumbersFromFile.size(); i++) {

            addDataEntry(pipeNumbersFromFile.get(i), totalLengthValuesFromFile.get(i),
                                                                    adjustedValuesFromFile.get(i));

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
        adjustedValues.remove(lastAddedRow);
        totalLengthValues.remove(lastAddedRow);
        pipeNumbers.remove(lastAddedRow);

        measurementsTableHandler.removeLastAddedRow();

        setAndCheckTotals();

        saveTallyDataToFile();

    }//end of TallyDataHandler::removeLastDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::saveTallyDataToFile
    //
    // Save the tally data to file.
    //

    private void saveTallyDataToFile()
    {


        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            fileOutputStream = new FileOutputStream(sharedSettings.getJobsFolderPath()
                                                    + jobInfo.getJobName()
                                                    + " ~ TallyData.csv");
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            out = new BufferedWriter(outputStreamWriter);

            out.write(generateFileText());

            out.flush();

        }
        catch(IOException e){
            Log.e(LOG_TAG, "Error creating file..");
        }
        finally{
            try{if (out != null) {out.close();}}
            catch(IOException e){Log.e(LOG_TAG, "Error closing BufferedWriter.");}
            try{if (outputStreamWriter != null) {outputStreamWriter.close();}}
            catch(IOException e){Log.e(LOG_TAG, "Error closing OutputStreamWriter.");}
            try{if (fileOutputStream != null) {fileOutputStream.close();}}
            catch(IOException e){Log.e(LOG_TAG, "Error closing FileOutputStream.");}
        }

    }//end of TallyDataHandler::saveTallyDataToFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::setAdjustmentValue
    //
    // Sets the adjustment value to the passed in value and recalculates the values
    // of the adjustment columns, if necessary
    //
    // The adjustment value is set to 0 if the string is empty.
    //

    private void setAdjustmentValue(String pNewAdjustmentValue) {

        double newValue = 0;

        //Check to make sure that the passed in string is not empty, to prevent
        //an error when trying to parse
        if (!pNewAdjustmentValue.equals("")) { newValue = Double.parseDouble(pNewAdjustmentValue); }

        //Quit the function if the new value is equal to the old one
        if (newValue == adjustmentValue) { return; }

        adjustmentValue = newValue;
        calculateAdjustmentValues();

        setAndCheckTotals();

    }//end of TallyDataHandler::setAdjustmentValue
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

        calculateTotals();

        measurementsTableHandler.setTotals(adjustedValuesTotal, totalLengthValuesTotal,
                                                                                checkTallyGoal());


    }//end of TallyDataHandler::setAndCheckTotals
    //-----------------------------------------------------------------------------

}//end of class TallyDataHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------