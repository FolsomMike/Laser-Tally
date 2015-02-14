/******************************************************************************
 * Title: TallyReportMaker.java
 * Author: Mike Schoonover
 * Date: 2/12/15
 *
 * Purpose:
 *
 * This class prints a report listing all tallies.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyReportMaker
//

public class TallyReportMaker {

    TableLayout measurementsTable;

    int numTubes;
    SparseIntArray rowAndPos;

    String companyName = "XYZ Pipe Co";
    String jobName = "x234554";
    String jobDate =  "01/12/15";
    double tallyAdj = -2.3;
    double tallyTarget = 1995.0;
    Context context;

    double tallyTotal = 0;
    double adjTallyTotal = 0;

    DecimalFormat decFormat = new  DecimalFormat("#.00");

    static final int NUM_TALLY_ROWS = 45;

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

    static final String htmlFooter = "</body></html>";

    //-----------------------------------------------------------------------------
    // TallyReportMaker::TallyReportMaker (constructor)
    //

    public TallyReportMaker(TableLayout pMeasurementsTable, String pCompanyName, String pJobName,
                            String pJobDate, double pTallyAdj, double pTallyTarget, Context pContext)
    {

        measurementsTable = pMeasurementsTable;

        companyName = pCompanyName;
        jobName = pJobName;
        jobDate = pJobDate;
        tallyAdj = pTallyAdj;
        tallyTarget = pTallyTarget;
        context = pContext;

        //if user left entry blank, print 0 for target
        if (tallyTarget > 999999) { tallyTarget = 0; }

    }// end of TallyReportMaker::TallyReportMaker (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        // get the row count and each of their positions
        rowAndPos = getRowCountAndPositions();

        numTubes = rowAndPos.size();

    }// end of TallyReportMaker::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::printTallyReport
    //

    public void printTallyReport()
    {

        // Create a WebView object specifically for printing
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView pView, String pUrl) {
                return false;
            }

            @Override
            public void onPageFinished(WebView pView, String pUrl) {
                createWebPrintJob(pView);
            }

        });

        // Generate HTML code on the fly and load it into webView:
        webView.loadDataWithBaseURL(null, generateHTMLCode(), "text/HTML", "UTF-8", null);

    }// end of TallyReportMaker::printTallyReport
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::createWebPrintJob
    //
    // Creates a print job for the passed in WebView.
    //

    private void createWebPrintJob(WebView pWebView)
    {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = pWebView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = context.getString(R.string.app_name) + " Document";
        // Send the print job to the printManager
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

    }// end of TallyReportMaker::createWebPrintJob
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::generateHTMlCode
    //
    // Generates and returns the HTML code for printing.
    //

    private String generateHTMLCode()
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

    }// end of TallyReportMaker::generateHTMLCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::createPageCode
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

            htmlCode += prePadString("" + getTubeNum(j), 4) + space5
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
                htmlCode += prePadString("" + getTubeNum(k), 4) + space5
                            + prePadString(decFormat.format(tally),6) + space5
                            + prePadString(decFormat.format(adjTally),6);

            }

            //html new line command so browser goes to next line
            htmlCode += "<br>";

        }

        htmlCode += createPageFooterCode(pPageNum, pNumPages, pageTallyTotal, pageAdjTallyTotal);

        return htmlCode;

    }// end of TallyReportMaker::createPageCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::createPageHeaderCode
    //
    // Creates and returns the HTML code for a page header.
    //

    public String createPageHeaderCode(int pNumTubes, int pPageNum, int pNumPages)
    {

        String htmlCode = "<div";

        //if this is not the last page, add a page break
        if (pPageNum != pNumPages) { htmlCode += " style='page-break-after: always;'"; }

        htmlCode += ">"
                    + "Company Name: " + companyName + sp + sp + sp + sp
                    + "Job Name: " + jobName + sp + sp + sp + sp
                    + "Date: " + jobDate
                    + "<br>"
                    + "Adjustment: " +  decFormat.format(tallyAdj)
                    + " Tally Target: " + decFormat.format(tallyTarget)
                    + " Total Tally: " + decFormat.format(tallyTotal) + " / "
                                                        + decFormat.format(adjTallyTotal)
                    + " Tube Count: " + pNumTubes
                    + "<br><br>"
                    + "Number" + space3 + "Length" + space3 + "Adjusted"
                    + space6 + space6 + "Number" + space3 + "Length" + space3 + "Adjusted"
                    + "<br>"
                    + "---------------------------------------------------------------------"
                    + "<br>";

        return htmlCode;

    }// end of TallyReportMaker::createPageHeaderCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::createPageFooterCode
    //
    // Creates and returns the HTML code for a page footer.
    //

    public String createPageFooterCode(int pPageNum, int pNumPages, double pPageTallyTotal,
                                                                        double pPageAdjTallyTotal)
    {

        String htmlCode = "<br>"
                            + "Page" + prePadString("" + pPageNum, 4)
                            + " of " + prePadString("" + pNumPages, 4)
                            + space10 + space5 + sp
                            + "Page Total: " + prePadString(decFormat.format(pPageTallyTotal),9)
                            + sp + sp
                            + prePadString(decFormat.format(pPageAdjTallyTotal),9)
                            + "</div>";

        return htmlCode;

    }// end of TallyReportMaker::createPageFooterCode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::prePadString
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
            case 8: padding ="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 9: padding ="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            default: padding = "";
        }

        return(padding + pInput);

    }// end of TallyReportMaker::prePadString
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::getRowCountAndPositions
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

    }//end of TallyReportMaker::getRowCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::getTubeNum
    //
    // Returns the value under the Pipe # column for row number pRow.
    //
    // Value is returned as an integer.
    //

    private int getTubeNum(int pRow) {

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

    }//end of TallyReportMaker::getTubeNum
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::getTally
    //
    // Returns the value under the Total Length column for row number pRow.
    //
    // Value is returned as a Double.
    //

    private double getTally(int pRow) {

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

    }//end of TallyReportMaker::getTally
    //-----------------------------------------------------------------------------

}//end of class TallyReportMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
