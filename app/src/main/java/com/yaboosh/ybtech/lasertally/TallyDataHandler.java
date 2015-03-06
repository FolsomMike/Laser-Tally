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

    private static DecimalFormat imperialTallyFormat = new DecimalFormat("#.##");
    private static DecimalFormat metricTallyFormat = new DecimalFormat("#.###");

    private MeasurementsTableHandler measurementsTableHandler;

    private DecimalFormat tallyFormat;

    private SharedSettings sharedSettings;
    public void setSharedSettings(SharedSettings pSet) { sharedSettings = pSet; handleSharedSettingsChanged(); }

    private JobInfo jobInfo;
    public void setJobInfo(JobInfo pJobInfo) { jobInfo = pJobInfo; handleJobInfoChanged(); }

    //File paths
    private String imperialDataFilePath;
    private String metricDataFilePath;
    //end of File paths

    //These lists are used to store the data originally read from file.
    //They are needed because the other lists need a TableRow to insert
    //data, but the Activity cannot be accessed while the file is being
    //accessed.
    private ArrayList<String> pipeNumbersFromFile = new ArrayList<String>();
    private ArrayList<String> imperialAdjustedValuesFromFile = new ArrayList<String>();
    private ArrayList<String> imperialTotalLengthValuesFromFile = new ArrayList<String>();
    private ArrayList<String> metricAdjustedValuesFromFile = new ArrayList<String>();
    private ArrayList<String> metricTotalLengthValuesFromFile = new ArrayList<String>();


    private LinkedHashMap<TableRow, String> pipeNumbers = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getPipeNumbers() { return pipeNumbers; }
    public String getPipeNumberOfRow (TableRow pRow) { return pipeNumbers.get(pRow);}

    private Map<TableRow, String> adjustedValues;
    private String adjustedValuesTotal;
    private Map<TableRow, String> totalLengthValues;
    public String getTotalLengthValueOfRow (TableRow pRow) { return totalLengthValues.get(pRow);}
    private String totalLengthValuesTotal;

    //Imperial
    private LinkedHashMap<TableRow, String> imperialAdjustedValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getImperialAdjustedValues() { return imperialAdjustedValues; }

    private LinkedHashMap<TableRow, String> imperialTotalLengthValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getImperialTotalLengthValues() { return imperialTotalLengthValues; }
    //End of Imperial

    //Metric
    private LinkedHashMap<TableRow, String> metricAdjustedValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getMetricAdjustedValues() { return metricAdjustedValues; }

    private LinkedHashMap<TableRow, String> metricTotalLengthValues = new LinkedHashMap<TableRow, String>();
    public LinkedHashMap getMetricTotalLengthValues() { return metricTotalLengthValues; }
    //End of Metric

    double adjustmentValue = 0;
    double maximumValueAllowed = 0;
    double minimumValueAllowed = 0;
    String unitSystem = "";

    //-----------------------------------------------------------------------------
    // TallyDataHandler::TallyDataHandler (constructor)
    //

    public TallyDataHandler(SharedSettings pSet, JobInfo pJobInfo, MeasurementsTableHandler pHandler)
    {

        sharedSettings = pSet;
        jobInfo = pJobInfo;
        measurementsTableHandler = pHandler;

        adjustmentValue = Double.parseDouble(jobInfo.getMakeupAdjustment());

    }//end of TallyDataHandler::TallyDataHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::init
    //

    public void init()
    {

        setUnitSystem(sharedSettings.getUnitSystem());
        maximumValueAllowed = Double.parseDouble(sharedSettings.getMaximumMeasurementAllowed());
        minimumValueAllowed = Double.parseDouble(sharedSettings.getMinimumMeasurementAllowed());

        imperialDataFilePath = jobInfo.getCurrentJobDirectoryPath() + File.separator
                                                                    + jobInfo.getJobName()
                                                                    + " ~ TallyData ~ Imperial.csv";

        metricDataFilePath = jobInfo.getCurrentJobDirectoryPath() + File.separator
                                                                    + jobInfo.getJobName()
                                                                    + " ~ TallyData ~ Metric.csv";

        loadDataFromFile();
        readDataFromLists();

    }//end of TallyDataHandler::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::addDataEntry
    //
    // Adds the passed in data to the appropriate lists and the measurements table.
    //

    private void addDataEntry(String pPipeNumber, String pImperialTotalLength,
                                String pImperialAdjusted, String pMetricTotalLength,
                                String pMetricAdjusted)
    {

        //Determine whether to add the imperial or metric values
        //to the table
        String tableAdjusted = "";
        String tableTotalLength = "";
        if (unitSystem.equals(Keys.IMPERIAL_MODE)) {
            tableAdjusted = pImperialAdjusted;
            tableTotalLength = pImperialTotalLength;
        }
        else if (unitSystem.equals(Keys.METRIC_MODE)) {
            tableAdjusted = pMetricAdjusted;
            tableTotalLength = pMetricTotalLength;
        }

        //insert the data into the measurements table
        TableRow tR = measurementsTableHandler.addValuesToTable(pPipeNumber, tableTotalLength, tableAdjusted);

        //store the data
        putImperialData(tR, pImperialAdjusted, pImperialTotalLength);
        putMetricData(tR, pMetricAdjusted, pMetricTotalLength);
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

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) {
            imperialAdjustedValues.put(pRow, newAdjusted);
            imperialTotalLengthValues.put(pRow, pTotalLength);
            metricAdjustedValues.put(pRow, Tools.convertToMetric(Double.parseDouble(newAdjusted)));
            metricTotalLengthValues.put(pRow, Tools.convertToMetric(Double.parseDouble(pTotalLength)));
        }
        else if (unitSystem.equals(Keys.METRIC_MODE)) {
            imperialAdjustedValues.put(pRow, Tools.convertToImperial(Double.parseDouble(newAdjusted)));
            imperialTotalLengthValues.put(pRow, Tools.convertToMetric(Double.parseDouble(pTotalLength)));
            metricAdjustedValues.put(pRow, newAdjusted);
            metricTotalLengthValues.put(pRow, pTotalLength);
        }

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
    // Generates the file text used for saving the imperial tally data, using the
    // passed in Maps for the total length and adjusted values.
    //
    // Comment lines are began with "#"
    //

    private String generateFileText(Map<TableRow, String> pAdjustedValues,
                                        Map<TableRow, String> pTotalLengthValues)
    {

        String fileText = "# Pipe Number, Total Length, Adjusted";

        for (Map.Entry<TableRow, String> entry : pipeNumbers.entrySet()) {

            String pipeNumber = pipeNumbers.get(entry.getKey());
            String totalLength = pTotalLengthValues.get(entry.getKey());
            String adjusted = pAdjustedValues.get(entry.getKey());

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
    // TallyDataHandler::handleImperialFileLine
    //
    // Either stores the data contained in the file line to the imperial lists
    // or skips over it if it is a comment.
    //

    private void handleImperialFileLine(String pLine)
    {

        //Skip over this file line if it is a comment
        if (pLine.startsWith("#")) { return; }

        pipeNumbersFromFile.add(getPipeNumberFromFileLine(pLine));
        imperialTotalLengthValuesFromFile.add(getTotalLengthValueFromFileLine(pLine));
        imperialAdjustedValuesFromFile.add(getAdjustedValueFromFileLine(pLine));

    }//end of TallyDataHandler::handleImperialFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleMetricFileLine
    //
    // Either stores the data contained in the file line to the metric lists
    // or skips over it if it is a comment.
    //

    private void handleMetricFileLine(String pLine)
    {

        //Skip over this file line if it is a comment
        if (pLine.startsWith("#")) { return; }

        //The pipe numbers are in the file but are not read
        //because the pipe numbers in the metric file should
        //match the pipe numbers from the imperial file
        metricTotalLengthValuesFromFile.add(getTotalLengthValueFromFileLine(pLine));
        metricAdjustedValuesFromFile.add(getAdjustedValueFromFileLine(pLine));

    }//end of TallyDataHandler::handleImperialFileLine
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
        double adjusted = pValue - adjustmentValue;
        String imperialAdjusted = imperialTallyFormat.format(adjusted);
        String imperialTotalLength = imperialTallyFormat.format(pValue);
        String metricAdjusted = Tools.convertToMetric(adjusted);
        String metricTotalLength = Tools.convertToMetric(pValue);

        //if the unit system is imperial, check to see if the imperial total length
        //is less than or greater than the minimum and maximum values
        double imperialTotalLengthDouble = Double.parseDouble(imperialTotalLength);
        if ((unitSystem.equals(Keys.IMPERIAL_MODE)) &&
                ((imperialTotalLengthDouble > maximumValueAllowed)
                || (imperialTotalLengthDouble < minimumValueAllowed))) {
            return;
        }

        //if the unit system is metric, check to see if the metric total length
        //is less than or greater than the minimum and maximum values
        double metricTotalLengthDouble = Double.parseDouble(metricTotalLength);
        if ((unitSystem.equals(Keys.METRIC_MODE)) &&
                ((metricTotalLengthDouble > maximumValueAllowed)
                        || (metricTotalLengthDouble < minimumValueAllowed))) {
            return;
        }

        addDataEntry(pipeNumber, imperialTotalLength, imperialAdjusted,
                        metricTotalLength, metricAdjusted);

    }//end of TallyDataHandler::handleNewDistanceValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::handleSharedSettingsChanged
    //
    // Sets the unit system variable equal to the unit system contained within
    // SharedSettings and recalculates the values.
    //

    private void handleSharedSettingsChanged()
    {

        setUnitSystem(sharedSettings.getUnitSystem());
        maximumValueAllowed = Double.parseDouble(sharedSettings.getMaximumMeasurementAllowed());
        minimumValueAllowed = Double.parseDouble(sharedSettings.getMinimumMeasurementAllowed());

    }//end of TallyDataHandler::handleSharedSettingsChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::loadDataFromFile
    //
    // Load the tally data from the metric and imperial files.
    //

    private void loadDataFromFile()
    {

        loadImperialDataFromFile();
        loadMetricDataFromFile();

    }//end of TallyDataHandler::loadDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::loadImperialDataFromFile
    //
    // Load the imperial tally data from the imperial file.
    //

    private void loadImperialDataFromFile()
    {

        FileReader fileReader = null;
        BufferedReader bufferedReader;

        try {

            fileReader = new FileReader(imperialDataFilePath);
            bufferedReader = new BufferedReader(fileReader);

            //Read all the lines from the file
            String s;
            while ((s = bufferedReader.readLine()) != null) { handleImperialFileLine(s); }

        }
        catch(Exception e){}
        finally{
            try { if (fileReader != null) { fileReader.close(); } } catch (Exception e) { }
        }

    }//end of TallyDataHandler::loadImperialDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::loadMetricDataFromFile
    //
    // Load the metric tally data from the metric file.
    //

    private void loadMetricDataFromFile()
    {

        FileReader fileReader = null;
        BufferedReader bufferedReader;

        try {

            fileReader = new FileReader(metricDataFilePath);
            bufferedReader = new BufferedReader(fileReader);

            //Read all the lines from the file
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                handleMetricFileLine(s);
            }

        }
        catch(Exception e){}
        finally{
            try { if (fileReader != null) { fileReader.close(); } } catch (Exception e) { }
        }

    }//end of TallyDataHandler::loadMetricDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::putListsIntoTable
    //
    // Put the passed in lists into the measurements table.
    //

    private void putListsIntoTable(Map<TableRow, String> pAdjustedValues, Map<TableRow,
                                        String> pTotalLengthValues)
    {

        measurementsTableHandler.setAdjustedColumns(pAdjustedValues);
        measurementsTableHandler.setTotalLengthColumns(pTotalLengthValues);

    }//end of TallyDataHandler::putListsIntoTable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::putImperialData
    //
    // Puts the passed in the appropriate maps, using the TableRow as the key.
    //

    private void putImperialData(TableRow pTR, String pAdjusted, String pTotalLength)
    {

        imperialAdjustedValues.put(pTR, pAdjusted);
        imperialTotalLengthValues.put(pTR, pTotalLength);

    }//end of TallyDataHandler::putImperialData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::putMetricData
    //
    // Puts the passed in the appropriate maps, using the TableRow as the key.
    //

    private void putMetricData(TableRow pTR, String pAdjusted, String pTotalLength)
    {

        metricAdjustedValues.put(pTR, pAdjusted);
        metricTotalLengthValues.put(pTR, pTotalLength);

    }//end of TallyDataHandler::putMetricData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::readDataFromLists
    //
    // Reads the tally data from the lists that were used to store the data read
    // from file and put the data into the appropriate Maps.
    //

    private void readDataFromLists()
    {

        for (int i=0; i<pipeNumbersFromFile.size(); i++) {

            addDataEntry(pipeNumbersFromFile.get(i),
                            imperialTotalLengthValuesFromFile.get(i),
                            imperialAdjustedValuesFromFile.get(i),
                            metricTotalLengthValuesFromFile.get(i),
                            metricAdjustedValuesFromFile.get(i));

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

        //Save the imperial data
        saveDataToFile(imperialDataFilePath, imperialAdjustedValues, imperialTotalLengthValues);

        //Save the metric data
        saveDataToFile(metricDataFilePath, metricAdjustedValues, metricTotalLengthValues);

    }//end of TallyDataHandler::saveTallyDataToFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyDataHandler::saveDataToFile
    //
    // Save the passed in tally data to file located the passed in path.
    //

    private void saveDataToFile(String pPath, Map<TableRow, String> pAdjustedValues,
                                            Map<TableRow, String> pTotalLengthValues)
    {


        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            fileOutputStream = new FileOutputStream(pPath);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            out = new BufferedWriter(outputStreamWriter);

            out.write(generateFileText(pAdjustedValues, pTotalLengthValues));

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

    }//end of TallyDataHandler::saveDataToFile
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

    //-----------------------------------------------------------------------------
    // TallyDataHandler::setUnitSystem
    //
    // Sets the unit system to passed in value.
    //

    private void setUnitSystem(String pSystem)
    {

        //No need to do anything else if the unit system hasn't changed or was
        //null
        if (unitSystem.equals(pSystem)) { return; }

        unitSystem = pSystem;

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) {
            adjustedValues = imperialAdjustedValues;
            totalLengthValues = imperialTotalLengthValues;
            tallyFormat = imperialTallyFormat;
        }
        else if (unitSystem.equals(Keys.METRIC_MODE)) {
            adjustedValues = metricAdjustedValues;
            totalLengthValues = metricTotalLengthValues;
            tallyFormat = metricTallyFormat;
        }

        putListsIntoTable(adjustedValues, totalLengthValues);

    }//end of TallyDataHandler::setUnitSystem
    //-----------------------------------------------------------------------------

}//end of class TallyDataHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------