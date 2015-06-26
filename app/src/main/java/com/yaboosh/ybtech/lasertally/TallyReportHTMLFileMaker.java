/******************************************************************************
 * Title: TallyReportHTMLFileMaker.java
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

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyReportHTMLFileMaker
//

public class TallyReportHTMLFileMaker extends TallyReportHTMLMaker {

    private static final String CHILD_LOG_TAG = "TallyReportHTMLFileMaker";


    //-----------------------------------------------------------------------------
    // TallyReportHTMLFileMaker::TallyReportHTMLFileMaker (constructor)
    //

    public TallyReportHTMLFileMaker(SharedSettings pSharedSettings, JobsHandler pJobsHandler)
    {

        super(pSharedSettings, pJobsHandler);

    }// end of TallyReportHTMLFileMaker::TallyReportHTMLFileMaker (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLFileMaker::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        super.init();

        LOG_TAG = CHILD_LOG_TAG;

        //page breaks are not to be inserted as each page is saved to a separate file
        pageBreakHTMLCode = "";

    }// end of TallyReportHTMLFileMaker::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLFileMaker::printTallyReport
    //

    public void printTallyReport()
    {

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter out = null;

        try{

            fileOutputStream = new FileOutputStream(sharedSettings.getReportsFolderPath()
                                            + jobsHandler.getJobName() + " ~ Tally Report.html");
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            out = new BufferedWriter(outputStreamWriter);

            out.write(generateHTMLCode());

            out.flush();

        }
        catch(IOException e){ Log.e(LOG_TAG, "Line 87 :: " + e.getMessage()); }
        finally{
            try{if (out != null) {out.close();}}
            catch(IOException e){ Log.e(LOG_TAG, "Line 90 :: " + e.getMessage()); }
            try{if (outputStreamWriter != null) {outputStreamWriter.close();}}
            catch(IOException e){ Log.e(LOG_TAG, "Line 92 :: " + e.getMessage()); }
            try{if (fileOutputStream != null) {fileOutputStream.close();}}
            catch(IOException e){ Log.e(LOG_TAG, "Line 94 :: " + e.getMessage()); }
        }

    }// end of TallyReportHTMLFileMaker::printTallyReport
    //-----------------------------------------------------------------------------

}//end of class TallyReportHTMLFileMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
