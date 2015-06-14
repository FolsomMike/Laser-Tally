/******************************************************************************
 * Title: TallyData.java
 * Author: Hunter Schoonover
 * Date: 03/09/15
 *
 * Purpose:
 *
 * This class is used for storing, comparing, and calculating the tally data.
 *
 * Intended for use as an abstract parent class for ImperialTallyData
 * and MetricTallyData.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyData
//

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class TallyData {

    public static String LOG_TAG;

    JobsHandler jobsHandler;
    SharedSettings sharedSettings;

    DecimalFormat decFormat;

    String thisClassUnitSystem;

    double conversionFactor;

    String fileFormat = "UTF-8";
    String filePath;
    String nL = System.lineSeparator();

    double calibrationValue;
    public double getCalibrationValue() { return calibrationValue; }
    double maxAllowed;
    double minAllowed;
    double tallyGoal;
    double adjustmentValue = 0;

    //These lists are used to store the data originally read from file.
    //They are needed because the other lists need a TableRow to insert
    //data, but the Activity cannot be accessed while the file is being
    //accessed; a table row cannot be created to store the data in the
    //normally used maps.
    //WIP HSS// -- these may not actually be needed anymore (: YAY!!!!
    private ArrayList<String> pipeNumbersFromFile = new ArrayList<String>();
    public ArrayList<String> getPipeNumbersFromFile() { return pipeNumbersFromFile; }

    private ArrayList<String> adjustedValuesFromFile = new ArrayList<String>();
    public ArrayList<String> getAdjustedValuesFromFile() { return adjustedValuesFromFile; }

    private ArrayList<String> totalLengthValuesFromFile = new ArrayList<String>();
    public ArrayList<String> getTotalLengthValuesFromFile() { return totalLengthValuesFromFile; }


    private String adjustedValuesTotal;
    public String getAdjustedValuesTotal() { return adjustedValuesTotal; }

    private String totalLengthValuesTotal;
    public String getTotalLengthValuesTotal() { return totalLengthValuesTotal; }

    /*//DEBUG HSS//private Map<TableRow, String> adjustedValues = new LinkedHashMap<TableRow, String>();
    private void putAdjustedValue(TableRow pR, String pVal) { adjustedValues.put(pR, pVal); }
    public Map getAdjustedValues() { return adjustedValues; }
    public String getAdjustedValueOfRow(TableRow pR) { return adjustedValues.get(pR); }

    private Map<TableRow, String> pipeNumbers = new LinkedHashMap<TableRow, String>();
    private void putPipeNumber(TableRow pR, String pVal) { pipeNumbers.put(pR, pVal); }
    public Map getPipeNumbers() { return pipeNumbers; }
    public String getPipeNumberOfRow(TableRow pR) { return pipeNumbers.get(pR); }

    private Map<TableRow, String> totalLengthValues = new LinkedHashMap<TableRow, String>();
    private void putTotalLengthValue(TableRow pR, String pVal) { totalLengthValues.put(pR, pVal); }
    public Map getTotalLengthValues() { return totalLengthValues; }
    public String getTotalLengthValueOfRow(TableRow pR) { return totalLengthValues.get(pR); }*///DEBUG HSS//

    private ArrayList<String> adjustedValues = new ArrayList<String>();
    public ArrayList<String> getAdjustedValues() { return adjustedValues; }
    public String getAdjustedValue(int pPos) { return adjustedValues.get(pPos); }

    private ArrayList<String> pipeNumbers = new ArrayList<String>();
    public ArrayList<String> getPipeNumbers() { return pipeNumbers; }
    public String getPipeNumber(int pPos) { return pipeNumbers.get(pPos); }

    private ArrayList<String> totalLengthValues = new ArrayList<String>();
    public ArrayList<String> getTotalLengthValues() { return totalLengthValues; }
    public String getTotalLengthValue(int pPos) { return totalLengthValues.get(pPos); }


    //abstract classes to be overridden by subclasses
    abstract void setJobInfoVariables();
    abstract void setSharedSettingsVariables();

    //-----------------------------------------------------------------------------
    // TallyData::TallyData (constructor)
    //

    public TallyData(SharedSettings pSet, JobsHandler pJobsHandler)
    {

        sharedSettings = pSet;
        jobsHandler = pJobsHandler;

    }//end of TallyData::TallyData(constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::init
    //

    public void init()
    {

        setJobInfoVariables();
        setSharedSettingsVariables();
        loadDataFromFile();
        calculateTotals();

    }//end of TallyData::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::addData
    //
    // Calculates and stores a new data entry.
    //
    // Used when a new measurement is received from the tally device.
    //
    // If necessary, the passed in lengths are converted to a different unit system.
    //
    // The adjusted value and pipe number are not manually set; they are determined
    // programmatically.
    //

    public void addData(double pTotalLength)
    {

        double dTotal = pTotalLength;
        dTotal += calibrationValue;

        //since all values coming from the tally device are
        //already Imperial, the passed in length only needs
        //to be converted if the class system is Metric
        if (thisClassUnitSystem.equals(Keys.METRIC_MODE)) {
            dTotal = Tools.convertToMetric(dTotal);
        }

        String pipe = Integer.toString(determineNextPipeNumber());
        String adjusted = calculateAdjustedValue(dTotal);
        String total = format(dTotal);

        //store the data
        storeData(pipe, adjusted, total);

    }//end of TallyData::addData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::addData
    //
    // Calculates and stores a new data entry.
    //
    // Used when the user manually edits a row.
    //
    // If necessary, the passed in lengths are converted to a different unit system.
    //
    // If the passed in boolean is true, then all of the pipe numbers in the rows
    // after the passed in row are renumbered using the passed in pipe number.
    //
    // The adjusted value is not manually set; it is determined  programmatically.
    //

    public void addData(int pIndex, int pPipeNum, double pTotalLength, boolean pRenumber)
    {

        double dTotal = convert(pTotalLength);

        String pipe = Integer.toString(pPipeNum);
        String adjusted = calculateAdjustedValue(dTotal);
        String total = format(dTotal);

        //check to see if all of the pipe numbers after the passed in
        //row should be renumbered
        if (pRenumber) { renumberAllPipeNumbersAfterIndex(pIndex, pipe); }

        //store the data
        storeData(pipe, adjusted, total);

    }//end of TallyData::addData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::addData
    //
    // Stores the passed in data.
    //
    // Used when adding data that has already been calculated.
    //

    public void addData(String pPipeNum, String pAdjustedLength, String pTotalLength)
    {

        //store the data
        storeData(pPipeNum, pAdjustedLength, pTotalLength);

    }//end of TallyData::addData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::calculateAdjustedValues
    //

    private void calculateAdjustedValues()
    {

        adjustedValues.clear();

        //For each of the total length values, subtract the adjustment value
        //and store the result

        for (String val : totalLengthValues) {

            double total = 0;

            try {  total = Double.parseDouble(val); }
            catch (Exception e) { Log.e(LOG_TAG, "Line 249 :: " + e.getMessage()); }

            adjustedValues.add(format(total - adjustmentValue));

        }

    }//end of TallyData::calculateAdjustedValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::calculateAdjustedValue
    //
    // Calculates and returns the adjusted value by subtracting the adjustment
    // value from the passed in value.
    //

    public String calculateAdjustedValue(double pValue)
    {

        return format(pValue - adjustmentValue);

    }//end of TallyData::calculateAdjustedValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::calculateTotals
    //

    private void calculateTotals()
    {

        //calculate the total of the adjusted values
        double adjustedTotal = 0;
        for (String value : adjustedValues) { adjustedTotal += Double.parseDouble(value); }

        adjustedValuesTotal = format(adjustedTotal);

        //calculate the total of the total length values
        double totalLengthTotal = 0;
        for (String value : totalLengthValues) { totalLengthTotal += Double.parseDouble(value); }

        totalLengthValuesTotal = format(totalLengthTotal);

    }//end of TallyData::calculateTotals
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::checkTallyGoal
    //
    // Check to see if the tally goal has been reached.
    //
    // If the adjustment values total is equal to or greater than the tally goal:
    //      returns true
    //
    // If the adjustment values total is less than the tally goal:
    //      returns false
    //

    public boolean checkTallyGoal() {

        boolean tallyGoalReached = false;

        //Check to see if the tally goal has been reached
        double totalOfAdjustedValues = Double.parseDouble(adjustedValuesTotal);
        if (totalOfAdjustedValues >= tallyGoal) { tallyGoalReached = true; }

        return tallyGoalReached;

    }//end of TallyData::checkTallyGoal
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::convert
    //
    // If the globally set unit system does not match the unit system used in this
    // class, then the passed in values is converted to a different unit system and
    // the result is returned.
    // If the unit systems do match, then no conversion was necessary and the
    // value passed in is just returned.
    //
    // The unit system that the value is converted to depends on what value the
    // conversion factor is set to by children classes.
    //

    private double convert(double pValue)
    {

        double newValue = pValue;

        if (!sharedSettings.getUnitSystem().equals(thisClassUnitSystem)) {
            newValue *= conversionFactor;
        }

        return newValue;

    }//end of TallyData::convert
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

    private int determineNextPipeNumber()
    {

        int pipeNumber = 1;

        //Return the pipe number as 1 if pipeNumbers is empty
        if (pipeNumbers.isEmpty()) { return pipeNumber; }

        //Iterate through the pipe numbers until the last one is reached
        for (String value : pipeNumbers) { pipeNumber = (Integer.parseInt(value)) + 1; }

        return pipeNumber;

    }//end of TallyDataHandler::determineNextPipeNumber
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::generateFileText
    //
    // Generates the file text used for saving the tally data to file.
    //
    // Comment lines are began with "#"
    //

    private String generateFileText()
    {

        String fileText = "# Pipe Number, Total Length, Adjusted";

        for (int i=0; i<pipeNumbers.size()-1; i++) {

            String pipeNumber = pipeNumbers.get(i);
            String totalLength = totalLengthValues.get(i);
            String adjusted = adjustedValues.get(i);

            String line = nL + pipeNumber + "," + totalLength + "," + adjusted;

            fileText += line;

        }

        return fileText;

    }//end of TallyData::generateFileText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::getAdjustedValueFromFileLine
    //
    // Strips and returns the adjusted value within the passed in file line.
    //

    private String getAdjustedValueFromFileLine(String pLine)
    {

        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pSecondComma+1);

    }//end of TallyData::getAdjustedValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::getDistanceLeft
    //
    // Calculates and returns the distance left until the adjusted values equal
    // the tally goal is reached. This is calculated by subtracting the tally goal
    // from the adjusted values total.
    //
    // The distance is returned as Imperial or Metric depending on class set
    // variables.
    //

    public String getDistanceLeft()
    {

        String distanceLeft = "";

        double adjTotal = Double.parseDouble(getAdjustedValuesTotal());

        if (adjTotal == 0 || tallyGoal == Double.MAX_VALUE) {
            return distanceLeft;
        }

        distanceLeft = format(tallyGoal - adjTotal);

        return distanceLeft;

    }//end of TallyData::getDistanceLeft
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::getNumberOfPipesLeft
    //
    // Calculates and returns the number of pipes left until the adjusted values
    // equal the tally goal is reached. This is calculated by subtracting the tally goal
    // from the adjusted values total.
    //
    // The distance is returned as Imperial or Metric depending on class set
    // variables.
    //

    public String getNumberOfPipesLeft()
    {

        String numberOfPipesLeft = "";

        double adjTotal = Double.parseDouble(getAdjustedValuesTotal());
        double numberOfPipes = pipeNumbers.size();
        String distanceLeft = getDistanceLeft();

        if (adjTotal == 0 || numberOfPipes == 0 || distanceLeft.isEmpty()) {
            return numberOfPipesLeft;
        }

        double average = adjTotal / numberOfPipes;

        int pipesLeft = (int)Math.ceil(Double.parseDouble(distanceLeft) / average);
        numberOfPipesLeft = Integer.toString(pipesLeft);

        return numberOfPipesLeft;

    }//end of TallyData::getNumberOfPipesLeft
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::getPipeNumberFromFileLine
    //
    // Strips and returns the pipe number within the passed in file line.
    //

    private String getPipeNumberFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");

        return pLine.substring(0, pFirstComma);

    }//end of TallyData::getPipeNumberFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::getTotalLengthValueFromFileLine
    //
    // Strips and returns the total length value within the passed in file line.
    //

    private String getTotalLengthValueFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");
        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pFirstComma+1, pSecondComma);

    }//end of TallyData::getTotalLengthValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::handleFileLine
    //

    private void handleFileLine(String pLine)
    {

        //Skip over this file line if it is a comment
        if (pLine.startsWith("#")) { return; }

        pipeNumbersFromFile.add(getPipeNumberFromFileLine(pLine));
        totalLengthValuesFromFile.add(getTotalLengthValueFromFileLine(pLine));
        adjustedValuesFromFile.add(getAdjustedValueFromFileLine(pLine));

    }//end of TallyData::handleFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::isGreaterThanMaxValueAllowed
    //
    // Checks to see if the passed in double is greater than the maximum value
    // allowed for measurements.
    //
    // Returns true if it is; false if not.
    //

    public boolean isGreaterThanMaxValueAllowed(double pVal)
    {

        boolean bool = false;

        if (pVal > maxAllowed) {
            bool = true;
        }

        return bool;

    }//end of TallyData::isGreaterThanMaxValueAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::isLessThanMinValueAllowed
    //
    // Checks to see if the passed in double is greater than the minimum value
    // allowed for measurements.
    //
    // Returns true if it is; false if not.
    //

    public boolean isLessThanMinValueAllowed(double pVal)
    {

        boolean bool = false;

        if (pVal < minAllowed) {
            bool = true;
        }

        return bool;

    }//end of TallyData::isLessThanMinValueAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::format
    //
    // Formats and returns the passed in value using a DecimalFormat.
    //

    public String format(double pVal)
    {

        return decFormat.format(pVal);

    }//end of TallyData::format
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::loadDataFromFile
    //

    private void loadDataFromFile()
    {

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader in = null;

        try{

            fileInputStream = new FileInputStream(filePath);

            inputStreamReader = new InputStreamReader(fileInputStream, fileFormat);

            in = new BufferedReader(inputStreamReader);

            //read until end of file reached
            String line;
            while ((line = in.readLine()) != null){ handleFileLine(line); }
        }
        catch (FileNotFoundException e){ Log.e(LOG_TAG, "Line 300 :: " + e.getMessage()); }
        catch(IOException e){ Log.e(LOG_TAG, "Line 301 :: " + e.getMessage()); }
        finally{
            try { if (in != null) { in.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 158 :: " + e.getMessage()); }

            try { if (inputStreamReader != null) { inputStreamReader.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 161 :: " + e.getMessage()); }

            try { if (fileInputStream != null) { fileInputStream.close(); } }
            catch (IOException e) { Log.e(LOG_TAG, "Line 164 :: " + e.getMessage()); }
        }

    }//end of TallyData::loadDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::renumberAllPipeNumbersAfterIndex
    //
    // Renumbers all of the pipe numbers after the passed in index.
    //
    // The first pipe number that will be changed is the one after the passed in
    // index.
    //
    // The first pipe number is set to the passed in pipe number plus one.
    //

    private void renumberAllPipeNumbersAfterIndex(int pIndex, String pPipe)
    {

        int pipeNumber = Integer.parseInt(pPipe);
        boolean pastIndex = false;

        for (int i=0; i<pipeNumbers.size()-1; i++) {
            if (pastIndex) { pipeNumbers.add(Integer.toString(++pipeNumber));  }
            if (i == pIndex) { pastIndex = true; }
        }

    }//end of TallyData::renumberAllPipeNumbersAfterIndex
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::removeData
    //
    // Removes the data associated with the passed in index from all of the lists.
    //

    private void removeData(int pIndex)
    {

        if (pipeNumbers.isEmpty()) { return; }

        pipeNumbers.remove(pIndex);
        adjustedValues.remove(pIndex);
        totalLengthValues.remove(pIndex);

        calculateTotals();

        saveDataToFile();

    }//end of TallyData::removeData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::removeLastDataEntry
    //
    // Removes the most recent data entry from all of the lists.
    //

    public void removeLastDataEntry()
    {

        removeData(pipeNumbers.size()-1);

    }//end of TallyData::removeLastDataEntry
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::saveDataToFile
    //

    private void saveDataToFile()
    {

        //create a buffered writer stream

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            fileOutputStream = new FileOutputStream(filePath);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, fileFormat);
            out = new BufferedWriter(outputStreamWriter);

            out.write(generateFileText());

            //Note! You MUST flush to make sure everything is written.
            out.flush();

        }
        catch(IOException e){
            Log.e(LOG_TAG, "Line 340 :: " + e.getMessage());
        }
        finally{
            try{ if (out != null) {out.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 344 :: " + e.getMessage());}

            try{ if (outputStreamWriter != null) {outputStreamWriter.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 347 :: " + e.getMessage());}

            try{ if (fileOutputStream != null) {fileOutputStream.close();} }
            catch(IOException e){ Log.e(LOG_TAG, "Line 350 :: " + e.getMessage()); }
        }

    }//end of TallyData::saveDataToFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setAdjustmentValue
    //
    // Sets the adjustment value using the passed in strings and recalculates all
    // of the adjusted values.
    //
    // If the string is blank, then the adjustment value is set to 0.
    //

    void setAdjustmentValue(String pValue)
    {

        if ((pValue.equals(""))) { adjustmentValue = 0; }
        else { adjustmentValue = Double.parseDouble(pValue); }

        calculateAdjustedValues();

    }//end of TallyData::setAdjustmentValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setCalibrationValue
    //
    // Sets the calibration value using the passed in string.
    //
    // If the string is blank or equals zero, then the calibration is set to 0.
    //

    void setCalibrationValue(String pValue)
    {

        if ((pValue.equals(""))) { calibrationValue = 0; }
        else { calibrationValue = Double.parseDouble(pValue); }

    }//end of TallyData::setCalibrationValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setJobInfo
    //

    public void setJobInfo(JobsHandler pJobsHandler)
    {

        jobsHandler = pJobsHandler;
        setJobInfoVariables();
        calculateAdjustedValues();
        calculateTotals();

    }//end of TallyData::setJobInfo
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setMaxAllowed
    //
    // Sets the maximum value allowed using the passed in string.
    //
    // If the string is blank or equals zero, then the maximum allowed is set to
    // Double.MAX_VALUE.
    //

    void setMaxAllowed(String pValue)
    {

        if ((pValue.equals(""))) { maxAllowed = Double.MAX_VALUE; }
        else { maxAllowed = Double.parseDouble(pValue); }

    }//end of TallyData::setMaxAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setMinAllowed
    //
    // Sets the minimum value allowed using the passed in string.
    //
    // If the string is blank or equals zero, then the minimum allowed is set to
    // Double.MIN_VALUE.
    //

    void setMinAllowed(String pValue)
    {

        if ((pValue.equals(""))) { minAllowed = Double.MIN_VALUE; }
        else { minAllowed = Double.parseDouble(pValue); }

    }//end of TallyData::setMinAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setSharedSettings
    //

    public void setSharedSettings(SharedSettings pSet)
    {

        sharedSettings = pSet;
        setSharedSettingsVariables();

    }//end of TallyData::setSharedSettings
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setTallyGoal
    //
    // Sets the tally goal using the passed in value.
    //
    // If the string is blank or equals zero, then the tally goal is set to
    // Double.MAX_VALUE.
    //

    void setTallyGoal(String pGoal)
    {

        if ((pGoal.equals("")) || (Double.parseDouble(pGoal) == 0)) {
            tallyGoal = Double.MAX_VALUE;
        }
        else {
            tallyGoal = Double.parseDouble(pGoal);
        }

    }//end of TallyData::setTallyGoal
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::storeData
    //
    // Stores the passed in data, calculates the totals, and saves everything to
    // file.
    //

    public void storeData(String pPipe, String pAdjusted, String pTotal)
    {


        pipeNumbers.add(pPipe);
        adjustedValues.add(pAdjusted);
        totalLengthValues.add(pTotal);

        calculateTotals();
        saveDataToFile();

    }//end of TallyData::storeData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::isValidLength
    //
    // Checks to see if the passed in length is in between the minimum value
    // allowed and the maximum value allowed. Returns true if it is, false if it
    // isn't.
    //

    public boolean isValidLength(double pLength)
    {

        boolean valid = true;

        if (isLessThanMinValueAllowed(pLength) || isGreaterThanMaxValueAllowed(pLength)) {
            valid = false;
        }

        return valid;

    }//end of TallyData::isValidLength
    //-----------------------------------------------------------------------------

}//end of class TallyData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
