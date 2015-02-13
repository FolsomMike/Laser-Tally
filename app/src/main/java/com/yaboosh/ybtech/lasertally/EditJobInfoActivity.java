/******************************************************************************
 * Title: EditJobInfoActivity.java
 * Author: Hunter Schoonover
 * Date: 09/15/14
 *
 * Purpose:
 *
 * This class is used as an activity to display a user interface that allows
 * users to create a job and edit job info, depending on the mode.
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class EditJobInfoActivity
//

public class EditJobInfoActivity extends Activity {

    public static final String TAG = "EditJobInfoActivity";

    private View decorView;
    private int uiOptions;

    private DecimalFormat tallyFormat = new DecimalFormat("#.##");

    ArrayList<String> fileLines = new ArrayList<String>();

    public static class EditJobInfoActivityMode {
        public static String CREATE_JOB = "CREATE_JOB";
        public static String EDIT_JOB_INFO = "EDIT_JOB_INFO";
    }

    private String activityMode;
    private Intent intent;

    private String activityPurposeCreateJobTitle = "Create Job";
    private String activityPurposeEditJobInfoTitle = "Edit Job";
    private String passedInJobName;

    private String companyName;
    private String diameter;
    private String facility;
    private String grade;
    private String job;
    private String makeupAdjustment;
    private String rack;
    private String range;
    private String rig;
    private String tallyGoal;
    private String wall;

    //-----------------------------------------------------------------------------
    // JobInfoActivity::JobInfoActivity (constructor)
    //

    public EditJobInfoActivity() {

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

        setContentView(R.layout.activity_edit_job_info);

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
        passedInJobName = bundle.getString(Keys.JOB_NAME_KEY);
        setActivityMode(bundle.getString(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY));

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

        Log.d(TAG, "Inside of JobInfoActivity onPause");

        super.onPause();

    }//end of JobInfoActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::setActivityMode
    //
    // Sets the activity mode to the passed in value and takes different
    // actions depending on the mode.
    //
    //

    private void setActivityMode(String pActivityMode) {

        activityMode = pActivityMode;

        View menuButton = findViewById(R.id.editJobInfoMenuButton);
        TextView titleTextView = (TextView)findViewById(R.id.editJobInfoActivityTitleTextView);

        if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) {
            titleTextView.setText(activityPurposeCreateJobTitle);
            menuButton.setVisibility(View.INVISIBLE);
            enableOkButton(false);
            intent = new Intent(this, JobDisplayActivity.class);
        }
        else if (activityMode.equals(EditJobInfoActivityMode.EDIT_JOB_INFO)) {
            titleTextView.setText(activityPurposeEditJobInfoTitle);
            menuButton.setVisibility(View.VISIBLE);
            enableOkButton(true);
            intent = new Intent();
            getJobInfoFromFile();
        }

    }//end of JobInfoActivity::setActivityMode
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
                if (f.isDirectory() && pJobName.equals(Tools.extractValueFromString(f.getName()))) {
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
    // JobInfoActivity::enableOkButton
    //
    // Sets the ok button to enabled or disabled depending on the passed in
    // boolean.
    //

    private void enableOkButton(boolean pBool) {

        Button okButton = (Button) findViewById(R.id.okButton);

        if (pBool) {
            okButton.setEnabled(true);
            okButton.setTextAppearance(getApplicationContext(), R.style.whiteStyledButton);
        } else {
            okButton.setEnabled(false);
            okButton.setTextAppearance(getApplicationContext(), R.style.disabledStyledButton);
        }

    }//end of JobInfoActivity::enableOkButton
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

        intent.putExtra(Keys.COMPANY_NAME_KEY, companyName);
        intent.putExtra(Keys.DIAMETER_KEY, diameter);
        intent.putExtra(Keys.FACILITY_KEY, facility);
        intent.putExtra(Keys.GRADE_KEY,  grade);
        intent.putExtra(Keys.JOB_NAME_KEY, job);
        intent.putExtra(Keys.ADJUSTMENT_KEY, makeupAdjustment);
        intent.putExtra(Keys.RACK_KEY, rack);
        intent.putExtra(Keys.RANGE_KEY, range);
        intent.putExtra(Keys.RIG_KEY, rig);
        intent.putExtra(Keys.TALLY_GOAL_KEY, tallyGoal);
        intent.putExtra(Keys.WALL_KEY, wall);

        setResult(Activity.RESULT_OK, intent);

        if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) {
            intent.putExtra(Keys.JOB_INFO_INCLUDED_KEY, true);
            startActivity(intent);
        }

        finish();

    }//end of JobInfoActivity::exitActivityByOk
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

        makeupAdjustment = ((EditText)findViewById(R.id.editTextProtectorMakeupAdjustment)).getText().toString();
        if ((makeupAdjustment.equals(""))) { makeupAdjustment = tallyFormat.format(0); }
        else {
            Float tempAdjFloat = Float.parseFloat(((EditText) findViewById
                                    (R.id.editTextProtectorMakeupAdjustment)).getText().toString());
            makeupAdjustment = tallyFormat.format(tempAdjFloat);
        }

        rack = ((EditText) findViewById(R.id.editTextRack)).getText().toString();
        range = ((EditText) findViewById(R.id.editTextRange)).getText().toString();
        rig = ((EditText) findViewById(R.id.editTextRig)).getText().toString();

        tallyGoal = ((EditText)findViewById(R.id.editTextTallyGoal)).getText().toString();
        if (!(tallyGoal.equals(""))) {
            Float tempAdjFloat = Float.parseFloat(((EditText) findViewById(R.id.editTextTallyGoal))
                                                                            .getText().toString());
            tallyGoal = tallyFormat.format(tempAdjFloat);
        }

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
            File thisJobDir = new File(jobsDir, "job=" + passedInJobName);

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
                (Tools.getValueFromList("Company Name", fileLines));

        ((EditText) findViewById(R.id.editTextDiameter)).setText
                                                    (Tools.getValueFromList("Diameter", fileLines));

        ((EditText) findViewById(R.id.editTextFacility)).setText
                                                    (Tools.getValueFromList("Facility", fileLines));

        ((EditText) findViewById(R.id.editTextGrade)).setText
                                                    (Tools.getValueFromList("Grade", fileLines));

        ((EditText) findViewById(R.id.editTextJob)).setText
                (Tools.getValueFromList("Job", fileLines));

        ((EditText) findViewById(R.id.editTextProtectorMakeupAdjustment)).setText
                (Tools.getValueFromList("Makeup Adjustment", fileLines));

        ((EditText) findViewById(R.id.editTextRack)).setText
                (Tools.getValueFromList("Rack", fileLines));

        ((EditText) findViewById(R.id.editTextRange)).setText
                (Tools.getValueFromList("Range", fileLines));

        ((EditText) findViewById(R.id.editTextRig)).setText
                (Tools.getValueFromList("Rig", fileLines));

        ((EditText) findViewById(R.id.editTextTallyGoal)).setText
                (Tools.getValueFromList("Tally Goal", fileLines));

        ((EditText) findViewById(R.id.editTextWall)).setText
                (Tools.getValueFromList("Wall", fileLines));

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
    // Determines whether or not the ok button should be enabled and whether or
    // not the jobNameAlreadyTextView should be set visible.
    //
    // Called when the text in the EditText used for the Job name is changed.
    //

    private void handleEditTextJobTextChanged(String pJobName, int pLength) {

        Boolean enableOkButton = false;
        Boolean jobExistsBool = false;

        // Check to see if the job name already exists and to see if the
        // user did not just retype the original name of the job.
        if (checkIfJobNameAlreadyExists(pJobName) && !pJobName.equals(passedInJobName)) {
            jobExistsBool = true;
        }

        // Check to see if the length of the edit text is greater than
        // 0 and to see if the job does not already exist.
        if (pLength > 0 && !jobExistsBool) { enableOkButton = true; }

        TextView textView = (TextView) findViewById(R.id.jobNameAlreadyExistsTextView);

        enableOkButton(enableOkButton);

        if (jobExistsBool) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
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
        intent.putExtra(Keys.JOB_NAME_KEY, passedInJobName);
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

        Log.d(TAG, getFilesDir().toString());

        // Retrieve/Create directory into internal memory;
        File jobsDir = getDir("jobsDir", Context.MODE_PRIVATE);

        File thisJobDir = null;

        if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) {
            thisJobDir = new File(jobsDir, "job=" + job);
            if (!thisJobDir.exists()) { thisJobDir.mkdir(); }
        }
        else if (activityMode.equals(EditJobInfoActivityMode.EDIT_JOB_INFO)) {
            thisJobDir = new File(jobsDir, "job=" + passedInJobName);
            if (!job.equals(passedInJobName)) {
                thisJobDir.renameTo(new File(jobsDir, "job=" + job));
                thisJobDir = new File(jobsDir, "job=" + job);
            }
        }

        if (thisJobDir == null) { return; }

        // Get a file jobInfoTextFile within the dir thisJobDir.
        File jobInfoTextFile = new File(thisJobDir, "jobInfo.txt");
        try { if (!jobInfoTextFile.exists()) { jobInfoTextFile.createNewFile(); } }
        catch (Exception e) {}

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
            writer.println("Tally Goal=" + tallyGoal);
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