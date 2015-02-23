/******************************************************************************
* Title: MeasurementsTableHandler.java
* Author: Hunter Schoonover
* Date: 02/20/15
*
* Purpose:
*
* This class handles all actions involving the measurements table.
*
*/

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MeasurementsTableHandler
//

public class MeasurementsTableHandler {

    public static final String LOG_TAG = "MeasurementsTableHandler";

    SharedSettings sharedSettings;
    View.OnClickListener onClickListener;

    TableLayout measurementsTable;
    View measurementsTableBottomBorderLine;
    TextView totalOfAdjustedColumnTextView;
    TextView totalOfTotalLengthColumnTextView;
    TableLayout totalsTable;

    private TableRow lastAddedRow;
    public TableRow getLastAddedRow() { return lastAddedRow; }

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::MeasurementsTableHandler (constructor)
    //

    public MeasurementsTableHandler(SharedSettings pSharedSettings,
                                        View.OnClickListener pOnClickListener,
                                        TableLayout pMeasurementsTable,
                                        View pBottomBorderLine,
                                        TableLayout pTotalsTable,
                                        TextView pAdjustedTotalTextViewColumn,
                                        TextView pTotalLengthTotalTextViewColumn)
    {

        sharedSettings = pSharedSettings;
        onClickListener = pOnClickListener;
        measurementsTable = pMeasurementsTable;
        measurementsTableBottomBorderLine = pBottomBorderLine;
        totalsTable = pTotalsTable;
        totalOfAdjustedColumnTextView = pAdjustedTotalTextViewColumn;
        totalOfTotalLengthColumnTextView = pTotalLengthTotalTextViewColumn;

    }//end of MeasurementsTableHandler::MeasurementsTableHandler (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::addValuesToTable
    //
    // Adds the passed in values to the end of the measurements table and returns
    // the last added row so that the row can be used to identify the data in a
    // LinkedHashMap.
    //

    public TableRow addValuesToTable(String pPipeNum, String pTotalLength, String pAdjustedValue)
    {

        // Remove the bottom border line of the table
        // It should be put back after the row has
        // has been added.
        measurementsTable.removeView(measurementsTableBottomBorderLine);

        lastAddedRow = createNewRow(pPipeNum, pTotalLength, pAdjustedValue);
        measurementsTable.addView(lastAddedRow);
        measurementsTable.addView(createNewRowDivider());

        //Reinsert the bottom border line
        measurementsTableBottomBorderLine.setVisibility(View.VISIBLE);
        measurementsTable.addView(measurementsTableBottomBorderLine);

        return lastAddedRow;

    }//end of MeasurementsTableHandler::addValuesToTable
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::changeValuesOfExistingRow
    //
    // Changes the values of the passed in row to the passed in values.
    //
    // If the passed in boolean is true, then all pipe numbers of the rows after
    // the passed in row should be renumbered.
    //

    public void changeValuesOfExistingRow(TableRow pRow, String pPipeNum, String pTotalLength,
                                          String pAdjusted, boolean pRenumberAllAfterRow)
    {

        setPipeNumberOfRow(pRow, pPipeNum);
        setTotalLengthOfRow(pRow, pTotalLength);
        setAdjustedOfRow(pRow, pAdjusted);

        if (pRenumberAllAfterRow) { renumberAllAfterRow(pRow, (Integer.parseInt(pPipeNum)+1)); }

    }//end of MeasurementsTableHandler::changeValuesOfExistingRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::createNewColumn
    //
    // Creates and returns a TextView object with the properties of a column used
    // with the measurement table. The column's text is set to the passed in string.
    //

    private TextView createNewColumn(String pColumnText) {

        TextView col = new TextView(sharedSettings.getContext());
        col.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        col.setText(pColumnText);
        col.setTextColor(Color.BLACK);
        col.setTextSize(30);
        col.setPadding(15, 25, 15, 25);

        return col;

    }//end of MeasurementsTableHandler::createNewColumn
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::createNewColumnDivider
    //
    // Creates and returns a View object with the properties of a column
    // divider used with the measurements table.
    //

    private View createNewColumnDivider() {

        View cd = new View(sharedSettings.getContext());
        cd.setLayoutParams(new TableRow.LayoutParams(2, TableRow.LayoutParams.MATCH_PARENT));
        cd.setBackgroundColor(Color.parseColor("#505050"));

        return cd;

    }//end of MeasurementsTableHandler::createNewColumnDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::createNewRow
    //
    // Creates and returns a new table row with three columns. The passed in
    // strings are put in the columns.
    //

    private TableRow createNewRow(String pCol1Text, String pCol2Text, String pCol3Text) {

        TableRow newRow = new TableRow(sharedSettings.getContext().getApplicationContext());
        newRow.setId(R.id.measurementsTableRow);
        newRow.setClickable(true);
        newRow.setOnClickListener(onClickListener);

        newRow.addView(createNewSideBorder());

        View pipeNumCol = createNewColumn(pCol1Text);
        pipeNumCol.setId(R.id.measurementsTableColumnPipeNum);
        newRow.addView(pipeNumCol);

        newRow.addView(createNewColumnDivider());

        View actualCol = createNewColumn(pCol2Text);
        actualCol.setId(R.id.measurementsTableColumnActual);
        newRow.addView(actualCol);

        newRow.addView(createNewColumnDivider());

        View adjustedCol = createNewColumn(pCol3Text);
        adjustedCol.setId(R.id.measurementsTableColumnAdjusted);
        newRow.addView(adjustedCol);

        newRow.addView(createNewSideBorder());

        return newRow;

    }//end of MeasurementsTableHandler::createNewRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::createNewRowDivider
    //
    // Creates and returns a View object with the properties of a row divider used
    // with the measurements table.
    //

    private View createNewRowDivider() {

        View rd = new View(sharedSettings.getContext());
        rd.setId(R.id.measurementsTableRowDivider);
        rd.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3));
        rd.setBackgroundColor(Color.parseColor("#000000"));

        return rd;

    }//end of MeasurementsTableHandler::createNewRowDivider
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::createNewSideBorder
    //
    // Creates and returns a View object with the properties of a side border used
    // with the measurements table.
    //

    private View createNewSideBorder() {

        View sb = new View(sharedSettings.getContext());
        sb.setLayoutParams(new TableRow.LayoutParams(6, TableRow.LayoutParams.MATCH_PARENT));
        sb.setBackgroundColor(Color.parseColor("#000000"));

        return sb;

    }//end of MeasurementsTableHandler::createNewSideBorder
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::getDividerCountAndPositions
    //
    // Gets the number of dividers and each of their positions in the measurements
    // table and returns them in a SparseIntArray.
    //

    private SparseIntArray getDividerCountAndPositions() {

        SparseIntArray divAndPos = new SparseIntArray();

        int divCount = 0;

        // Determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            // Checks to see if the child at i is a TableRow
            if (measurementsTable.getChildAt(i).getId() == R.id.measurementsTableRowDivider) {
                divAndPos.put(++divCount, i);
            }

        }

        return divAndPos;

    }//end of MeasurementsTableHandler::getDividerCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::getPositionOfRow
    //
    // Returns the position of the passed in row in the measurementsTable.
    //

    private int getPositionOfRow(TableRow pRow) {

        // For each child in the row, get a pointer
        // to the row and compare it to the passed
        // in row.
        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            if (measurementsTable.getChildAt(i) == pRow) { return i; }

        }

        return 0;

    }//end of MeasurementsTableHandler::getPositionOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::getRowCountAndPositions
    //
    // Gets the number of rows and each of their positions in the measurements
    // table and returns them in a SparseIntArray.
    //

    private SparseIntArray getRowCountAndPositions() {

        SparseIntArray rowAndPos = new SparseIntArray();

        int rowCount = 0;

        // Determine the number of rows and their positions
        for (int i = 0; i<measurementsTable.getChildCount(); i++) {

            // Checks to see if the child at i is a TableRow
            if (!(measurementsTable.getChildAt(i) instanceof TableRow)) { continue; }

            rowAndPos.put(++rowCount, i);

        }

        return rowAndPos;

    }//end of MeasurementsTableHandler::getRowCountAndPositions
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::removeLastAddedRow
    //
    // Removes the last row of the measurements table.
    //

    public void removeLastAddedRow() {

        // Remove the last row in the table, if there is one
        SparseIntArray rowAndPosition = getRowCountAndPositions();
        int rowCount = rowAndPosition.size();
        if (rowCount <= 0) { return; }
        // Get the position of the last row found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(rowAndPosition.get(rowCount)));

        //Set the last row added to what is now the last row in the table, if there
        //is one left
        if (rowAndPosition.size() > 1) {
            lastAddedRow = (TableRow) measurementsTable.getChildAt(rowAndPosition.get(rowCount - 1));
        }


        // Remove the last row divider in the table, if there is one
        SparseIntArray dividerAndPosition = getDividerCountAndPositions();
        int divCount = dividerAndPosition.size();
        if (divCount <= 0) { return; }
        // Get the position of the last row divider found and remove it from the table
        measurementsTable.removeView(measurementsTable.getChildAt(dividerAndPosition.get(divCount)));

    }//end of MeasurementsTableHandler::removeLastAddedRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::renumberAllAfterRow
    //
    // Renumbers all of the pipe numbers after the passed in row, starting with the
    // passed in value.
    //

    private void renumberAllAfterRow(TableRow pRow, int pPipeNum) {

        for (int pos=(getPositionOfRow(pRow)+1); pos<measurementsTable.getChildCount(); pos++) {

            View v = measurementsTable.getChildAt(pos);

            if (!(v instanceof TableRow)) { continue; }

            setPipeNumberOfRow((TableRow)v, Integer.toString(pPipeNum++));

        }

    }//end of MeasurementsTableHandler::renumberAllAfterRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setAdjustedColumns
    //
    // Sets the values of all the rows in the Adjusted column using the passed
    // in Map.
    //

    public void setAdjustedColumns(Map<TableRow, String> pAdjustedValues) {

        // Set the adjusted value of each row to the
        // adjusted value in the LinkedHashMap associated
        // with that row
        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            View child = measurementsTable.getChildAt(i);

            if (!(child instanceof TableRow)) { continue; }

            TableRow tR = (TableRow) child;

            setAdjustedOfRow(tR, pAdjustedValues.get(tR));

        }

    }//end of MeasurementsTableHandler::setAdjustedColumns
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setAdjustedOfRow
    //
    // Sets the Adjusted value of the passed in row to the passed in string.
    //

    private void setAdjustedOfRow(TableRow pR, String pAdjusted) {

        // For each child in the row, check its id
        // and see if it is the Adjusted column.
        // If it is, the text of the TextView is
        // set to pAdjusted.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnAdjusted) {
                TextView tV = (TextView)pR.getChildAt(i);
                tV.setText(pAdjusted);
                break;
            }

        }

    }//end of MeasurementsTableHandler::setAdjustedOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setPipeNumberOfRow
    //
    // Sets the pipe number of the passed in row to the passed in string.
    //

    private void setPipeNumberOfRow(TableRow pR, String pPipeNum) {

        // For each child in the row, check its id
        // and see if it is the Pipe # column.
        // If it is, then the TextView is set
        // to the passed in string.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnPipeNum) {
                TextView tV = (TextView)pR.getChildAt(i);
                tV.setText(pPipeNum);
                break;
            }

        }

    }//end of MeasurementsTableHandler::setPipeNumberOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setTotalLengthColumns
    //
    // Sets the values of all the rows in the Total Length column using the passed
    // in Map.
    //

    public void setTotalLengthColumns(Map<TableRow, String> pTotalLengthValues) {

        // Set the adjusted value of each row to the
        // adjusted value in the LinkedHashMap associated
        // with that row
        for (int i=0; i<measurementsTable.getChildCount(); i++) {

            View child = measurementsTable.getChildAt(i);

            if (!(child instanceof TableRow)) { continue; }

            TableRow tR = (TableRow) child;

            setTotalLengthOfRow(tR, pTotalLengthValues.get(tR));

        }

    }//end of MeasurementsTableHandler::setTotalLengthColumns
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setTotalLengthOfRow
    //
    // Sets the Total Length value of the passed in row to the passed in string.
    //

    private void setTotalLengthOfRow(TableRow pR, String pTotalLength) {

        // For each child in the row, check its id
        // and see if it is the TotalLength column.
        // If it is, the text of the TextView is
        // set to pTotalLength.
        for (int i=0; i<pR.getChildCount(); i++) {

            if (pR.getChildAt(i).getId() == R.id.measurementsTableColumnActual) {
                TextView tV = (TextView)pR.getChildAt(i);
                tV.setText(pTotalLength);
                break;
            }

        }

    }//end of MeasurementsTableHandler::setTotalLengthOfRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MeasurementsTableHandler::setTotals
    //
    // Sets the columns for the totals of the Adjusted and Total Length values to
    // the passed in values. The background color of the totals table is set to
    // green if the tally goal was reached; set to its normal color if not.
    //

    public void setTotals(String pAdjustedTotal, String pTotalLengthTotal,
                            boolean pTallyGoalReached)
    {

        totalOfAdjustedColumnTextView.setText(pAdjustedTotal);
        totalOfTotalLengthColumnTextView.setText(pTotalLengthTotal);

        if (pTallyGoalReached) { totalsTable.setBackgroundColor(Color.parseColor("#33CC33")); }
        else {  totalsTable.setBackgroundColor(Color.parseColor("#000000")); }

    }//end of MeasurementsTableHandler::setTotals
    //-----------------------------------------------------------------------------

}//end of class MeasurementsTableHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
