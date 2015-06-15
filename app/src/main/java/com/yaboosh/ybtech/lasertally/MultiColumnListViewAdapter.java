/******************************************************************************
 * Title: MultiColumnListViewAdapter.java
 * Author: Hunter Schoonover
 * Date: 06/15/15
 *
 * Purpose:
 *
 * This class is a custom ArrayAdapter which can handle any number of columns
 * for each row in the ListView.
 *
 * All of the information the adapter needs to handle different numbers of rows
 * is received in the constructor parameters:
 *
 *      pLayout         =>      The resource ID for a layout file containing a
 *                                  layout use for each of the ListView rows.
 *
 *      pColumns        =>      The number of columns for each row.
 *
 *      pColumnIds      =>      The ids for the layout views that are used as
 *                                  columns inside of pLayout.
 *
 *      pList           =>      A list of HashMaps containing the values we
 *                                  want to use to populate the columns of each
 *                                  ListView row with.
 *                                  The index of each map should match the row
 *                                      in which you want the map.
 *                                  The key-value pair should be as follows:
 *                                      key = id of column
 *                                      value = value to be put into column
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MultiColumnListViewAdapter
//

public class MultiColumnListViewAdapter extends ArrayAdapter<String> {

    public static String LOG_TAG = "MultiColumnListViewAdapter";

    LayoutInflater inflater;
    private int layout;

    private static int selectedPos = -1;
    private static View selectedView = null;


    int numberOfColumns = 0;
    ArrayList<Integer> columnIds;
    ArrayList<HashMap<Integer, String>> list;

    //DEBUG HSS//
    private Activity a;
    private ArrayList<View> test = new ArrayList<View>();

    //holder to cache views
    static class ViewHolder { SparseArray<TextView> columns = new SparseArray<TextView>(); }

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::MultiColumnListViewAdapter (constructor)
    //

    public MultiColumnListViewAdapter(Activity pActivity, int pLayout, int pColumns,
                                      ArrayList<Integer> pColumnIds,
                                      ArrayList<HashMap<Integer, String>> pList)
    {

        super(pActivity, pLayout);

        a = pActivity;
        layout = pLayout;
        numberOfColumns = pColumns;
        columnIds = pColumnIds;
        list = pList;

        inflater = pActivity.getLayoutInflater();

    }//end of MultiColumnListViewAdapter::MultiColumnListViewAdapter(constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::restoreSelection
    //
    // If there a ListView row was selected, it is reselected.
    //
    // This can be used to ensure that
    //

    public void restoreSelection() {

        setSelection(selectedPos, selectedView, true);

    }//end of MultiColumnListViewAdapter::restoreSelection
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::getCount
    //

    @Override
    public int getCount() {

        return list.size();

    }//end of MultiColumnListViewAdapter::getCount
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::getView
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

        test.add(view);

        return view;

    }//end of MultiColumnListViewAdapter::getView
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::clearSelectedValues
    //
    // Clears the selected values by setting back them to their default values.
    //

    public static void clearSelectedValues() {

        selectedPos = -1;
        selectedView = null;

    }//end of MultiColumnListViewAdapter::clearSelected
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MultiColumnListViewAdapter::setSelection
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

    }//end of MultiColumnListViewAdapter::setSelection
    //-----------------------------------------------------------------------------

}//end of class MultiColumnListViewAdapter
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------