/******************************************************************************
 * Title: TallyReportHTMLMaker.java
 * Author(s): Mike Schoonover
 * Date: 2/17/15
 *
 * Purpose:
 *
 * This is the parent class for classes which create tally reports in HTML
 * format.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

import android.util.SparseIntArray;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyReportHTMLMaker
//

public class TallyReportHTMLMaker {

    SharedSettings sharedSettings;
    JobInfo jobInfo;


    DecimalFormat decFormat;
    String filePath;
    static final int NUM_TALLY_ROWS = 42;

    //debug hss// -- should be added to job info
    String jobDate = "02/20/15";
    String adjustmentValue;
    double tallyTotal = 0;
    double adjTallyTotal = 0;
    int numTubes;
    double tallyTarget;

    private ArrayList<String> adjustedValuesFromFile = new ArrayList<String>();
    private ArrayList<String> pipeNumbersFromFile = new ArrayList<String>();
    private ArrayList<String> totalLengthValuesFromFile = new ArrayList<String>();

    static final String sp = "&nbsp;";
    static final String space3 = "&nbsp;&nbsp;&nbsp;";
    //not used yet -- static final String space4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space5 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space6 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space10 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    static final String htmlHeader = "<!DOCTYPE html>"
            + "<html style=\"font-family: 'Courier New', Courier, monospace\">"
            + "<head>"
            + "<title>Tally Zap</title>"
            + "<meta content='text/html; charset=utf-8' http-equiv='Content-Type'>"
            + "</head>"
            + "<body>";

    //if page break code is to be used, the child class sets pageBreakHTMLCode to the appropriate
    //HTML code, otherwise it should be set to ""

    String pageBreakHTMLCode = "";

    static final String htmlFooter = "</body></html>";

    static String LOG_TAG = "TallyReportHTMLMaker";

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::TallyReportHTMLMaker (constructor)
    //

    public TallyReportHTMLMaker(SharedSettings pSharedSettings, JobInfo pJobInfo)
    {

        sharedSettings = pSharedSettings;
        jobInfo = pJobInfo;

        //if user left entry blank or entered very large value, use 0 for target
        if (jobInfo.getTallyGoal().equals("")
                || Double.parseDouble(jobInfo.getTallyGoal()) > 999999) {
            tallyTarget = 0;
        }

        else { tallyTarget = Double.parseDouble(jobInfo.getTallyGoal()); }

    }// end of TallyReportHTMLMaker::TallyReportHTMLMaker (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        loadTallyDataFromFile();
        numTubes = pipeNumbersFromFile.size();

        //set the adjustment value to the adjustment
        //value used (imperial or metric)
        if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
            filePath = jobInfo.getCurrentJobDirectoryPath() + File.separator
                                                            + jobInfo.getJobName()
                                                            + " ~ TallyData ~ Imperial.csv";

            decFormat = new DecimalFormat("#.00");

            adjustmentValue = jobInfo.getImperialAdjustment();
        }
        else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
            filePath = jobInfo.getCurrentJobDirectoryPath() + File.separator
                                                            + jobInfo.getJobName()
                                                            + " ~ TallyData ~ Metric.csv";

            decFormat = new DecimalFormat("#.000");

            adjustmentValue = jobInfo.getMetricAdjustment();
        }

    }// end of TallyReportHTMLMaker::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::createPageCode
    //
    // Creates and returns the HTML code for one page.
    //

    private String createPageCode (int pIndex, int pPageNum, int pNumPages)
    {

        String htmlCode = createPageHeaderCode(numTubes, pPageNum, pNumPages);

        double pageTallyTotal = 0;
        double pageAdjTallyTotal = 0;

        for (int i=0; i<NUM_TALLY_ROWS; i++) {

            int j = pIndex + i;

            if (j >= numTubes) { break; }

            //write the first column

            double tally = Double.parseDouble(totalLengthValuesFromFile.get(j));
            double adjTally = Double.parseDouble(adjustedValuesFromFile.get(j));

            htmlCode += prePadString("" + pipeNumbersFromFile.get(j), 6) + space3
                    + prePadString(decFormat.format(tally),6) + space5
                    + prePadString(decFormat.format(adjTally),6);

            pageTallyTotal += tally;
            pageAdjTallyTotal += adjTally;

            //write the second column if not already past end of list

            int k = j + NUM_TALLY_ROWS;

            if (k < numTubes){

                tally = Double.parseDouble(totalLengthValuesFromFile.get(k));
                adjTally = Double.parseDouble(adjustedValuesFromFile.get(k));

                htmlCode += space6 + space6;
                htmlCode += prePadString("" + pipeNumbersFromFile.get(k), 6) + space3
                        + prePadString(decFormat.format(tally),6) + space5
                        + prePadString(decFormat.format(adjTally),6);

            }

            //html new line command so browser goes to next line
            htmlCode += "<br>";

        }

        htmlCode += createPageFooterCode(pPageNum, pNumPages, pageTallyTotal, pageAdjTallyTotal);

        return htmlCode;

    }// end of TallyReportHTMLMaker::createPageCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::createPageFooterCode
    //
    // Creates and returns the HTML code for a page footer.
    //

    public String createPageFooterCode(int pPageNum, int pNumPages, double pPageTallyTotal,
                                       double pPageAdjTallyTotal)
    {

        return  (
                "<br>"
                        + "<b>Page</b>" + prePadString("" + pPageNum, 4)
                        + " <b>of</b> " + prePadString("" + pNumPages, 4)
                        + space10 + space5 + sp
                        + "<b>Page Total: </b>" + prePadString(decFormat.format(pPageTallyTotal),9)
                        + sp + sp
                        + prePadString(decFormat.format(pPageAdjTallyTotal),9)
                        + "<br>" + sp
                        + "</div>"
        );

    }// end of TallyReportHTMLMaker::createPageFooterCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::createPageHeaderCode
    //
    // Creates and returns the HTML code for a page header.
    //

    public String createPageHeaderCode(int pNumTubes, int pPageNum, int pNumPages)
    {

        String htmlCode = "<div";

        //if this is not the last page, add a page break
        if (pPageNum != pNumPages) { htmlCode += pageBreakHTMLCode; }

        htmlCode += ">"
                + "<b>Company Name: </b>" + jobInfo.getCompanyName() + sp + sp + sp + sp
                + "<b>Job Name: </b>" + jobInfo.getJobName() + sp + sp + sp + sp
                + "<b>Date: </b>" + jobDate + sp
                + "<br>"
                + "<b>Adjustment: </b>" +  adjustmentValue + sp
                + "<b>Tally Target: </b>" + decFormat.format(tallyTarget) + sp + "<br>"
                + "<b>Tube Count: </b>" + pNumTubes + sp
                + "<b>Total Tally: </b>" + decFormat.format(tallyTotal) + " / "
                + decFormat.format(adjTallyTotal) + sp
                + "<br><br>"
                + "<b>Number</b>" + space3 + "<b>Length</b>" + space3 + "<b>Adjusted</b>"
                + space6 + space6
                + "<b>Number</b>" + space3 + "<b>Length</b>" + space3 + "<b>Adjusted</b>"
                + "<br>"
                + "---------------------------------------------------------------------"
                + "<br>";

        return htmlCode;

    }// end of TallyReportHTMLMaker::createPageHeaderCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::generateHTMlCode
    //
    // Generates and returns the HTML code for printing.
    //

    String generateHTMLCode()
    {

        String htmlCode = htmlHeader;
        int printIndex = 0;
        int pageNum = 1;

        int numPages = (int) Math.ceil((double)numTubes / (double)(NUM_TALLY_ROWS * 2));

        tallyTotal = 0;
        adjTallyTotal = 0;

        //calculate the total tally and total adjusted tally
        for (int i=0; i<numTubes; i++) {
            tallyTotal += Double.parseDouble(totalLengthValuesFromFile.get(i));
            adjTallyTotal += Double.parseDouble(adjustedValuesFromFile.get(i));
        }

        //create page code until there are no more tallies
        while(printIndex < numTubes) {
            htmlCode += createPageCode(printIndex, pageNum, numPages);
            pageNum++;
            printIndex += NUM_TALLY_ROWS * 2;
        }

        htmlCode += htmlFooter;

        return htmlCode;

    }// end of TallyReportHTMLMaker::generateHTMLCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getAdjustedValueFromFileLine
    //
    // Strips and returns the adjusted value within the passed in file line.
    //

    private String getAdjustedValueFromFileLine(String pLine)
    {

        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pSecondComma+1);

    }//end of TallyReportHTMLMaker::getAdjustedValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getPipeNumberFromFileLine
    //
    // Strips and returns the pipe number within the passed in file line.
    //

    private String getPipeNumberFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");

        return pLine.substring(0, pFirstComma);

    }//end of TallyReportHTMLMaker::getPipeNumberFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getTotalLengthValueFromFileLine
    //
    // Strips and returns the total length value within the passed in file line.
    //

    private String getTotalLengthValueFromFileLine(String pLine)
    {

        int pFirstComma = pLine.indexOf(",");
        int pSecondComma = pLine.lastIndexOf(",");

        return pLine.substring(pFirstComma+1, pSecondComma);

    }//end of TallyReportHTMLMaker::getTotalLengthValueFromFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::handleFileLine
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

    }//end of TallyReportHTMLMaker::handleFileLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::loadTallyDataFromFile
    //
    // Load the tally data from file.
    //

    private void loadTallyDataFromFile()
    {

        FileReader fileReader = null;
        BufferedReader bufferedReader;

        try {

            fileReader = new FileReader(filePath);

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

    }//end of TallyReportHTMLMaker::loadTallyDataFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::prePadString
    //
    // Adds spaces to pInput until it is length pToLength.
    //
    // 0 < pToLength < 10
    //

    public String prePadString(String pInput, int pToLength)
    {

        if (pToLength < 1) { pToLength = 1; }
        if (pToLength > 9) { pToLength = 9; }

        int pad = pToLength - pInput.length();

        String padding;

        switch (pad){
            case 0: padding = "";  break;
            case 1: padding = "&nbsp;"; break;
            case 2: padding = "&nbsp;&nbsp;"; break;
            case 3: padding = "&nbsp;&nbsp;&nbsp;"; break;
            case 4: padding = "&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 5: padding = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 6: padding = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 7: padding = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 8: padding = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 9: padding = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            default: padding = "";
        }

        return(padding + pInput);

    }// end of TallyReportHTMLMaker::prePadString
    //-----------------------------------------------------------------------------

}//end of class TallyReportHTMLMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
