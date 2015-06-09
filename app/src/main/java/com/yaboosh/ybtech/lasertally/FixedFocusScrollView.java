/******************************************************************************
* Title: FixedFocusScrollView.java
* Author: Hunter Schoonover
* Create Date: 06/09/15
* Last Edit:
*
* Purpose:
*
* This class is used in place of ScrollView when the developer wants full
* control of the focusing of views.
*
* The standard ScrollView often steals focus from and changes focus of
* descendant view. This class prevents that from happening.
*
* Example of use in xml:
* <com.yaboosh.ybtech.lasertally.FixedFocusScrollView
*       android:layout_height="fill_parent"
*       android:layout_width="fill_parent" >
* </com.yaboosh.ybtech.lasertally.FixedFocusScrollView>
*
*/

//-----------------------------------------------------------------------------


package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//class FixedFocusScrollView
//

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class FixedFocusScrollView extends ScrollView {

    public FixedFocusScrollView(Context context) {
        super(context);
    }

    public FixedFocusScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedFocusScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

}//end of class FixedFocusScrollView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
