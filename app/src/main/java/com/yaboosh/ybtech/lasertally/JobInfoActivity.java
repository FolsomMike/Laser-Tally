/******************************************************************************
 * Title: JobInfoActivity.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class JobInfoActivity
//

public class JobInfoActivity extends Activity {

    public static final String LOG_TAG = "JobInfoActivity";

    private SharedSettings sharedSettings;

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

    private String newJobFolderPath;
    private String newJobInfoFilePath;
    private String originalJobFolderPath;
    private String originalJobInfoFilePath;

    //Keys to be used for saving an instance of the activity
    private final String COMPANY_NAME_KEY = "COMPANY_NAME_KEY";
    private final String DATE_KEY = "DATE_KEY";
    private final String DIAMETER_KEY = "DIAMETER_KEY";
    private final String FACILITY_KEY = "FACILITY_KEY";
    private final String GRADE_KEY = "GRADE_KEY";
    private final String IMPERIAL_ADJUSTMENT_KEY = "IMPERIAL_ADJUSTMENT_KEY";
    private final String IMPERIAL_TALLY_GOAL_KEY = "IMPERIAL_TALLY_GOAL_KEY";
    private final String JOB_KEY = "JOB_KEY";
    private final String METRIC_ADJUSTMENT_KEY = "METRIC_ADJUSTMENT_KEY";
    private final String METRIC_TALLY_GOAL_KEY = "METRIC_TALLY_GOAL_KEY";
    private final String RACK_KEY = "RACK_KEY";
    private final String RANGE_KEY = "RANGE_KEY";
    private final String RIG_KEY = "RIG_KEY";
    private final String WALL_KEY = "WALL_KEY";

    private String companyName;
    private String date;
    private String diameter;
    private String facility;
    private String grade;
    private String imperialAdjustment;
    private String imperialTallyGoal;
    private String job;
    private String metricAdjustment;
    private String metricTallyGoal;
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
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);

        Log.d(LOG_TAG, "Inside of JobInfoActivity onCreate");

        setContentView(R.layout.activity_job_info);

        this.setFinishOnTouchOutside(false);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        decorView = getWindow().getDecorView();

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        createUiChangeListener();

        //Get the extras from the intent
        Bundle bundle = getIntent().getExtras();

        //Set the activity mode
        setActivityMode(bundle.getString(Keys.EDIT_JOB_INFO_ACTIVITY_MODE_KEY));

        // Check whether we're recreating a previously destroyed instance
        if (pSavedInstanceState != null) {
            // Restore values from saved state

            companyName = pSavedInstanceState.getString(COMPANY_NAME_KEY);
            date = pSavedInstanceState.getString(DATE_KEY);
            diameter = pSavedInstanceState.getString(DIAMETER_KEY);
            facility = pSavedInstanceState.getString(FACILITY_KEY);
            grade = pSavedInstanceState.getString(GRADE_KEY);
            imperialAdjustment = pSavedInstanceState.getString(IMPERIAL_ADJUSTMENT_KEY);
            imperialTallyGoal = pSavedInstanceState.getString(IMPERIAL_TALLY_GOAL_KEY);
            job = pSavedInstanceState.getString(JOB_KEY);
            metricAdjustment = pSavedInstanceState.getString(METRIC_ADJUSTMENT_KEY);
            metricTallyGoal = pSavedInstanceState.getString(METRIC_TALLY_GOAL_KEY);
            rack = pSavedInstanceState.getString(RACK_KEY);
            range = pSavedInstanceState.getString(RANGE_KEY);
            rig = pSavedInstanceState.getString(RIG_KEY);
            wall = pSavedInstanceState.getString(WALL_KEY);

        } else {
            //initialize members with values from the bundle for a new instance

            sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
            //although the passed in job name may be null if the activity
            //mode is CREATE, we can still attempt to pull it out at this
            //point, so long as we do not attempt to use it unless the
            //mode is EDIT
            passedInJobName = bundle.getString(Keys.JOB_NAME_KEY);

            setOriginalFilePaths(passedInJobName);

            getJobInfoFromFile();

        }

        //Add a listener to the job name edit text field to listen for changes
        ((TextView)findViewById(R.id.editTextJob)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable pE) {

                handleEditTextJobTextChanged(pE.toString(), pE.length());

            }

            public void beforeTextChanged(CharSequence pS, int pStart, int pCount, int pAfter) {}

            public void onTextChanged(CharSequence pS, int pStart, int pBefore, int pCount) {}
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

        Log.d(LOG_TAG, "Inside of JobInfoActivity onDestroy");

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

        Log.d(LOG_TAG, "Inside of JobInfoActivity onResume");

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

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

        Log.d(LOG_TAG, "Inside of JobInfoActivity onPause");

        super.onPause();

    }//end of JobInfoActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        getAndStoreJobInfoFromUserInput();

        //store necessary data
        pSavedInstanceState.putString(COMPANY_NAME_KEY, companyName);
        pSavedInstanceState.putString(DATE_KEY, date);
        pSavedInstanceState.putString(DIAMETER_KEY, diameter);
        pSavedInstanceState.putString(FACILITY_KEY, facility);
        pSavedInstanceState.putString(GRADE_KEY, grade);
        pSavedInstanceState.putString(IMPERIAL_ADJUSTMENT_KEY, imperialAdjustment);
        pSavedInstanceState.putString(IMPERIAL_TALLY_GOAL_KEY, imperialTallyGoal);
        pSavedInstanceState.putString(JOB_KEY, job);
        pSavedInstanceState.putString(METRIC_ADJUSTMENT_KEY, metricAdjustment);
        pSavedInstanceState.putString(METRIC_TALLY_GOAL_KEY, metricTallyGoal);
        pSavedInstanceState.putString(RACK_KEY, rack);
        pSavedInstanceState.putString(RANGE_KEY, range);
        pSavedInstanceState.putString(RIG_KEY, rig);
        pSavedInstanceState.putString(WALL_KEY, wall);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(pSavedInstanceState);

    }//end of JobInfoActivity::onSaveInstanceState
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

        // activity is in CREATE mode

        if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) {

            titleTextView.setText(activityPurposeCreateJobTitle);
            menuButton.setVisibility(View.INVISIBLE);
            enableOkButton(false);
            intent = new Intent(this, JobDisplayActivity.class);

        }
        // activity is in EDIT mode
        else if (activityMode.equals(EditJobInfoActivityMode.EDIT_JOB_INFO)) {

            titleTextView.setText(activityPurposeEditJobInfoTitle);
            menuButton.setVisibility(View.VISIBLE);
            enableOkButton(true);
            intent = new Intent();

        }

    }//end of JobInfoActivity::setActivityMode
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::checkIfJobNameAlreadyExists
    //
    // Searches through the jobs in the jobs directory to see if a job already
    // has the passed in name.
    //
    // Returns true if name already exists. False if it doesn't.
    //
    //

    private Boolean checkIfJobNameAlreadyExists(String pJobName) {

        Boolean exists = false;

        try {

            // Retrieve the jobs directory
            File jobsDir = new File (sharedSettings.getJobsFolderPath());

            // All of the names of the directories in the
            // jobs directory are job names. If one of the
            // directory names is equal to the passed
            // in job, then the job already exists
            File[] dirs = jobsDir.listFiles();
            for (File f : dirs) {
                if (f.isDirectory() && pJobName.equals(f.getName())) { exists = true; }
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

        setNewFilePaths(job);

        saveInformationToFile();

        JobInfo jobInfo = new JobInfo(newJobFolderPath, companyName, date, diameter, facility,
                                        grade, imperialAdjustment, imperialTallyGoal, job,
                                        metricAdjustment, metricTallyGoal, rack, range, rig, wall);
        jobInfo.init();

        intent.putExtra(Keys.JOB_INFO_INCLUDED_KEY, true);
        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) { startActivity(intent); }

        setResult(Activity.RESULT_OK, intent);

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

        date = ((EditText) findViewById(R.id.editTextDate)).getText().toString();

        diameter = ((EditText) findViewById(R.id.editTextDiameter)).getText().toString();

        facility = ((EditText) findViewById(R.id.editTextFacility)).getText().toString();

        grade = ((EditText) findViewById(R.id.editTextGrade)).getText().toString();

        job = ((EditText) findViewById(R.id.editTextJob)).getText().toString();

        setAdjustmentValues(((EditText)findViewById(R.id.editTextProtectorMakeupAdjustment)).
                                                                            getText().toString());

        rack = ((EditText) findViewById(R.id.editTextRack)).getText().toString();

        range = ((EditText) findViewById(R.id.editTextRange)).getText().toString();

        rig = ((EditText) findViewById(R.id.editTextRig)).getText().toString();

        setTallyGoals(((EditText)findViewById(R.id.editTextTallyGoal)).getText().toString());

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

        ArrayList<String> fileLines = new ArrayList<String>();
        FileInputStream fStream = null;
        Scanner br = null;

        try {

            //Retrieve the job info file for the current job
            File file = new File(originalJobInfoFilePath);

            //read the file into an array list
            fStream = new FileInputStream(file);
            br = new Scanner(new InputStreamReader(fStream));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                fileLines.add(strLine);
            }

        }
        catch (Exception e) {}
        finally {

            try {
                if (br != null) { br.close(); }
                if (fStream != null) { fStream.close(); }
            }
            catch (Exception e) {}

        }

        // If there were no lines in the file,
        // this function is exited.
        if (fileLines.isEmpty()) { return; }

        ((EditText) findViewById(R.id.editTextCompanyName)).setText
                                            (Tools.getValueFromList("Company Name", fileLines));

        ((EditText) findViewById(R.id.editTextDate)).setText
                                                    (Tools.getValueFromList("Date", fileLines));

        ((EditText) findViewById(R.id.editTextDiameter)).setText
                                            (Tools.getValueFromList("Diameter", fileLines));

        ((EditText) findViewById(R.id.editTextFacility)).setText
                                            (Tools.getValueFromList("Facility", fileLines));

        ((EditText) findViewById(R.id.editTextGrade)).setText
                                            (Tools.getValueFromList("Grade", fileLines));

        ((EditText) findViewById(R.id.editTextJob)).setText
                                            (Tools.getValueFromList("Job", fileLines));

        ((EditText) findViewById(R.id.editTextRack)).setText
                                            (Tools.getValueFromList("Rack", fileLines));

        ((EditText) findViewById(R.id.editTextRange)).setText
                                            (Tools.getValueFromList("Range", fileLines));

        ((EditText) findViewById(R.id.editTextRig)).setText
                                            (Tools.getValueFromList("Rig", fileLines));

        ((EditText) findViewById(R.id.editTextWall)).setText
                                            (Tools.getValueFromList("Wall", fileLines));


        imperialAdjustment = Tools.getValueFromList("Imperial Adjustment", fileLines);
        metricAdjustment = Tools.getValueFromList("Metric Adjustment", fileLines);
        imperialTallyGoal = Tools.getValueFromList("Imperial Tally Goal", fileLines);
        metricTallyGoal = Tools.getValueFromList("Metric Tally Goal", fileLines);
        // use the metric or the imperial values
        // depending on the unit system
        String adjustment = "";
        String goal = "";
        if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
            adjustment = imperialAdjustment;
            goal = imperialTallyGoal;
        }
        else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
            adjustment = metricAdjustment;
            goal = metricTallyGoal;
        }
        ((EditText) findViewById(R.id.editTextProtectorMakeupAdjustment)).setText(adjustment);
        ((EditText) findViewById(R.id.editTextTallyGoal)).setText(goal);

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
        if (!pJobName.equals(passedInJobName) && checkIfJobNameAlreadyExists(pJobName)) {
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
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);
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
    // JobInfoActivity::setAdjustmentValues
    //
    // Sets the imperial and metric adjustment values using the passed in value.
    //
    // If the unit system is set to Imperial, then the passed in value is assumed
    // to be Imperial and is converted to Metric for the metric ajustment value.
    //
    // If the unit system is set to Metric, then the passed in value is assumed
    // to be Metric and is converted to Imperial for the imperial adjustment value.
    //

    private void setAdjustmentValues(String pValue) {

        //if the adjustment value hasn't changed,
        //then no action needs to be taken
        if ((sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)
                && pValue.equals(imperialAdjustment))
            || (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)
                && pValue.equals(metricAdjustment)))
        {
            return;
        }

        if (!(pValue.equals(""))) {

            Double adjDouble = Double.parseDouble(((EditText) findViewById
                                    (R.id.editTextProtectorMakeupAdjustment)).getText().toString());

            //Take different actions depending on the unit system
            if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
                //passed in value is assumed to be Imperial
                imperialAdjustment = Tools.IMPERIAL_TALLY_FORMAT.format(adjDouble);
                metricAdjustment = Tools.convertToMetricAndFormat(adjDouble);
            }
            else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
                //passed in value is assumed to be Metric
                metricAdjustment = Tools.METRIC_TALLY_FORMAT.format(adjDouble);
                imperialAdjustment = Tools.convertToImperialAndFormat(adjDouble);
            }

        }
        else {
            imperialAdjustment = "";
            metricAdjustment = "";
        }

    }//end of JobInfoActivity::setAdjustmentValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::setNewFilePaths
    //
    // Sets important file paths involving the location of the job files using
    // the passed in job name.
    //

    private void setNewFilePaths(String pJobName) {

        newJobFolderPath = sharedSettings.getJobsFolderPath() + File.separator + pJobName;
        newJobInfoFilePath = newJobFolderPath + File.separator + pJobName + " ~ JobInfo.txt";

    }//end of JobInfoActivity::setNewFilePaths
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::setOriginalFilePaths
    //
    // Sets the originals file paths for important files involving the location of
    // the job files using the passed in job name.
    //
    // The originals are stored so that they can be used later to copy the job
    // from the old location to a new location; you need to use the original file
    // paths to copy the job from the original location.
    //

    private void setOriginalFilePaths(String pJobName) {

        originalJobFolderPath = sharedSettings.getJobsFolderPath() + File.separator + pJobName;
        originalJobInfoFilePath = originalJobFolderPath + File.separator + pJobName + " ~ JobInfo.txt";

    }//end of JobInfoActivity::setOriginalFilePaths
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::setTallyGoals
    //
    // Sets the imperial and metric tally goals using the passed in value.
    //
    // If the unit system is set to Imperial, then the passed in value is assumed
    // to be Imperial and is converted to Metric for the metric adjustment value.
    //
    // If the unit system is set to Metric, then the passed in value is assumed
    // to be Metric and is converted to Imperial for the imperial adjustment value.
    //

    private void setTallyGoals(String pValue) {

        //if the adjustment value hasn't changed,
        //then no action needs to be taken
        if ((sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)
                && pValue.equals(imperialTallyGoal))
                || (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)
                && pValue.equals(metricTallyGoal)))
        {
            return;
        }

        if (!(pValue.equals(""))) {

            Double goalDouble = Double.parseDouble(
                            ((EditText) findViewById(R.id.editTextTallyGoal)).getText().toString());

            //Take different actions depending on the unit system
            if (sharedSettings.getUnitSystem().equals(Keys.IMPERIAL_MODE)) {
                //passed in value is assumed to be Imperial
                imperialTallyGoal = Tools.IMPERIAL_TALLY_FORMAT.format(goalDouble);
                metricTallyGoal = Tools.convertToMetricAndFormat(goalDouble);
            }
            else if (sharedSettings.getUnitSystem().equals(Keys.METRIC_MODE)) {
                //passed in value is assumed to be Metric
                metricTallyGoal = Tools.METRIC_TALLY_FORMAT.format(goalDouble);
                imperialTallyGoal = Tools.convertToImperialAndFormat(goalDouble);
            }

        }

        else {
            imperialTallyGoal = "";
            metricTallyGoal = "";
        }

    }//end of JobInfoActivity::setTallyGoals
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // JobInfoActivity::saveInformationToFile
    //
    // Stores the job info in a file.
    //

    private void saveInformationToFile() {

        PrintWriter writer = null;

        try {

            // Create the directory for this job
            // or rename the old directory for the
            // new job name

            File thisJobDir;

            // Activity is in CREATE mode
            if (activityMode.equals(EditJobInfoActivityMode.CREATE_JOB)) {

                thisJobDir = new File(newJobFolderPath);

                if (!thisJobDir.exists()) { thisJobDir.mkdir(); }

            }
            // Activity is in EDIT mode
            else if (activityMode.equals(EditJobInfoActivityMode.EDIT_JOB_INFO)) {

                thisJobDir = new File(originalJobFolderPath);

                // if the job name has changed, the directory
                // needs to be renamed
                if (!job.equals(passedInJobName)) {
                    thisJobDir.renameTo(new File(newJobFolderPath));
                }

            }

            // end of Create the directory for this job
            // or rename the old directory for the
            // new job name

            //Get the job info file. Create it if it does not exist
            File jobInfoFile = new File (newJobInfoFilePath);
            if (!jobInfoFile.exists()) { jobInfoFile.createNewFile(); }

            // Use a PrintWriter to write to the file
            writer = new PrintWriter(jobInfoFile, "UTF-8");

            writer.println("Company Name=" + companyName);
            writer.println("Date=" + date);
            writer.println("Diameter=" + diameter);
            writer.println("Facility=" + facility);
            writer.println("Grade=" + grade);
            writer.println("Imperial Adjustment=" + imperialAdjustment);
            writer.println("Imperial Tally Goal=" + imperialTallyGoal);
            writer.println("Job=" + job);
            writer.println("Metric Adjustment=" + metricAdjustment);
            writer.println("Metric Tally Goal=" + metricTallyGoal);
            writer.println("Rack=" + rack);
            writer.println("Range=" + range);
            writer.println("Rig=" + rig);
            writer.println("Wall=" + wall);

        }
        catch (Exception e) {}
        finally { try { if (writer != null) { writer.close(); } } catch (Exception e) {} }

    }//end of JobInfoActivity::saveInformationToFile
    //-----------------------------------------------------------------------------

}//end of class JobInfoActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------