/******************************************************************************
 * Title: TallyDataHandler.java
 * Author: Hunter Schoonover
 * Date: 02/21/15
 *
 * Purpose:
 *
 * This class handles the saving and reading of the tally data to file
 * and displaying the tally data in the measurements table.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.IntegerRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ListViewAdapter extends ArrayAdapter<String>{

    LayoutInflater inflater;
    private int layout;

    private int selectedPos = -1;
    private View selected = null;

    int numberOfColumns = 0;
    ArrayList<Integer> columnIds;
    ArrayList<String> columnKeys;
    ArrayList<HashMap<String, String>> list;

    //holder to cache views
    static class ViewHolder { HashMap<String, TextView> columns = new HashMap<String, TextView>(); }

    public ListViewAdapter(Activity pActivity, int pLayout, int pColumns,
                            ArrayList<Integer> pColumnIds,  ArrayList<String> pColumnKeys,
                            ArrayList<HashMap<String, String>> pList)
    {

        super(pActivity, pLayout);

        layout = pLayout;
        numberOfColumns = pColumns;
        columnIds = pColumnIds;
        columnKeys = pColumnKeys;
        list = pList;

        inflater = pActivity.getLayoutInflater();

    }

    @Override
    public int getCount() { return list.size(); }

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
                holder.columns.put(columnKeys.get(i), v);
            }

            view.setTag(holder);

        }
        else { holder = (ViewHolder)view.getTag(); }

        //set the background color of the row depending on whether it's highlighted or not
        if (pPosition == selectedPos) { view.setBackgroundColor(Color.parseColor("#0099FF")); }
        else { view.setBackgroundColor(Color.parseColor("#FFFFFF")); }

        //set the text of the columns
        for (int i=0; i<numberOfColumns; i++) {
            holder.columns.get(columnKeys.get(i))
                                            .setText(list.get(pPosition).get(columnKeys.get(i)));
        }

        return view;

    }

    public void setSelection(int pPos, View pView, boolean pSelected) {

        if (pView == null) { return; }

        if (pSelected) {
            selectedPos = pPos;
            pView.setBackgroundColor(Color.parseColor("#0099FF"));
            if (selected != null) { selected.setBackgroundColor(Color.parseColor("#FFFFFF")); }
            selected = pView;
        }
        else {
            pView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            if (selectedPos == pPos) { selectedPos = -1; }
            if (selected == pView) { selected = null; }
        }

        notifyDataSetChanged();

    }

}