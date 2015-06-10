/******************************************************************************
 * Title: MoreOptionsActivity.java
 * Author: Hunter Schoonover
 * Date: 02/22/15
 *
 * Purpose:
 *
 * This class is used as an activity to display options for the more activity.
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
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MoreOptionsActivity
//

public class MoreOptionsActivity extends StandardActivity {

    private Handler handler = new Handler();

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
    // MoreOptionsActivity::MoreOptionsActivity (constructor)
    //
    // Constructor to be used for initial creation.
    //

    public MoreOptionsActivity()
    {

        layoutResID = R.layout.activity_more_options;

        LOG_TAG = "MoreOptionsActivity";

    }//end of MoreOptionsActivity::MoreOptionsActivity (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::performActivitySpecificActionsForFocusChange
    //
    // Scrolls to the top of the scrollview if the view in focus is the first item
    // in the array.
    //
    // Scrolls to the bottom of the scrollview if the view in focus is the last
    // item in the array.
    //
    // This is done because when the user is using the keyboard for navigation,
    // the last and first views are are not fully brought into sight.
    //

    @Override
    protected void performActivitySpecificActionsForFocusChange() {

        final ScrollView sv = (ScrollView)findViewById(R.id.optionsScrollView);
        sv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        int index = focusArray.indexOf(viewInFocus);
        if (index == 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_UP);
                }
            });
        }
        else if (index == (focusArray.size()-1)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_DOWN);
                }
            });
        }


    }//end of MoreOptionsActivity::performActivitySpecificActionsForFocusChange
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::handleF3KeyPressed
    //
    // Perform a click on the ok button.
    //

    @Override
    protected void handleF3KeyPressed() {

        Button okButton = (Button) findViewById(R.id.okButton);
        if (okButton != null && okButton.isEnabled()) { okButton.performClick(); }

    }//end of MoreOptionsActivity::handleF3KeyPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::performOnCreateActivitySpecificActions
    //
    // All actions that must be done upon instantiation should be done here.
    //

    @Override
    protected void performOnCreateActivitySpecificActions() {

        //add Views to focus array
        focusArray.add(findViewById(R.id.switchUnitSystemButton));
        focusArray.add(findViewById(R.id.editTextMinimumMeasurementAllowed));
        focusArray.add(findViewById(R.id.editTextMaximumMeasurementAllowed));
        focusArray.add(findViewById(R.id.calibrationValueEditText));

        maximumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMaximumMeasurementAllowed));
        minimumMeasurementAllowedEditText = ((EditText)findViewById(R.id.editTextMinimumMeasurementAllowed));
        calibrationValueEditText = ((EditText)findViewById(R.id.calibrationValueEditText));

    }//end of MoreOptionsActivity::performOnCreateActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::performOnResumeActivitySpecificActions
    //
    // All actions that must be done upon activity resume should be done here.
    //

    @Override
    protected void performOnResumeActivitySpecificActions() {

        setSwitchUnitSystemButtonText();
        setMaxAndMinEditTextFields();
        setCalibrationValueEditTextField();

    }//end of MoreOptionsActivity::performOnResumeActivitySpecificActions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::restoreActivitySpecificValuesFromSavedInstance
    //
    // Restores values using the passed in saved instance.
    //

    @Override
    protected void restoreActivitySpecificValuesFromSavedInstance(Bundle pSavedInstanceState) {

        calValue = pSavedInstanceState.getString(CAL_VALUE_KEY);
        maxAllowed = pSavedInstanceState.getString(MAX_ALLOWED_KEY);
        minAllowed = pSavedInstanceState.getString(MIN_ALLOWED_KEY);
        unitSystem = pSavedInstanceState.getString(UNIT_SYSTEM_KEY);

    }//end of MoreOptionsActivity::restoreActivitySpecificValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::storeActivitySpecificValuesFromSavedInstance
    //
    // Stores activity specific values in the passed in saved instance.
    //

    @Override
    protected void storeActivitySpecificValuesToSavedInstance(Bundle pSavedInstanceState) {

        pSavedInstanceState.putString(CAL_VALUE_KEY, getCalibrationValue());
        pSavedInstanceState.putString(MAX_ALLOWED_KEY, getMaximumAllowed());
        pSavedInstanceState.putString(MIN_ALLOWED_KEY, getMinimumAllowed());
        pSavedInstanceState.putString(UNIT_SYSTEM_KEY, unitSystem);

    }//end of MoreOptionsActivity::storeActivitySpecificValuesFromSavedInstance
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::useActivitySpecificActivityStartUpValues
    //
    // Uses default values for variables.
    //
    // Activity dependent.
    //

    @Override
    protected void useActivitySpecificActivityStartUpValues() {

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

    }//end of MoreOptionsActivity::useActivitySpecificActivityStartUpValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::convertCalValue
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

    }//end of MoreOptionsActivity::convertCalValue
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::convertMaxValueAllowed
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

    }//end of MoreOptionsActivity::convertMaxValueAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::convertMinValueAllowed
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

    }//end of MoreOptionsActivity::convertMinValueAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::exitActivityByCancel
    //
    // Used when the user closes the activity using the cancel or red x button.
    // Sets the result to canceled and finishes the activity.
    //

    private void exitActivityByCancel() {

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();

    }//end of MoreOptionsActivity::exitActivityByCancel
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::exitActivityByOk
    //
    // Used when the user closes the activity using the ok button.
    //
    // Sets the necessary data in shared settings and exits the activity.
    //

    private void exitActivityByOk() {

        sharedSettings.setGeneralSettings(unitSystem, getMinimumAllowed(), getMaximumAllowed(),
                                                                            getCalibrationValue());

        Intent intent = new Intent();

        intent.putExtra(Keys.JOBS_HANDLER_KEY, jobsHandler);
        intent.putExtra(Keys.SHARED_SETTINGS_KEY, sharedSettings);

        setResult(Activity.RESULT_OK, intent);

        finish();

    }//end of MoreOptionsActivity::exitActivityByOk
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::getCalibrationValue
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

    }//end of MoreOptionsActivity::getMaximumAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::getMinimumAllowed
    //
    // Gets and returns the minimum allowed measurement from the edit text field.
    //

    private String getMinimumAllowed() {

        return minimumMeasurementAllowedEditText.getText().toString();

    }//end of MoreOptionsActivity::getMinimumAllowed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::handleCancelButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleCancelButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of MoreOptionsActivity::handleCancelButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::handleOkButtonPressed
    //
    // Exits the activity by calling exitActivityByOk().
    //

    public void handleOkButtonPressed(View pView) {

        exitActivityByOk();

    }//end of MoreOptionsActivity::handleOkButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::handleRedXButtonPressed
    //
    // Exits the activity by calling exitActivityByCancel().
    //

    public void handleRedXButtonPressed(View pView) {

        exitActivityByCancel();

    }//end of MoreOptionsActivity::handleRedXButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::handleSwitchUnitSystemButtonPressed
    //
    // The unit system is set to either Imperial mode or Metric mode, depending
    // on the current mode.
    //

    public void handleSwitchUnitSystemButtonPressed(View pView) {

        if (unitSystem.equals(Keys.IMPERIAL_MODE)) { setUnitSystem(Keys.METRIC_MODE); }
        else if (unitSystem.equals(Keys.METRIC_MODE)) { setUnitSystem(Keys.IMPERIAL_MODE); }

    }//end of MoreOptionsActivity::handleSwitchUnitSystemButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::setCalibrationValueEditTextField
    //
    // Sets the calibration value edit text field to a preset value.
    //

    private void setCalibrationValueEditTextField() {

        calibrationValueEditText.setText(calValue);

    }//end of MoreOptionsActivity::setCalibrationValueEditTextField
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::setMaxAndMinEditTextFields
    //
    // Sets the maximum and minimum measurements allowed edit text fields to
    // the preset variables.
    //

    private void setMaxAndMinEditTextFields() {

        maximumMeasurementAllowedEditText.setText(maxAllowed);
        minimumMeasurementAllowedEditText.setText(minAllowed);

    }//end of MoreOptionsActivity::setMaxAndMinEditTextFields
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::setSwitchUnitSystemButtonText
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

    }//end of MoreOptionsActivity::setSwitchUnitSystemButtonText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MoreOptionsActivity::setUnitSystem
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

    }//end of MoreOptionsActivity::setUnitSystem
    //-----------------------------------------------------------------------------

}//end of class MoreOptionsActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------