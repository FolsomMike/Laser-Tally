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

import java.text.DecimalFormat;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyReportHTMLMaker
//

public class TallyReportHTMLMaker {

    SharedSettings sharedSettings;

    TableLayout measurementsTable;

    int numTubes;
    SparseIntArray rowAndPos;

    String companyName;
    String jobName;
    String jobDate;
    double tallyAdj;
    double tallyTarget;

    double tallyTotal = 0;
    double adjTallyTotal = 0;

    DecimalFormat decFormat = new  DecimalFormat("#.00");

    static final int NUM_TALLY_ROWS = 42;

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

    public TallyReportHTMLMaker(SharedSettings pSharedSettings,
            TableLayout pMeasurementsTable, String pCompanyName,
                          String pJobName, String pJobDate, double pTallyAdj, double pTallyTarget)
    {

        sharedSettings = pSharedSettings;
        measurementsTable = pMeasurementsTable;

        companyName = pCompanyName;
        jobName = pJobName;
        jobDate = pJobDate;
        tallyAdj = pTallyAdj;
        tallyTarget = pTallyTarget;

        //if user left entry blank or entered very large value, print 0 for target
        if (tallyTarget > 999999){ tallyTarget = 0; }

    }// end of TallyReportHTMLMaker::TallyReportHTMLMaker (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        // get the row count and each of their positions
        rowAndPos = getRowCountAndPositions();

        numTubes = rowAndPos.size();

    }// end of TallyReportHTMLMaker::init
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
            case 8: padding =
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 9: padding =
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            default: padding = "";
        }

        return(padding + pInput);

    }// end of TallyReportHTMLMaker::prePadString
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getRowCountAndPositions
    //
    // Gets the number of rows and each of their positions in the measurements table
    // and returns them in a SparseIntArray.
    //
    // There will be children in the table which are not rows. This creates an array where each
    // position in the array holds the table's child index of a valid row which contains tally
    // information. Thus any row can be retrieved by using the corresponding child index in the
    // array.
    //

    private SparseIntArray getRowCountAndPositions() {

        SparseIntArray rowAndPos = new SparseIntArray();

        int rowCount = 0;

        // determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            // checks to see if the child at i is a TableRow
            if (!(measurementsTable.getChildAt(i) instanceof TableRow)) { continue; }

            rowAndPos.put(++rowCount, i);

        }

        return rowAndPos;

    }//end of TallyReportHTMLMaker::getRowCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getTubeNum
    //
    // Returns the value under the Pipe # column for row number pRow.
    //
    // Value is returned as an integer.
    //

    int getTubeNum(int pRow) {

        TableRow row = (TableRow) measurementsTable.getChildAt(rowAndPos.get(pRow));

        String pipeNum = "0";

        // For each child in the row, check its id
        // and see if it is the Pipe # column.
        for (int i=0; i<row.getChildCount(); i++) {

            if (row.getChildAt(i).getId() == R.id.measurementsTableColumnPipeNum) {
                TextView tV = (TextView)row.getChildAt(i);
                pipeNum = tV.getText().toString();
                break;
            }

        }

        return Integer.parseInt(pipeNum);

    }//end of TallyReportHTMLMaker::getTubeNum
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLMaker::getTally
    //
    // Returns the value under the Total Length column for row number pRow.
    //
    // Value is returned as a Double.
    //

    double getTally(int pRow) {

        TableRow row = (TableRow) measurementsTable.getChildAt(rowAndPos.get(pRow));

        String actual = "0.0";

        // For each child in the row, check its id
        // and see if it is the Actual column.
        for (int i=0; i<row.getChildCount(); i++) {

            if (row.getChildAt(i).getId() == R.id.measurementsTableColumnActual) {
                TextView tV = (TextView)row.getChildAt(i);
                actual = tV.getText().toString();
                break;
            }

        }

        return Double.parseDouble(actual);

    }//end of TallyReportHTMLMaker::getTally
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
            double tally = getTally(i);
            tallyTotal += tally;
            adjTallyTotal += tally + tallyAdj;
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

            double tally = getTally(j);
            double adjTally = tally + tallyAdj;

            htmlCode += prePadString("" + getTubeNum(j), 6) + space3
                    + prePadString(decFormat.format(tally),6) + space5
                    + prePadString(decFormat.format(adjTally),6);

            pageTallyTotal += tally;
            pageAdjTallyTotal += adjTally;

            //write the second column if not already past end of list

            int k = j + NUM_TALLY_ROWS;

            if (k < numTubes){

                tally = getTally(k);
                adjTally = tally + tallyAdj;

                htmlCode += space6 + space6;
                htmlCode += prePadString("" + getTubeNum(k), 6) + space3
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
                + "<b>Company Name: </b>" + companyName + sp + sp + sp + sp
                + "<b>Job Name: </b>" + jobName + sp + sp + sp + sp
                + "<b>Date: </b>" + jobDate + sp
                + "<br>"
                + "<b>Adjustment: </b>" +  decFormat.format(tallyAdj) + sp
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

}//end of class TallyReportHTMLMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
