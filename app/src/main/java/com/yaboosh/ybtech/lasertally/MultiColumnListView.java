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
import android.graphics.Color;
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
    ArrayList<SparseArray<String>> list;

    private MultiColumnAdapter adapter;

    //Values for row selection
    private static int selectedPos = -1;
    private static View selectedView = null;
    private static int selectedVisiblePosition = -1;

    //holder to cache views used with the adapter
    private static class ViewHolder { SparseArray<TextView> columns = new SparseArray<TextView>(); }

    //-----------------------------------------------------------------------------
    // MultiColumnListView::MultiColumnListView (constructor)
    //

    public MultiColumnListView(Activity pActivity)
    {

        super(pActivity);

    }//end of MultiColumnListView::MultiColumnListView(constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::init
    //
    // Sets essential values and uses them to populate the ListView.
    //

    public void init(Activity pActivity, int pLayout, int pNumberOfColumns,
                              ArrayList<Integer> pColumnIds, ArrayList<SparseArray<String>> pList)
    {

        activity = pActivity;
        layout = pLayout;
        numberOfColumns = pNumberOfColumns;
        columnIds = pColumnIds;
        list = pList;

        adapter = new MultiColumnAdapter();
        setAdapter(adapter);

        selectRow();

    }//end of MultiColumnListView::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::clearSelectionValues
    //
    // Clears the selection values by setting back them to their default values.
    //

    public static void clearSelectionValues() {

        selectedPos = -1;
        selectedView = null;

    }//end of MultiColumnListView::clearSelectionValues
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectNextRow
    //
    // //WIP HSS//
    //

    public void selectNextRow() {

        zzz
        setSelectionFromTop(pPos, getHeight()/2);


        adapter.setSelection();

    }//end of MultiColumnListView::selectNextRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectRow
    //
    // Highlights and centers the row at the passed in position without scrolling.
    //

    public void selectRow(int pPos) {

        setSelectionFromTop(pPos, getHeight()/2);


        adapter.setSelection();

    }//end of MultiColumnListView::selectRow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListView::selectRowSmoothly
    //
    // Highlights and centers the row at the passed in position by scrolling.
    //

    public void selectRowSmoothly(int pPos) {

        smoothScrollToPositionFromTop(pPos, getHeight()/2);

        getVisi

    }//end of MultiColumnListView::selectRowSmoothly
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
        // MultiColumnAdapter::restoreSelection
        //
        // If there a ListView row was selected, it is reselected.
        //
        // This can be used to ensure that
        //

        public void restoreSelection() {

            setSelection(selectedPos, selectedView, true);

        }//end of MultiColumnAdapter::restoreSelection
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // MultiColumnAdapter::getCount
        //

        @Override
        public int getCount() {

            return list.size();

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
            if (pPosition == selectedPos) { view.setBackgroundColor(Color.parseColor("#0099FF")); }
            else { view.setBackgroundColor(Color.parseColor("#FFFFFF")); }

            //set the text of the columns
            for (int i=0; i<numberOfColumns; i++) {
                holder.columns.get(columnIds.get(i))
                                                .setText(list.get(pPosition).get(columnIds.get(i)));
            }

            return view;

        }//end of MultiColumnAdapter::getView
        //-----------------------------------------------------------------------------

        //-----------------------------------------------------------------------------
        // MultiColumnAdapter::setSelection
        //
        // Selects or deselects the passed in view at the passed in position,
        // depending on the passed in boolean.
        //

        public void setSelection(int pPos, View pView, boolean pSelected) {

            if (pPos == -1 || pView == null) { return; }

            if (pSelected) {
                selectedPos = pPos;
                pView.setBackgroundColor(Color.parseColor("#0099FF"));
                if (selectedView != null) {
                    selectedView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                selectedView = pView;
            }
            else {
                pView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                if (selectedPos == pPos) { selectedPos = -1; }
                if (selectedView == pView) { selectedView = null; }
            }

            notifyDataSetChanged();

        }//end of MultiColumnAdapter::setSelection
        //-----------------------------------------------------------------------------

    }//end of class MultiColumnAdapter
    //-----------------------------------------------------------------------------
    //-----------------------------------------------------------------------------

}//end of class MultiColumnListView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------