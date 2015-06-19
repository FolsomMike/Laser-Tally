/******************************************************************************
 * Title: MultiColumnListView.java
 * Author: Hunter Schoonover
 * Date: 06/16/15
 *
 * Purpose:
 *
 * //WIP HSS//
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
    SparseArray<View> positionToViewMap = new SparseArray<View>();
    public static final int SCROLL_MODE_NONE = 0;
    public static final int SCROLL_MODE_JUMP = 1;
    public static final int SCROLL_MODE_SMOOTH = 2;
    private final int normalRowColor = Color.parseColor("#FFFFFF");
    private final int selectedRowColor = Color.parseColor("#0099FF");
    private static int selectedPos = -1;
    private View selectedView = null;

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
                setSelectionFromTop(pPos, getHeight()/2-positionToViewMap.get(pPos).getHeight()/2);
            }
        });

    }//end of MultiColumnAdapter::centerRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::clearSelectionValues
    //
    // Clears values pertaining to row selection by setting back them to their
    // default values.
    //
    // Intended for use when a new job is opened or created -- when the new job
    // is displayed, there will be no views selected.
    //

    public static void clearSelectionValues()
    {

        selectedPos = -1;

    }//end of MultiColumnListView::clearSelectionValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::clickSelectedRow
    //
    // Brings the selected row into sight and performs a click on it.
    //

    public void clickSelectedRow()
    {

        if (selectedView == null || selectedPos == -1) { return; }

        //center the currently selected row (if there is one)
        if (selectedPos > -1) { centerRow(selectedPos); }

        //perform a click on the selected view
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

        selectRow(pPos);

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
    // MultiColumnAdapter::restoreSelection
    //
    // If a ListView row was previously selected, it is reselected and brought into
    // view by "jumping". If a row was not previously selected, the ListView jumps
    // down to display the last row.
    //

    public void restoreSelection()
    {

        if (selectedPos == -1) { jumpToRow(list.size()); }
        else { selectRow(selectedPos); }

    }//end of MultiColumnAdapter::restoreSelection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnAdapter::scrollToRow
    //
    // Brings the row at the passed in position into sight at the top of the
    // ListView by scrolling.
    //

    private void scrollToRow(final int pPos)
    {

        post(new Runnable() { @Override public void run() { smoothScrollToPosition(pPos); } });

    }//end of MultiColumnAdapter::scrollToRow
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

        //select the last row
        selectRow(lastRowIndex);

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

    public void selectNextRow()
    {

        if (selectedView == null || selectedPos == -1) { selectLastRow(); return; }

        //return if the last row is selected
        if (selectedPos == list.size()-1) { return; }

        //center the currently selected row (if there is one)
        if (selectedPos > -1) { centerRow(selectedPos); }

        selectRow(selectedPos + 1);

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

    public void selectPreviousRow()
    {

        if (selectedView == null || selectedPos == -1) { selectLastRow(); return; }

        //return if the first row is selected
        if (selectedPos == 0) { return; }

        //center the currently selected row (if there is one)
        if (selectedPos > -1) { centerRow(selectedPos); }

        selectRow(selectedPos - 1);

    }//end of MultiColumnListView::selectPreviousRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectRow
    //
    // Centers and highlights the row at the passed in position.
    //

    public void selectRow(int pPos)
    {

        //unhighlight the currently selected row (if there is one)
        if (selectedView != null) { highlightRow(selectedPos, false); }

        //set selected values to the passed in values
        selectedPos = pPos;
        selectedView = positionToViewMap.get(selectedPos);

        //highlight the new selected row
        highlightRow(selectedPos, true);

        //center the new selected row
        centerRow(selectedPos);

        adapter.notifyDataSetChanged();

    }//end of MultiColumnListView::selectRow
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

            return view;

        }//end of MultiColumnAdapter::getView
        //-----------------------------------------------------------------------------

    }//end of class MultiColumnAdapter
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class MultiColumnListView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------