/******************************************************************************
 * Title: MenuOptionsActivity.java
 * Author: Hunter Schoonover
 * Date: 02/22/15
 *
 * Purpose:
 *
 * This class is used as an activity to display the menu options for the
 * application.
 *
 * The activity displays:
 *      Switch to Metric/Switch to Imperial button (shows one or the other)
 *      Minimum Measurement Allowed entry (double value entry...any measurement less than this will be ignored)
 *      Maximum Measurement Allowed entry (double value entry...any measurement greater than this will be ignored)
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MenuOptionsActivity
//

public class MoreOptionsActivity extends Activity {

    public static final String LOG_TAG = "MoreOptionsActivity";

    private View decorView;
    private int uiOptions;

    private SharedSettings sharedSettings;
    private JobInfo jobInfo;

    private EditText maximumMeasurementAllowedEditText;
    private EditText minimumMeasurementAllowedEditText;
    private EditText calibrationValueEditText;
    private Spinner addSubCalibrationValueSpinner;

    private String unitSystem;
    private String switchToImperialButtonText = "Switch to Imperial";
    private String switchToMetricButtonText = "Switch to Metric";

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::MenuOptionsActivity (constructor)
    //

    public MoreOptionsActivity() {

        super();

    }//end of MenuOptionsActivity::MenuOptionsActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon creation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more_options);

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

        maximumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMaximumMeasurementAllowed));
        minimumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMinimumMeasurementAllowed));
        calibrationValueEditText = ((EditText)findViewById(R.id.calibrationValueEditText));
        addSubCalibrationValueSpinner = ((Spinner)findViewById(R.id.addSubCalibrationValueSpinner));

        Bundle bundle = getIntent().getExtras();
        sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
        jobInfo = bundle.getParcelable(Keys.JOB_INFO_KEY);

        unitSystem = sharedSettings.getUnitSystem();

    }//end of MenuOptionsActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

    }//end of MenuOptionsActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onResume
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onResume() {

        super.onResume();

        decorView.setSystemUiVisibility(uiOptions);

        sharedSettings.setContext(this);

        setSwitchUnitSystemButtonText();
        setMaxAndMinEditTextFields(sharedSettings.getMaximumMeasurementAllowed(),
                                sharedSettings.getMinimumMeasurementAllowed());


    }//end of MenuOptionsActivity::onResume
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::onPause
    //
    // Automatically called when the activity is paused when it does not have
    // user's focus but it still partially visible.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onPause() {

        super.onPause();

    }//end of MenuOptionsActivity::onPause
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::createUiChangeListener
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

    }//end of MenuOptionsActivity::createUiChangeListener
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of MenuOptionsActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    //
    // Sets the necessary data in shared settings and exits the activity.
    //

    private void exitActivityByOk() {

        sharedSettings.setGeneralSettings(unitSystem, getMinimumAllowed(), getMaximumAllowed(),
                                                                            getCalibrationValue());

        Intent intent = new Intent();

        intent.putExtra(Keys.JOB_INFO_KEY, jobInfo);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        setResult(Activity.RESULT_OK, intent);

        finish();

    }//end of MenuOptionsActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::getCalibrationValue
    //
    // Gets and returns the calibration value allowed measurement from the edit
    // text field.
    //
    // Uses the add/subtract spinner to determine whether or not the value should
    // be negative or positive.
    //

    private String getCalibrationValue() {


        String calValueString = calibrationValueEditText.getText().toString();

        //if the edit text field was left blank,
        // then just return the empty string
        if (calValueString.equals("")) { return calValueString; }

        double cal = Double.parseDouble(calValueString);

        //determine whether or not the cal value
        //should be negative
        String addOrSub = addSubCalibrationValueSpinner.getSelectedItem().toString();
        if (addOrSub.equals("sub")) { cal = -cal; }
        calValueString = Double.toString(cal);

        return calValueString;

    }//end of MenuOptionsActivity::getCalibrationValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::getMaximumAllowed
    //
    // Gets and returns the maximum allowed measurement from the edit text field.
    //

    private String getMaximumAllowed() {

        return maximumMeasurementAllowedEditText.getText().toString();

    }//end of MenuOptionsActivity::getMaximumAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::getMinimumAllowed
    //
    // Gets and returns the minimum allowed measurement from the edit text field.
    //

    private String getMinimumAllowed() {

        return minimumMeasurementAllowedEditText.getText().toString();

    }//end of MenuOptionsActivity::getMinimumAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of MenuOptionsActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of MenuOptionsActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        finish();

    }//end of MenuOptionsActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::handleSwitchUnitSystemButtonPressed
    //
    // The unit system is set to either Imperial mode or Metric mode, depending
    // on the current mode.
    //

    public void handleSwitchUnitSystemButtonPressed(View pView) {

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) { unitSystem = Keys.METRIC_MODE; }
        else if (unitSystem.equals(Keys.METRIC_MODE)) { unitSystem = Keys.IMPERIAL_MODE; }

        setMaximumAndMinimumMeasurementsAllowed();
        setSwitchUnitSystemButtonText();

    }//end of MenuOptionsActivity::handleSwitchUnitSystemButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setMaxAndMinEditTextFields
    //
    // Sets the maximum and minimum measurements allowed edit text fields passed
    // in.
    //

    private void setMaxAndMinEditTextFields(String pMax, String pMin) {

        maximumMeasurementAllowedEditText.setText(pMax);

        minimumMeasurementAllowedEditText.setText(pMin);

    }//end of MenuOptionsActivity::setMaxAndMinEditTextFields
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setMaximumAndMinimumMeasurementsAllowed
    //
    // Converts the maximum and minimum measurements allowed to either Imperial
    // or Metric, depending on the unit system. The new values are sent to the
    // edit text fields.
    //

    private void setMaximumAndMinimumMeasurementsAllowed() {

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) {
            String newMax = Tools.convertToImperial(Double.parseDouble(getMaximumAllowed()));
            String newMin = Tools.convertToImperial(Double.parseDouble(getMinimumAllowed()));
            setMaxAndMinEditTextFields(newMax, newMin);
        }
        else if (unitSystem.equals(Keys.METRIC_MODE)) {
            String newMax = Tools.convertToMetric(Double.parseDouble(getMaximumAllowed()));
            String newMin = Tools.convertToMetric(Double.parseDouble(getMinimumAllowed()));
            setMaxAndMinEditTextFields(newMax, newMin);
        }

    }//end of MenuOptionsActivity::setMaximumAndMinimumMeasurementsAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setSwitchUnitSystemButtonText
    //
    // Determines what text the button should be displaying.
    //
    // If the app is already in Imperial mode the button text is set to:
    //      "Switch to Metric"
    //
    // If the app is already in Metric mode the button text is set to:
    //      "Switch to Imperial"
    //

    private void setSwitchUnitSystemButtonText() {

        Button button = (Button)findViewById(R.id.switchUnitSystemButton);

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) { button.setText(switchToMetricButtonText); }
        else if (unitSystem.equals(Keys.METRIC_MODE)) { button.setText(switchToImperialButtonText); }

    }//end of MenuOptionsActivity::setSwitchUnitSystemButtonText
    //-----------------------------------------------------------------------------

}//end of class MenuOptionsActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------