/******************************************************************************
 * Title: JobInfoActivity.java
 * Author: Hunter Schoonover
 * Date: 09/15/14
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to edit job info.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobInfoActivity
//

public class JobInfoActivity extends Activity {

    public static final String TAG = "JobInfoActivity";

    private View decorView;
    private int uiOptions;

    public static final String COMPANY_NAME_KEY = "COMPANY_NAME_KEY";
    public static final String DIAMETER_KEY = "DIAMETER_KEY";
    public static final String FACILITY_KEY = "FACILITY_KEY";
    public static final String GRADE_KEY = "GRADE_KEY";
    public static final String JOB_KEY =  "JOB_KEY";
    public static final String MAKEUP_ADJUSTMENT_KEY = "PROTECTOR_MAKE_UP_ADJUSTMENT_KEY";
    public static final String RACK_KEY = "RACK_KEY";
    public static final String RANGE_KEY = "RANGE_KEY";
    public static final String RIG_KEY = "RIG_KEY";
    public static final String WALL_KEY = "WALL_KEY";

    ArrayList<String> fileLines = new ArrayList<String>();

    private String companyName;
    private String diameter;
    private String facility;
    private String grade;
    private String job;
    private String passedInJob;
    private String makeupAdjustment;
    private String rack;
    private String range;
    private String rig;
    private String wall;

    //-----------------------------------------------------------------------------
    // JobInfoActivity::JobInfoActivity (constructor)
    //

    public JobInfoActivity() {

        super();

    }//end of JobInfoActivity::JobInfoActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Inside of JobInfoActivity onCreate");

        setContentView(R.layout.activity_job_info);

        this.setFinishOnTouchOutside(false);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        createUiChangeListener();

        Bundle bundle = getIntent().getExtras();
        passedInJob = bundle.getString(JOB_KEY);

        getJobInfoFromFile();

        ((TextView)findViewById(R.id.editTextJob)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable pE) {

                handleEditTextJobTextChanged(pE.toString(), pE.length());

            }

            public void beforeTextChanged(CharSequence pS, int pStart, int pCount, int pAfter) {
            }

            public void onTextChanged(CharSequence pS, int pStart, int pBefore, int pCount) {
            }
        });

    }//end of JobInfoActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        Log.d(TAG, "Inside of JobInfoActivity onDestroy");

        super.onDestroy();

    }//end of JobInfoActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "Inside of JobInfoActivity onResume");

        decorView.setSystemUiVisibility(uiOptions);

    }//end of JobInfoActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

        Log.d(TAG, "Inside of JobInfoActivity onPause");

    }//end of JobInfoActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::checkIfJobNameAlreadyExists
    //
    // Searches through the jobs in the jobsDir directory to see if a job already
    // has the passed in name.
    //
    // Returns true if name already exists. False if it doesn't.
    //
    //

    private Boolean checkIfJobNameAlreadyExists(String pJobName) {

        Boolean exists = false;

        try {

            File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);
            File[] dirs = jobsDir.listFiles();

            for (File f : dirs) {
                if (f.isDirectory() && pJobName.equals(extractValueFromString(f.getName()))) {
                    exists = true;
                }
            }

        } catch (Exception e) {}

        return exists;

    }//end of JobInfoActivity::checkIfJobNameAlreadyExists
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::createUiChangeListener
    //
    // Listens for visibility changes in the ui.
    //
    // If the system bars are visible, the system visibility is set to the uiOptions.
    //
    //

    private void createUiChangeListener() {

        decorView.setOnSystemUiVisibilityChangeListener (
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int pVisibility) {

                        if ((pVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                    }

                });

    }//end of JobInfoActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of JobInfoActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    // Gets the job info and puts it into a file and into the intent extras, sets
    // the result to ok, and finishes the activity.
    //
    //

    private void exitActivityByOk() {

        getAndStoreJobInfoFromUserInput();

        saveInformationToFile();

        extractValueFromString("j=fall");

        Intent resultIntent = new Intent();

        resultIntent.putExtra(COMPANY_NAME_KEY, companyName);
        resultIntent.putExtra(DIAMETER_KEY, diameter);
        resultIntent.putExtra(FACILITY_KEY, facility);
        resultIntent.putExtra(GRADE_KEY,  grade);
        resultIntent.putExtra(JOB_KEY, job);
        resultIntent.putExtra(MAKEUP_ADJUSTMENT_KEY, makeupAdjustment);
        resultIntent.putExtra(RACK_KEY, rack);
        resultIntent.putExtra(RANGE_KEY, range);
        resultIntent.putExtra(RIG_KEY, rig);
        resultIntent.putExtra(WALL_KEY, wall);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }//end of JobInfoActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::extractValueFromString
    //
    // Extracts and returns the value after the equals sign from the passed in
    // string.
    //

    private String extractValueFromString(String pString) {

        int startPos = pString.indexOf("=") + 1;
        return pString.substring(startPos);

    }//end of JobInfoActivity::extractValueFromString
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::getAndStoreJobInfoFromUserInput
    //
    // Gets and stores the job info by retrieving the values entered by the user.
    //

    private void getAndStoreJobInfoFromUserInput() {

        companyName = ((EditText) findViewById(R.id.editTextCompanyName)).getText().toString();
        diameter = ((EditText) findViewById(R.id.editTextDiameter)).getText().toString();
        facility = ((EditText) findViewById(R.id.editTextFacility)).getText().toString();
        grade = ((EditText) findViewById(R.id.editTextGrade)).getText().toString();
        job = ((EditText) findViewById(R.id.editTextJob)).getText().toString();
        makeupAdjustment = ((EditText)
                        findViewById(R.id.editTextProtectorMakeupAdjustment)).getText().toString();
        rack = ((EditText) findViewById(R.id.editTextRack)).getText().toString();
        range = ((EditText) findViewById(R.id.editTextRange)).getText().toString();
        rig = ((EditText) findViewById(R.id.editTextRig)).getText().toString();
        wall = ((EditText) findViewById(R.id.editTextWall)).getText().toString();

    }//end of JobInfoActivity::getAndStoreJobInfoFromUserInput
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::getJobInfoFromFile
    //
    // Gets and stores the job info by retrieving the values from the jobInfo.txt
    // file of the current job.
    //

    private void getJobInfoFromFile() {

        try {
            fileLines.clear();

            // Retrieve/Create directory into internal memory;
            File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

            // Retrieve/Create sub-directory thisJobDir
            File thisJobDir = new File(jobsDir, "job=" + passedInJob);

            // Get a file jobInfoTextFile within the dir thisJobDir.
            File jobInfoTextFile = new File(thisJobDir, "jobInfo.txt");

            FileInputStream fStream = new FileInputStream(jobInfoTextFile);
            Scanner br = new Scanner(new InputStreamReader(fStream));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                Log.d(TAG, "New Line Found " + strLine);
                fileLines.add(strLine);
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "getJobInfoFromFile() FileNotFoundException " + e.toString());
        } catch (Exception e) {}

        // If there were no lines in the file,
        // this function is exited.
        if (fileLines.size() == 0) { return; }

        ((EditText) findViewById(R.id.editTextCompanyName)).setText
                (getValueFromList("Company Name", fileLines));
        ((EditText) findViewById(R.id.editTextDiameter)).setText
                                                    (getValueFromList("Diameter", fileLines));
        ((EditText) findViewById(R.id.editTextFacility)).setText
                                                    (getValueFromList("Facility", fileLines));
        ((EditText) findViewById(R.id.editTextGrade)).setText
                                                    (getValueFromList("Grade", fileLines));
        ((EditText) findViewById(R.id.editTextJob)).setText
                (getValueFromList("Job", fileLines));
        ((EditText) findViewById(R.id.editTextProtectorMakeupAdjustment)).setText
                (getValueFromList("Makeup Adjustment", fileLines));
        ((EditText) findViewById(R.id.editTextRack)).setText
                (getValueFromList("Rack", fileLines));
        ((EditText) findViewById(R.id.editTextRange)).setText
                (getValueFromList("Range", fileLines));
        ((EditText) findViewById(R.id.editTextRig)).setText
                (getValueFromList("Rig", fileLines));
        ((EditText) findViewById(R.id.editTextWall)).setText
                (getValueFromList("Wall", fileLines));

    }//end of JobInfoActivity::getJobInfoFromFile
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of JobInfoActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleEditTextJobTextChanged
    //
    // Checks to see if the passed in length is more than 0 and if the job name
    // already exists. If the length is more than 0 and the name does not exist,
    // then the ok button is enabled. The ok button is disabled if the opposite is
    // true.
    //
    // Called when the text in the EditText used for the Job name is changed.
    //

    private void handleEditTextJobTextChanged(String pJobName, int pLength) {

        Boolean bool = false;
        Boolean jobExistsBool = false;

        if ((pLength > 0 && !checkIfJobNameAlreadyExists(pJobName))
                || (pJobName.equals(passedInJob))) { bool = true; }

        Button okButton = (Button) findViewById(R.id.jobInfoOkButton);
        TextView textView = (TextView) findViewById(R.id.jobNameAlreadyExistsTextView);

        okButton.setEnabled(bool);

        if (bool) {
            okButton.setTextAppearance(getApplicationContext(), R.style.whiteStyledButton);
            textView.setVisibility(View.INVISIBLE);
        } else {
            okButton.setTextAppearance(getApplicationContext(), R.style.disabledStyledButton);
            textView.setVisibility(View.VISIBLE);
        }

        if (jobExistsBool) {

        }

    }//end of JobInfoActivity::handleEditTextJobTextChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleMenuButtonPressed
    //
    // Starts the JobInfoMenu activity.
    //

    public void handleMenuButtonPressed(View pView) {

        Intent intent = new Intent(this, JobInfoMenuActivity.class);
        intent.putExtra(JOB_KEY, passedInJob);
        startActivity(intent);

    }//end of JobInfoActivity::handleMenuButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of JobInfoActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::getValueFromList
    //
    // Scans through the passed in List for the passed in Key and returns the
    // value found at that string. Returns null if the Key is not found.
    //

    private String getValueFromList(String pKey, ArrayList<String> pList) {

        // If the key is found in the list, the value for the
        // key is extracted from the line and returned.
        for (String s : pList) { if (s.contains(pKey)) { return extractValueFromString(s);} }

        return null;

    }//end of JobInfoActivity::getValueFromList
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of JobInfoActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::saveInformationToFile
    //
    // Stores the job info in a file.
    //

    private void saveInformationToFile() {

        // Retrieve/Create directory into internal memory;
        File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

        // Retrieve/Create sub-directory thisJobDir
        File thisJobDir = new File(jobsDir, "job=" + passedInJob);
        if (!job.equals(passedInJob)) {
            Log.d(TAG, "Rename result: " + thisJobDir.renameTo(new File(jobsDir, "job=" + job)));
            thisJobDir = new File(jobsDir, "job=" + job);
        }

        // Get a file jobInfoTextFile within the dir thisJobDir.
        File jobInfoTextFile = new File(thisJobDir, "jobInfo.txt");
        try {
            if (!jobInfoTextFile.exists()) {
                Boolean success = jobInfoTextFile.createNewFile();
            }
        } catch (Exception e) {}

        // Use a PrintWriter to write to the file
        try {
            PrintWriter writer = new PrintWriter(jobInfoTextFile, "UTF-8");

            writer.println("Company Name=" + companyName);
            writer.println("Diameter=" + diameter);
            writer.println("Facility=" + facility);
            writer.println("Grade=" + grade);
            writer.println("Job=" + job);
            writer.println("Makeup Adjustment=" + makeupAdjustment);
            writer.println("Rack=" + rack);
            writer.println("Range=" + range);
            writer.println("Rig=" + rig);
            writer.println("Wall=" + wall);

            writer.close();
        } catch (Exception e) {
            Log.d(TAG, "Writing failed");
        }

    }//end of JobInfoActivity::saveInformationToFile
    //-----------------------------------------------------------------------------

}//end of class JobInfoActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------