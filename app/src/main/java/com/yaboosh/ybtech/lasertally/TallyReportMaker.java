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

import android.util.SparseIntArray;
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

    double tallyTotal = 0;
    double adjTallyTotal = 0;

    DecimalFormat decFormat = new  DecimalFormat("#.00");

    static final int NUM_TALLY_ROWS = 40;

    static final String sp = "&nbsp;";
    static final String space3 = "&nbsp;&nbsp;&nbsp;";
    //not used yet -- static final String space4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space5 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space6 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String space10 =
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    static final String htmlHeader =
            "<!DOCTYPE html>\n<html>\n\n<head>\n" +
                    "<meta content=\"en-us\" http-equiv=\"Content-Language\">\n" +
                    "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\n" +
                    "<title>Tally Report</title>\n" +
                    "<style type=\"text/css\">\n.auto-style1 {" +
                    "font-family: \"Courier New\", Courier, monospace;}" +
                    "</style>\n" +
                    "</head>\n\n<body>\n" +
                    "<p class=\"auto-style1\">\n";

    static final String htmlFooter = "<p>\n</body>\n\n</html>\n";

    //-----------------------------------------------------------------------------
    // TallyReportMaker::TallyReportMaker (constructor)
    //

    public TallyReportMaker(TableLayout pMeasurementsTable, String pCompanyName, String pJobName,
                            String pJobDate, double pTallyAdj, double pTallyTarget)
    {

        measurementsTable = pMeasurementsTable;

        companyName = pCompanyName;
        jobName = pJobName;
        jobDate = pJobDate;
        tallyAdj = pTallyAdj;
        tallyTarget = pTallyTarget;

//debug mks -- remove this
        String companyName = "XYZ Pipe Co";
        String jobName = "x234554";
        String jobDate =  "01/12/15";
        double tallyAdj = -2.3;
        double tallyTarget = 1995.0;
//debug mks -- remove this end

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

        int printIndex = 0;
        int pageNum = 1;

        int numPages =
                (int) Math.ceil((double)numTubes / (double)(NUM_TALLY_ROWS * 2));

        // calculate the tally totals before starting to print

        tallyTotal = 0;
        adjTallyTotal = 0;

        //calculate the total tally and total adjusted tally

        for (int i=0; i<numTubes; i++) {
            double tally = getTally(i);
            tallyTotal += tally;
            adjTallyTotal += tally + tallyAdj;
        }

        while(printIndex < numTubes){

            printPage(printIndex, pageNum, numPages);

            pageNum++;

            printIndex += NUM_TALLY_ROWS * 2;

        }

    }// end of TallyReportMaker::printTallyReport
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::printPage
    //

    public void printPage(int pIndex, int pPageNum, int pNumPages)
    {

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            fileOutputStream = new FileOutputStream("Page " + pPageNum + ".html");
            outputStreamWriter =
                    new OutputStreamWriter(fileOutputStream, "UTF-8");
            out = new BufferedWriter(outputStreamWriter);

            printPageText(out, pIndex, pPageNum, pNumPages);

            out.flush();

            //wip hss -- this is where you send the page to the printer

        }
        catch(IOException e){

        }
        finally{
            try{if (out != null) {out.close();}}
            catch(IOException e){}
            try{if (outputStreamWriter != null) {outputStreamWriter.close();}}
            catch(IOException e){}
            try{if (fileOutputStream != null) {fileOutputStream.close();}}
            catch(IOException e){}
        }

    }// end of TallyReportMaker::printPage
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::printPageText
    //

    public void printPageText(BufferedWriter pOut, int pIndex, int pPageNum, int pNumPages)
            throws IOException
    {

        double pageTallyTotal = 0;
        double pageAdjTallyTotal = 0;

        pOut.write(htmlHeader);

        printPageHeader(pOut, numTubes);

        for(int i=0; i<NUM_TALLY_ROWS; i++){

            int j = pIndex + i;

            if (j >= numTubes) { break; }

            //write the first column

            double tally = getTally(j);
            double adjTally = tally + tallyAdj;

            pOut.write("" +
                    prePadString("" + getTubeNum(j), 4) + space5
                    + prePadString(decFormat.format(tally),6) + space5
                    + prePadString(
                    decFormat.format(adjTally),6));

            pageTallyTotal += tally;
            pageAdjTallyTotal += adjTally;

            //write the second column if not already past end of list

            int k = j + NUM_TALLY_ROWS;

            if (k < numTubes){

                tally = getTally(k);
                adjTally = tally + tallyAdj;

                pOut.write(space6 + space6);
                pOut.write("" +
                        prePadString("" + getTubeNum(k), 4) + space5
                        + prePadString(decFormat.format(tally),6) + space5
                        + prePadString(
                        decFormat.format(adjTally),6));

            }

            //html new line command so browser goes to next line
            pOut.write("<br>");

            //new line to make the file human readable instead of
            //everything on one line
            pOut.write(System.lineSeparator());

        }

        printPageFooter(pOut, pPageNum, pNumPages, pageTallyTotal,
                pageAdjTallyTotal);

        pOut.write(htmlFooter);

    }// end of TallyReportMaker::printPageText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::printPageHeader
    //

    public void printPageHeader(BufferedWriter pOut, int pNumTubes)
            throws IOException
    {

        pOut.write("Company Name: " + companyName + sp + sp + sp + sp);
        pOut.write("Job Name: " + jobName + sp + sp + sp + sp);
        pOut.write("Date: " + jobDate + "<br>");
        pOut.write(System.lineSeparator());

        pOut.write("Adjustment: " +  decFormat.format(tallyAdj));
        pOut.write(" Tally Target: " + decFormat.format(tallyTarget));
        pOut.write(" Total Tally: " + decFormat.format(tallyTotal) +
                " / " + decFormat.format(adjTallyTotal));
        pOut.write(" Tube Count: " + pNumTubes);
        pOut.write("<br><br>");
        pOut.write(System.lineSeparator());

        pOut.write("Number" + space3 + "Length" + space3 + "Adjusted" + space6 + space6
                + "Number" + space3 + "Length" + space3 + "Adjusted");
        pOut.write("<br>\n");
        pOut.write("-------------------------------------------------------------"
                + "-------------------------------------------");
        pOut.write("<br>\n");

    }// end of TallyReportMaker::printPageHeader
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportMaker::printPageFooter
    //

    public void printPageFooter(BufferedWriter pOut, int pPageNum, int pNumPages,
                                double pPageTallyTotal, double pPageAdjTallyTotal) throws IOException
    {


        pOut.write("<br>" + System.lineSeparator());

        pOut.write("Page"
                + prePadString("" + pPageNum, 4) + " of "
                + prePadString("" + pNumPages, 4));

        pOut.write(space10 + space5 + sp);
        pOut.write("Page Total: ");
        pOut.write(prePadString(decFormat.format(pPageTallyTotal),9));
        pOut.write(sp + sp);
        pOut.write(prePadString(decFormat.format(pPageAdjTallyTotal),9));
        pOut.write(System.lineSeparator());

    }// end of TallyReportMaker::printPageFooter
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
            case 8: padding =
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
            case 9: padding =
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; break;
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
            }

        }

        return Double.parseDouble(actual);

    }//end of TallyReportMaker::getTally
    //-----------------------------------------------------------------------------

}//end of class TallyReportMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
