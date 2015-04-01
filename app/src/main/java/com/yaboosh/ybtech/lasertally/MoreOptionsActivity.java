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

    private String CAL_VALUE_KEY = "CAL_VALUE_KEY";
    private String MAX_ALLOWED_KEY = "MAX_ALLOWED_KEY";
    private String MIN_ALLOWED_KEY = "MIN_ALLOWED_KEY";
    private String UNIT_SYSTEM_KEY = "UNIT_SYSTEM_KEY";
    private String calValue;
    private String maxAllowed;
    private String minAllowed;
    private String unitSystem;
    private EditText calibrationValueEditText;
    private EditText maximumMeasurementAllowedEditText;
    private EditText minimumMeasurementAllowedEditText;

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
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);

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

        Bundle bundle = getIntent().getExtras();
        sharedSettings = bundle.getParcelable(Keys.SHARED_SETTINGS_KEY);
        jobInfo = bundle.getParcelable(Keys.JOB_INFO_KEY);

        // Check whether we're recreating a previously destroyed instance
        if (pSavedInstanceState != null) {
            // Restore values from saved state

            //Set the values to stored data
            calValue = pSavedInstanceState.getString(CAL_VALUE_KEY);
            maxAllowed = pSavedInstanceState.getString(MAX_ALLOWED_KEY);
            minAllowed = pSavedInstanceState.getString(MIN_ALLOWED_KEY);
            unitSystem = pSavedInstanceState.getString(UNIT_SYSTEM_KEY);

        } else {
            //initialize members with values from sharedSettings

            unitSystem = sharedSettings.getUnitSystem();

            //initialize variables using Imperial or Metric
            //values, depending on the unit system
            if (unitSystem.equals(Keys.IMPERIAL_MODE)) {
                calValue = sharedSettings.getImperialCalibrationValue();
                maxAllowed = sharedSettings.getMaximumImperialMeasurementAllowed();
                minAllowed = sharedSettings.getMinimumImperialMeasurementAllowed();
            }
            else if (unitSystem.equals(Keys.METRIC_MODE)) {
                calValue = sharedSettings.getMetricCalibrationValue();
                maxAllowed = sharedSettings.getMaximumMetricMeasurementAllowed();
                minAllowed = sharedSettings.getMinimumMetricMeasurementAllowed();
            }


        }

        maximumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMaximumMeasurementAllowed));
        minimumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMinimumMeasurementAllowed));
        calibrationValueEditText = ((EditText)findViewById(R.id.calibrationValueEditText));

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
        setMaxAndMinEditTextFields();
        setCalibrationValueEditTextField();


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
    // MenuOptionsActivity::onSaveInstanceState
    //
    // As the activity begins to stop, the system calls onSaveInstanceState()
    // so the activity can save state information with a collection of key-value
    // pairs. This functions is overridden so that additional state information can
    // be saved.
    //

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {

        //store necessary data
        pSavedInstanceState.putString(CAL_VALUE_KEY, getCalibrationValue());
        pSavedInstanceState.putString(MAX_ALLOWED_KEY, getMaximumAllowed());
        pSavedInstanceState.putString(MIN_ALLOWED_KEY, getMinimumAllowed());
        pSavedInstanceState.putString(UNIT_SYSTEM_KEY, unitSystem);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(pSavedInstanceState);

    }//end of MenuOptionsActivity::onSaveInstanceState
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::convertCalValue
    //
    // Converts the calibration value allowed to Imperial and Metric, depending on
    // the passed in unit system.
    //

    private void convertCalValue(String pSys) {

        if (pSys.equals(Keys.IMPERIAL_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Imperial value can just be gotten from sharedSettings
            if (calValue.equals(sharedSettings.getMetricCalibrationValue())
                    || calValue.equals(""))
            {
                calValue = sharedSettings.getImperialCalibrationValue();
            }
            else {
                calValue = Tools.convertToImperialAndFormat(Double.parseDouble(calValue));
            }

        }

        else if (pSys.equals(Keys.METRIC_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Metric value can just be gotten from sharedSettings
            if (calValue.equals(sharedSettings.getImperialCalibrationValue())
                    || calValue.equals(""))
            {
                calValue = sharedSettings.getMetricCalibrationValue();
            }
            else {
                calValue = Tools.convertToMetricAndFormat(Double.parseDouble(calValue));
            }

        }

    }//end of MenuOptionsActivity::convertCalValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::convertMaxValueAllowed
    //
    // Converts the maximum value allowed to Imperial and Metric, depending on
    // the passed in unit system.
    //

    private void convertMaxValueAllowed(String pSys) {

        if (pSys.equals(Keys.IMPERIAL_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Imperial value can just be gotten from sharedSettings
            if (maxAllowed.equals(sharedSettings.getMaximumMetricMeasurementAllowed())
                    || maxAllowed.equals(""))
            {
                maxAllowed = sharedSettings.getMaximumImperialMeasurementAllowed();
            }
            else {
                maxAllowed = Tools.convertToImperialAndFormat(Double.parseDouble(maxAllowed));
            }

        }

        else if (pSys.equals(Keys.METRIC_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Metric value can just be gotten from sharedSettings
            if (maxAllowed.equals(sharedSettings.getMaximumImperialMeasurementAllowed())
                    || maxAllowed.equals(""))
            {
                maxAllowed = sharedSettings.getMaximumMetricMeasurementAllowed();
            }
            else {
                maxAllowed = Tools.convertToMetricAndFormat(Double.parseDouble(maxAllowed));
            }

        }

    }//end of MenuOptionsActivity::convertMaxValueAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::convertMinValueAllowed
    //
    // Converts the minimum value allowed to Imperial and Metric, depending on
    // the passed in unit system.
    //

    private void convertMinValueAllowed(String pSys) {

        if (pSys.equals(Keys.IMPERIAL_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Imperial value can just be gotten from sharedSettings
            if (minAllowed.equals(sharedSettings.getMinimumMetricMeasurementAllowed())
                    || minAllowed.equals(""))
            {
                minAllowed = sharedSettings.getMinimumImperialMeasurementAllowed();
            }
            else {
                minAllowed = Tools.convertToImperialAndFormat(Double.parseDouble(minAllowed));
            }

        }

        else if (pSys.equals(Keys.METRIC_MODE)) {

            // If the user hasn't changed the measurement or left the
            // text field blank, then no conversion needs to be done --
            // the Metric value can just be gotten from sharedSettings
            if (minAllowed.equals(sharedSettings.getMinimumImperialMeasurementAllowed())
                    || minAllowed.equals(""))
            {
                minAllowed = sharedSettings.getMinimumMetricMeasurementAllowed();
            }
            else {
                minAllowed = Tools.convertToMetricAndFormat(Double.parseDouble(minAllowed));
            }

        }

    }//end of MenuOptionsActivity::convertMinValueAllowed
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
    // Gets and returns the calibration value from the proper edit text field.
    //

    private String getCalibrationValue() {

        return calibrationValueEditText.getText().toString();

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

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) { setUnitSystem(Keys.METRIC_MODE); }
        else if (unitSystem.equals(Keys.METRIC_MODE)) { setUnitSystem(Keys.IMPERIAL_MODE); }

    }//end of MenuOptionsActivity::handleSwitchUnitSystemButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setCalibrationValueEditTextField
    //
    // Sets the calibration value edit text field to a preset value.
    //

    private void setCalibrationValueEditTextField() {

        calibrationValueEditText.setText(calValue);

    }//end of MenuOptionsActivity::setCalibrationValueEditTextField
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setMaxAndMinEditTextFields
    //
    // Sets the maximum and minimum measurements allowed edit text fields to
    // the preset variables.
    //

    private void setMaxAndMinEditTextFields() {

        maximumMeasurementAllowedEditText.setText(maxAllowed);
        minimumMeasurementAllowedEditText.setText(minAllowed);

    }//end of MenuOptionsActivity::setMaxAndMinEditTextFields
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

    //-----------------------------------------------------------------------------
    // MenuOptionsActivity::setUnitSystem
    //
    // The unit system is set to either Imperial mode or Metric mode, depending
    // on the current mode. zzz
    //

    private void setUnitSystem(String pSys) {

        unitSystem = pSys;

        //get the values from the edit text fields
        calValue = getCalibrationValue();
        maxAllowed = getMaximumAllowed();
        minAllowed = getMinimumAllowed();

        setSwitchUnitSystemButtonText();
        convertCalValue(unitSystem);
        setCalibrationValueEditTextField();
        convertMaxValueAllowed(unitSystem);
        convertMinValueAllowed(unitSystem);
        setMaxAndMinEditTextFields();

    }//end of MenuOptionsActivity::setUnitSystem
    //-----------------------------------------------------------------------------

}//end of class MenuOptionsActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------