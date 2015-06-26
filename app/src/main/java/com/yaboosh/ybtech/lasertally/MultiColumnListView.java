/******************************************************************************
 * Title: MultiColumnListView.java
 * Author: Hunter Schoonover
 * Date: 06/16/15
 *
 * Purpose:
 *
 * This class extends ListView by adding functions that allow developers to
 * use this as a ListView with multiple columns.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MultiColumnListView
//

public class MultiColumnListView extends ListView {

    public static String LOG_TAG = "MultiColumnListView";

    private Activity activity;
    int layout;
    int numberOfColumns;
    ArrayList<Integer> columnIds;
    ArrayList<SparseArray<String>> list = null;

    private MultiColumnAdapter adapter;

    //Values for row selection
    public static final int STARTING_POSITION_FIRST_ROW = 0;
    public static final int STARTING_POSITION_LAST_ROW = 1;
    private int startingPosition = STARTING_POSITION_FIRST_ROW;
    SparseArray<View> positionToViewMap = new SparseArray<View>();
    private final int normalRowColor = Color.parseColor("#FFFFFF");
    private final int selectedRowColor = Color.parseColor("#0099FF");
    public static final String SELECTION_POS_KEY = "SELECTION_POS_KEY";
    private int selectedPos = -1;
    public int getSelectedPosition() { return  selectedPos; }
    private int newSelectedRowPosition = -1;

    //holder to cache views used with the adapter
    private static class ViewHolder { SparseArray<TextView> columns = new SparseArray<TextView>(); }

    //-----------------------------------------------------------------------------
    // MultiColumnListView::MultiColumnListView (constructors)
    //

    public MultiColumnListView(Context context) {
        super(context);
    }

    public MultiColumnListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiColumnListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //end of MultiColumnListView::MultiColumnListView(constructors)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::init
    //
    // Sets essential values and uses them to populate the ListView.
    //

    public void init(Activity pActivity, int pLayout, int pNumberOfColumns,
                      ArrayList<Integer> pColumnIds)
    {

        activity = pActivity;
        layout = pLayout;
        numberOfColumns = pNumberOfColumns;
        columnIds = pColumnIds;

        adapter = new MultiColumnAdapter();
        setAdapter(adapter);

    }//end of MultiColumnListView::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::centerRow
    //
    // Centers the row at the passed in position in the ListView by "jumping".
    //

    private void centerRow(final int pPos)
    {

        post(new Runnable() {
            @Override
            public void run() {
                setSelectionFromTop(pPos, getHeight() / 2 - positionToViewMap.get(pPos).getHeight() / 2);
            }
        });

    }//end of MultiColumnAdapter::centerRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::checkIfRowIsVisible
    //
    // Checks to see the row at the passed in position is visible.
    //
    // Returns true if visible; false if not.
    //

    private boolean checkIfRowIsVisible(final int pPos)
    {

        if (pPos >= getFirstVisiblePosition() && pPos <= getLastVisiblePosition()) { return true; }

        return false;

    }//end of MultiColumnAdapter::checkIfRowIsVisible
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::clickSelectedRow
    //
    // Brings the selected row into sight and performs a click on it.
    //

    public void clickSelectedRow()
    {

        if (selectedPos == -1 || list.isEmpty()) { return; }

        //center the currently selected row (if there is one)
        if (selectedPos > -1) { centerRow(selectedPos); }

        //perform a click on the selected view
        View selectedView = positionToViewMap.get(selectedPos);
        performItemClick(selectedView, selectedPos, selectedView.getId());

    }//end of MultiColumnAdapter::clickSelectedRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::handleRowClicked
    //
    // Selects the row associated with the passed in position
    //
    // Should be called from an onclicklistener each time the user clicks on a
    // ListView item/row.
    //

    public void handleRowClicked(int pPos)
    {

        selectRow(pPos, true);

    }//end of MultiColumnListView::handleRowClicked
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::highlightRow
    //
    // Highlights or unhighlights the row at the passed in position, depending
    // on the passed in boolean.
    //

    private void highlightRow(int pPos, boolean pSelected)
    {

        if (pPos == -1) { return; }

        if (pSelected) { positionToViewMap.get(pPos).setBackgroundColor(selectedRowColor); }
        else { positionToViewMap.get(pPos).setBackgroundColor(normalRowColor); }

    }//end of MultiColumnAdapter::highlightRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::jumpToRow
    //
    // Brings the row at the passed in position into sight at the top of the
    // ListView by "jumping".
    //

    private void jumpToRow(final int pPos)
    {

        post(new Runnable() { @Override public void run() { setSelection(pPos); } });

    }//end of MultiColumnAdapter::jumpToRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::jumpToRow
    //
    // Scrolls the ListView by "jumping" so that the row at the passed in position
    // is pPosFromTop from the top of the ListView display window.
    //

    private void jumpToRow(final int pPos, final int pPosFromTop)
    {

        post(new Runnable() {
            @Override public void run() { setSelectionFromTop(pPos, pPosFromTop); }
        });

    }//end of MultiColumnAdapter::jumpToRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::jumpToStartingRow
    //
    // Jumps to either the first or last row, depending on the starting position.
    //

    public void jumpToStartingRow()
    {

        if (startingPosition == STARTING_POSITION_FIRST_ROW) { jumpToRow(0); }
        else if (startingPosition == STARTING_POSITION_LAST_ROW) { jumpToRow(list.size()); }

    }//end of MultiColumnListView::jumpToStartingRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::scrollToRow
    //
    // Brings the row at the passed in position into sight at the top of the
    // ListView by scrolling.
    //

    private void scrollToRow(final int pPos)
    {

        post(new Runnable() {
            @Override
            public void run() {
                smoothScrollToPosition(pPos);
            }
        });

    }//end of MultiColumnAdapter::scrollToRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::selectFirstRow
    //
    // Selects and highlights the first row in the ListView.
    //

    public void selectFirstRow()
    {

        int firstRowIndex = 0;

        //return if the last row is already
        //selected or if there are no rows
        if (selectedPos == firstRowIndex || list.isEmpty()) { return; }

        //jump to the bottom of the ListView
        jumpToRow(firstRowIndex);

        //if the first row is not visible, this means that it
        //currently does not have a View assigned to it and
        //we must wait to select it until after the adapter
        //assigns it one
        if (!checkIfRowIsVisible(firstRowIndex)) { newSelectedRowPosition = firstRowIndex;}
        else { selectRow(firstRowIndex, false); }

    }//end of MultiColumnAdapter::selectFirstRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::selectLastRow
    //
    // Selects and highlights the last row in the ListView.
    //

    public void selectLastRow()
    {

        int lastRowIndex = list.size()-1;

        //return if the last row is already
        //selected or if there are no rows
        if (selectedPos == lastRowIndex || list.isEmpty()) { return; }

        //jump to the bottom of the ListView
        jumpToRow(lastRowIndex);

        //if the last row is not visible, this means that it
        //currently does not have a View assigned to it and
        //we must wait to select it until after the adapter
        //assigns it one
        if (!checkIfRowIsVisible(lastRowIndex)) { newSelectedRowPosition = lastRowIndex;}
        else { selectRow(lastRowIndex, false); }

    }//end of MultiColumnAdapter::selectLastRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectNextRow
    //
    // Selects and highlights the next row in the ListView.
    //
    // To highlight the next row, the currently selected row is brought into sight
    // and unhighlighted. Then, the next row is highlighted and scrolled to be
    // centered.
    //
    // If a row is not currently selected, the last one in the ListView is selected.
    //

    public void selectNextRow() {

        if (selectedPos == -1) { selectStartingRow(); return; }

        //return if the last row is selected
        if (selectedPos == list.size()-1) { return; }

        //center the currently selected row if it's not visible
        if (!checkIfRowIsVisible(selectedPos)) { centerRow(selectedPos); }

        int newSelectedPos = selectedPos+1;

        //if the next row is not completely visible, bring it into sight
        View selectedView = positionToViewMap.get(selectedPos);
        if (selectedView.getBottom()+selectedView.getHeight() > getHeight()) {
            jumpToRow(newSelectedPos, getHeight() - selectedView.getHeight());
        }

        //if the row that is to be selected is not visible, this
        //means that it currently does not have a View assigned
        //to it and we must wait to select it until after the
        //adapter assigns it one
        if (!checkIfRowIsVisible(newSelectedPos)) { newSelectedRowPosition = newSelectedPos; }
        else { selectRow(newSelectedPos, false); }

    }//end of MultiColumnListView::selectNextRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectPreviousRow
    //
    // Selects and highlights the previous row in the ListView.
    //
    // To highlight the previous row, the currently selected row is brought into
    // sight and unhighlighted. Then, the previous row is highlighted and scrolled
    // to be centered.
    //
    // If a row is not currently selected, the last one in the ListView is selected.
    //

    public void selectPreviousRow() {

        if (selectedPos == -1) { selectStartingRow(); return; }

        //return if the first row is selected
        if (selectedPos == 0) { return; }

        //center the currently selected row if it's not visible
        if (!checkIfRowIsVisible(selectedPos)) { centerRow(selectedPos); }

        int newSelectedPos = selectedPos-1;

        //if the previous row is not visible, bring it into sight
        View selectedView = positionToViewMap.get(selectedPos);
        if (selectedView.getTop()-selectedView.getHeight() < 0) { jumpToRow(newSelectedPos); }

        //if the row that is to be selected is not visible, this
        //means that it currently does not have a View assigned
        //to it and we must wait to select it until after the
        //adapter assigns it one
        if (!checkIfRowIsVisible(newSelectedPos)) { newSelectedRowPosition = newSelectedPos; }
        else { selectRow(newSelectedPos, false); }



    }//end of MultiColumnListView::selectPreviousRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectRow
    //
    // Centers and highlights the row at the passed in position.
    //

    public void selectRow(int pPos, boolean center)
    {

        //unhighlight the currently selected row (if there is one)
        if (selectedPos != -1) { highlightRow(selectedPos, false); }

        //set selected values to the passed in values
        selectedPos = pPos;

        //highlight the new selected row
        highlightRow(selectedPos, true);

        //if specified, center the new selected row
        if (center) { centerRow(selectedPos); }

        adapter.notifyDataSetChanged();

    }//end of MultiColumnListView::selectRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectStartingRow
    //
    // Selects either the first or last row, depending on the starting position.
    //

    private void selectStartingRow()
    {

        if (startingPosition == STARTING_POSITION_FIRST_ROW) { selectFirstRow(); }
        else if (startingPosition == STARTING_POSITION_LAST_ROW) { selectLastRow(); }

    }//end of MultiColumnListView::selectStartingRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::setList
    //
    // Sets the display list to the new passed in list and notifies the adapter.
    //

    public void setList(ArrayList<SparseArray<String>> pList)
    {

        list = pList;

        adapter.notifyDataSetChanged();

    }//end of MultiColumnListView::setList
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::setSelectionStartingPosition
    //
    // Sets the selection starting position to the passed in value.
    //

    public void setSelectionStartingPosition(final int pPos)
    {

        startingPosition = pPos;

    }//end of MultiColumnAdapter::setSelectionStartingPosition
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------
    // class MultiColumnAdapter
    //

    private class MultiColumnAdapter extends ArrayAdapter<String> {

        public String LOG_TAG = "MultiColumnListViewAdapter";

        LayoutInflater inflater;

        //-----------------------------------------------------------------------------
        // MultiColumnAdapter::MultiColumnAdapter(constructor)
        //

        public MultiColumnAdapter()
        {

            super(activity, layout);

            inflater = activity.getLayoutInflater();

        }//end of MultiColumnAdapter::MultiColumnAdapter
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // MultiColumnAdapter::getCount
        //

        @Override
        public int getCount()
        {

            int count = 0;
            if (list != null) { count = list.size(); }
            return count;

        }//end of MultiColumnAdapter::getCount
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // MultiColumnAdapter::getView
        //
        // Converts and returns the passed in view for use in a ListView.
        //

        @Override
        public View getView(int pPosition, View pView, ViewGroup pParent)
        {

            View view = pView;
            ViewHolder holder;

            if (view == null) {

                view = inflater.inflate(layout, pParent, false);

                //cache views into the holder
                holder = new ViewHolder();

                for (int i=0; i<numberOfColumns; i++) {
                    TextView v = (TextView)view.findViewById(columnIds.get(i));
                    holder.columns.put(columnIds.get(i), v);
                }

                view.setTag(holder);

            }
            else { holder = (ViewHolder)view.getTag(); }

            //set the background color of the row depending on whether it's highlighted or not
            if (pPosition == selectedPos) { view.setBackgroundColor(selectedRowColor); }
            else { view.setBackgroundColor(normalRowColor); }

            //set the text of the columns
            for (int i=0; i<numberOfColumns; i++) {
                holder.columns.get(columnIds.get(i))
                                                .setText(list.get(pPosition).get(columnIds.get(i)));
            }

            //link this view to the position it was used for
            positionToViewMap.put(pPosition, view);

            if (pPosition == newSelectedRowPosition) {
                selectRow(newSelectedRowPosition, false);
                newSelectedRowPosition = -1;
            }

            return view;

        }//end of MultiColumnAdapter::getView
        //-----------------------------------------------------------------------------

    }//end of class MultiColumnAdapter
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class MultiColumnListView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------