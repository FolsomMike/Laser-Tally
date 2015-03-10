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
import android.widget.TableRow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TallyData {

    public static String LOG_TAG;

    JobInfo jobInfo;
    SharedSettings sharedSettings;

    DecimalFormat decFormat;

    String thisClassUnitSystem;

    double conversionFactor;

    String fileFormat = "UTF-8";
    String filePath;
    String nL = System.lineSeparator();

    double calibrationValue;
    double maxAllowed;
    double minAllowed;
    double tallyGoal;
    double adjustmentValue = 0;

    //These lists are used to store the data originally read from file.
    //They are needed because the other lists need a TableRow to insert
    //data, but the Activity cannot be accessed while the file is being
    //accessed; a table row cannot be created to store the data in the
    //normally used maps.
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

    private Map<TableRow, String> adjustedValues = new LinkedHashMap<TableRow, String>();
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
    public String getTotalLengthValueOfRow(TableRow pR) { return totalLengthValues.get(pR); }


    //abstract classes to be overridden by subclasses
    abstract void setJobInfoVariables();
    abstract void setSharedSettingsVariables();

    //-----------------------------------------------------------------------------
    // TallyData::TallyData (constructor)
    //

    public TallyData(SharedSettings pSet, JobInfo pJobInfo)
    {

        sharedSettings = pSet;
        jobInfo = pJobInfo;

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

    public void addData(TableRow pR, double pTotalLength)
    {

        double dTotal = pTotalLength;
        //since all values coming from the tally device are
        //already Imperial, the passed in length only needs
        //to be converted if the Metric is needed
        if (thisClassUnitSystem.equals(Keys.METRIC_MODE)) { dTotal = convert(dTotal); }
        dTotal += calibrationValue;

        String pipe = Integer.toString(determineNextPipeNumber());
        String adjusted = calculateAdjustedValue(dTotal);
        String total = format(dTotal);

        //store the data
        storeData(pR, pipe, adjusted, total);

    }//end of TallyData::addData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::addData
    //
    // Calculates and stores a new data entry.
    //
    // Used when the user manually sets row data.
    //
    // If necessary, the passed in lengths are converted to a different unit system.
    //
    // If the passed in boolean is true, then all of the pipe numbers in the rows
    // after the passed in row are renumbered using the passed in pipe number.
    //
    // The adjusted value is not manually set; it is determined  programmatically.
    //

    public void addData(TableRow pR, int pPipeNum, double pTotalLength, boolean pRenumber)
    {

        double dTotal = convert(pTotalLength);

        String pipe = Integer.toString(pPipeNum);
        String adjusted = calculateAdjustedValue(dTotal);
        String total = format(dTotal);

        //check to see if all of the pipe numbers after the passed in
        //row should be renumbered
        if (pRenumber) { renumberAllPipeNumbersAfterRow(pR, pipe); }

        //store the data
        storeData(pR, pipe, adjusted, total);

    }//end of TallyData::addData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::addData
    //
    // Stores the passed in data.
    //
    // Used when adding data that has already been calculated.
    //

    public void addData(TableRow pR, String pPipeNum, String pAdjustedLength, String pTotalLength)
    {

        //store the data
        storeData(pR, pPipeNum, pAdjustedLength, pTotalLength);

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

        for (Map.Entry<TableRow, String> entry : totalLengthValues.entrySet()) {

            double total = Double.parseDouble(totalLengthValues.get(entry.getKey()));

            adjustedValues.put(entry.getKey(), format(total - adjustmentValue));

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
        for (String value : adjustedValues.values()) {
            adjustedTotal += Double.parseDouble(value);
        }

        adjustedValuesTotal = format(adjustedTotal);

        //calculate the total of the total length values
        double totalLengthTotal = 0;
        for (String value : totalLengthValues.values()) {
            totalLengthTotal += Double.parseDouble(value);
        }

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
    //

    public double convert(double pValue)
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
        for (String value : pipeNumbers.values()) { pipeNumber = (Integer.parseInt(value)) + 1; }

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

        for (Map.Entry<TableRow, String> entry : pipeNumbers.entrySet()) {

            String pipeNumber = pipeNumbers.get(entry.getKey());
            String totalLength = totalLengthValues.get(entry.getKey());
            String adjusted = adjustedValues.get(entry.getKey());

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
    // TallyData::renumberAllPipeNumbersAfterRow
    //
    // Renumbers all of the pipe numbers after the passed in row.
    //
    // The first TableRow that will be changed is the one after the passed in row.
    // The first pipe number is set to the passed in pipe number plus one.
    //

    private void renumberAllPipeNumbersAfterRow(TableRow pR, String pPipe)
    {

        double pipeNumber = Double.parseDouble(pPipe);
        boolean firstRowReached = false;

        for (TableRow key : pipeNumbers.keySet()) {
            if (firstRowReached) { pipeNumbers.put(key, format(++pipeNumber));  }
            if (key == pR) { firstRowReached = true; }
        }

    }//end of TallyData::renumberAllPipeNumbersAfterRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::removeData
    //
    // Removes all of the data linked to passed in TableRow from the Maps.
    //

    public void removeData(TableRow pR)
    {

        pipeNumbers.remove(pR);
        adjustedValues.remove(pR);
        totalLengthValues.remove(pR);

        calculateTotals();

        saveDataToFile();

    }//end of TallyData::removeData
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyData::setAdjustmentValue
    //
    // Sets the adjustment value to passed in value and recalculates all of the
    // adjusted values.
    //

    void setAdjustmentValue(double pValue)
    {

        //return if the adjustment value hasn't changed
        if (adjustmentValue == pValue) { return; }

        adjustmentValue = pValue;
        calculateAdjustedValues();

    }//end of TallyData::setAdjustmentValue
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
    // TallyData::setJobInfo
    //

    public void setJobInfo(JobInfo pJobInfo)
    {

        jobInfo = pJobInfo;
        setJobInfoVariables();
        calculateAdjustedValues();
        calculateTotals();

    }//end of TallyData::setJobInfo
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

    public void storeData(TableRow pR, String pPipe, String pAdjusted, String pTotal)
    {

        putPipeNumber(pR, pPipe);
        putAdjustedValue(pR, pAdjusted);
        putTotalLengthValue(pR, pTotal);

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

        convert(pLength);
        if (isLessThanMinValueAllowed(pLength) || isGreaterThanMaxValueAllowed(pLength)) {
            valid = false;
        }

        return valid;

    }//end of TallyData::isValidLength
    //-----------------------------------------------------------------------------

}//end of class TallyData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
