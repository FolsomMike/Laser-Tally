/******************************************************************************
 * Title: TallyReportHTMLPrintoutMaker.java
 * Author(s): Mike Schoonover, Hunter Schoonover
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
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TallyReportHTMLPrintoutMaker
//

public class TallyReportHTMLPrintoutMaker extends TallyReportHTMLMaker {

    private static final String CHILD_LOG_TAG = "TallyReportHTMLPrintoutMaker";


    //-----------------------------------------------------------------------------
    // TallyReportHTMLPrintoutMaker::TallyReportHTMLPrintoutMaker (constructor)
    //

    public TallyReportHTMLPrintoutMaker(SharedSettings pSharedSettings, JobsHandler pJobsHandler)
    {

        super(pSharedSettings, pJobsHandler);

    }// end of TallyReportHTMLPrintoutMaker::TallyReportHTMLPrintoutMaker (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLPrintoutMaker::init
    //
    // Initializes the object.  Must be called immediately after instantiation.
    //

    public void init()
    {

        super.init();

        LOG_TAG = CHILD_LOG_TAG;

        //page breaks are to be inserted so pages will be separated
        pageBreakHTMLCode = " style='page-break-after: always;'";

    }// end of TallyReportHTMLPrintoutMaker::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLPrintoutMaker::printTallyReport
    //

    public void printTallyReport()
    {

        // Create a WebView object specifically for printing
        WebView webView = new WebView(sharedSettings.getContext());

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

    }// end of TallyReportHTMLPrintoutMaker::printTallyReport
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TallyReportHTMLPrintoutMaker::createWebPrintJob
    //
    // Creates a print job for the passed in WebView.
    //

    private void createWebPrintJob(WebView pWebView)
    {

        // Get a PrintManager instance
        PrintManager printManager =
                (PrintManager) sharedSettings.getContext().getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = pWebView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = sharedSettings.getContext().getString(R.string.app_name) + " Document";
        // Send the print job to the printManager
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

    }// end of TallyReportHTMLPrintoutMaker::createWebPrintJob
    //-----------------------------------------------------------------------------

}//end of class TallyReportHTMLPrintoutMaker
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------
