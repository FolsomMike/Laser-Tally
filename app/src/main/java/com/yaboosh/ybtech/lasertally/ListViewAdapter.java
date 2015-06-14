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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewAdapter extends ArrayAdapter<String>{

    LayoutInflater inflater;
    private int selectedPos = -1;
    private View selected = null;
    private final ArrayList<String> adjustedValues;
    private final ArrayList<String> pipeNumbers;
    private final ArrayList<String> totalLengthValues;

    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

    static class ViewHolder {
        TextView col1;
        TextView col2;
        TextView col3;
    }

    public ListViewAdapter(Activity pContext, ArrayList<String> pPipeNumbers,
                           ArrayList<String> pTotalLengthValues, ArrayList<String> pAdjustedValues)
    {

        super(pContext, R.layout.layout_list_view_row, pAdjustedValues);

        adjustedValues = pAdjustedValues;
        pipeNumbers = pPipeNumbers;
        totalLengthValues = pTotalLengthValues;

        inflater = pContext.getLayoutInflater();

    }

    @Override
    public View getView(int pPosition, View pView, ViewGroup pParent)
    {

        View view = pView;
        ViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.layout_list_view_row, pParent, false);

            //cache views into the holder
            holder = new ViewHolder();
            holder.col1 = (TextView)view.findViewById(R.id.COLUMN_PIPE_NUMBER);
            holder.col2 = (TextView)view.findViewById(R.id.COLUMN_TOTAL_LENGTH);
            holder.col3 = (TextView)view.findViewById(R.id.COLUMN_ADJUSTED);

            view.setTag(holder);

        }
        else { holder = (ViewHolder)view.getTag(); }

        //set the background color of the row depending on whether it's highlighted or not
        if (pPosition == selectedPos) { view.setBackgroundColor(Color.parseColor("#0099FF")); }
        else { view.setBackgroundColor(Color.parseColor("#FFFFFF")); }

        //set the text of the columns
        holder.col1.setText(pipeNumbers.get(pPosition));
        holder.col2.setText(totalLengthValues.get(pPosition));
        holder.col3.setText(adjustedValues.get(pPosition));

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