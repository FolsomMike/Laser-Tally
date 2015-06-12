/******************************************************************************
 * Title: TextFitTextView.java
 * Author: Hunter Schoonover
 * Date: 06/12/15
 *
 * Purpose:
 *
 * This class is used in place of Android's normal TextView class when the
 * developer wants a TextView whose text is automatically resized to fit
 * inside.
 *
 * Original writer's description:
 *      Text view that auto adjusts text size to fit within the view.
 *      If the text size equals the minimum text size and still does not
 *      fit, append with an ellipsis.
 *
 * Original code from:
 *      "http://stackoverflow.com/questions/5033012/
 *              auto-scale-textview-text-to-fit-within-bounds/17782522#17782522"
 *
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.TextView;


//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TextFitTextView
//

public class TextFitTextView extends TextView {

    private interface SizeTester {
        /**
         *
         * @param suggestedSize
         *            Size of text to be tested
         * @param availableSpace
         *            available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         *         text, it takes less space than {@code availableSpace}, > 0
         *         otherwise
         */
        public int onTestSize(int suggestedSize, RectF availableSpace);
    }

    private RectF mTextRect = new RectF();

    private RectF mAvailableSpaceRect;

    private SparseIntArray mTextCachedSizes;

    private TextPaint mPaint;

    private float mMaxTextSize;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private float mMinTextSize = 6;

    private int mWidthLimit;

    private static final int NO_LINE_LIMIT = -1;
    private int mMaxLines;

    private boolean mEnableSizeCache = true;
    private boolean mInitiallized;

    //-----------------------------------------------------------------------------
    // TextFitTextView::TextFitTextView (constructor)
    //

    public TextFitTextView(Context pContext)
    {

        super(pContext);

        init();

    }//end of TextFitTextView::TextFitTextView (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::TextFitTextView (constructor)
    //

    public TextFitTextView(Context pContext, AttributeSet pAttrs)
    {

        super(pContext, pAttrs);

        init();

    }//end of TextFitTextView::TextFitTextView (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::TextFitTextView (constructor)
    //

    public TextFitTextView(Context pContext, AttributeSet pAttrs, int pDefStyle)
    {

        super(pContext, pAttrs, pDefStyle);

        init();

    }//end of TextFitTextView::TextFitTextView (constructor)
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::init
    //
    // Initializes the object.
    //
    // Must be called in the constructor to ensure that using an instance of this
    // class in xml will call this function.
    //

    private void init()
    {

        mPaint = new TextPaint(getPaint());
        mMaxTextSize = getTextSize();
        mAvailableSpaceRect = new RectF();
        mTextCachedSizes = new SparseIntArray();

        if (mMaxLines == 0) {
            // no value was assigned during construction
            mMaxLines = NO_LINE_LIMIT;
        }

        mInitiallized = true;

    }//end of TextFitTextView::init
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::getMaxLines
    //

    @Override
    public int getMaxLines()
    {

        return mMaxLines;

    }//end of TextFitTextView::getMaxLines
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::onSizeChanged
    //

    @Override
    protected void onSizeChanged(int pWidth, int pHeight, int pOldWidth, int pOldHeight)
    {

        super.onSizeChanged(pWidth, pHeight, pOldWidth, pOldHeight);

        mTextCachedSizes.clear();

        if (pWidth != pOldWidth || pHeight != pOldHeight) { reAdjust(); }

    }//end of TextFitTextView::onSizeChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::onTextChanged
    //

    @Override
    protected void onTextChanged(final CharSequence pText, final int pStart, final int pBefore,
                                 final int pAfter)
    {

        super.onTextChanged(pText, pStart, pBefore, pAfter);

        reAdjust();

    }//end of TextFitTextView::onTextChanged
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setLines
    //

    @Override
    public void setLines(int pLines)
    {

        super.setLines(pLines);

        mMaxLines = pLines;
        reAdjust();

    }//end of TextFitTextView::setLines
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setLineSpacing
    //

    @Override
    public void setLineSpacing(float pAdd, float pMult)
    {

        super.setLineSpacing(pAdd, pMult);

        mSpacingMult = pMult;
        mSpacingAdd = pAdd;

    }//end of TextFitTextView::setLineSpacing
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setMaxLines
    //

    @Override
    public void setMaxLines(int pMaxLines)
    {

        super.setMaxLines(pMaxLines);
        mMaxLines = pMaxLines;
        reAdjust();

    }//end of TextFitTextView::setMaxLines
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setSingleLine
    //

    @Override
    public void setSingleLine()
    {

        super.setSingleLine();

        mMaxLines = 1;
        reAdjust();

    }//end of TextFitTextView::setSingleLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setSingleLine
    //

    @Override
    public void setSingleLine(boolean pSingleLine)
    {

        super.setSingleLine(pSingleLine);

        if (pSingleLine) { mMaxLines = 1; }
        else { mMaxLines = NO_LINE_LIMIT; }
        reAdjust();

    }//end of TextFitTextView::setSingleLine
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setText
    //

    @Override
    public void setText(final CharSequence pText, BufferType pType)
    {

        super.setText(pText, pType);

        adjustTextSize(pText.toString());

    }//end of TextFitTextView::setText
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setTextSize
    //

    @Override
    public void setTextSize(float pSize)
    {

        mMaxTextSize = pSize;
        mTextCachedSizes.clear();
        adjustTextSize(getText().toString());

    }//end of TextFitTextView::setTextSize
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::setTextSize
    //

    @Override
    public void setTextSize(int pUnit, float pSize)
    {

        Context c = getContext();
        Resources r;

        if (c == null) { r = Resources.getSystem(); }
        else { r = c.getResources(); }

        mMaxTextSize = TypedValue.applyDimension(pUnit, pSize, r.getDisplayMetrics());
        mTextCachedSizes.clear();
        adjustTextSize(getText().toString());

    }//end of TextFitTextView::setTextSize
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::mSizeTester
    //
    // Not really a function.
    //

    private final SizeTester mSizeTester = new SizeTester()
    {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

        @Override
        public int onTestSize(int pSuggestedSize, RectF pAvailableSPace) {

            mPaint.setTextSize(pSuggestedSize);
            mPaint.setTypeface(getTypeface());
            String text = getText().toString();

            boolean singleLine = getMaxLines() == 1;
            if (singleLine) {

                mTextRect.bottom = mPaint.getFontSpacing();
                mTextRect.right = mPaint.measureText(text);

            }
            else {

                StaticLayout layout = new StaticLayout(text, mPaint, mWidthLimit,
                        Alignment.ALIGN_NORMAL, mSpacingMult,
                        mSpacingAdd, true);

                // return early if we have more lines
                if (getMaxLines() != NO_LINE_LIMIT && layout.getLineCount() > getMaxLines()) {
                    return 1;
                }

                mTextRect.bottom = layout.getHeight();

                int maxWidth = -1;
                for (int i = 0; i < layout.getLineCount(); i++) {
                    if (maxWidth < layout.getLineWidth(i)) {
                        maxWidth = (int) layout.getLineWidth(i);
                    }
                }

                mTextRect.right = maxWidth;

            }

            mTextRect.offsetTo(0, 0);
            // may be too small, don't worry we will find the best match
            if (pAvailableSPace.contains(mTextRect)) { return -1; }
            //too big
            else { return 1; }

        }

    };//end of TextFitTextView::mSizeTester
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::adjustTextSize
    //

    private void adjustTextSize(String pString)
    {

        if (!mInitiallized) { return; }

        int startSize = (int)mMinTextSize;

        mWidthLimit = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        int heightLimit = getTextHeight(pString, getPaint(), mWidthLimit, mMaxTextSize);

        mAvailableSpaceRect.right = mWidthLimit;
        mAvailableSpaceRect.bottom = heightLimit;

        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, efficientTextSizeSearch(startSize,
                (int) mMaxTextSize, mSizeTester, mAvailableSpaceRect));

    }//end of TextFitTextView::adjustTextSize
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::binarySearch
    //

    private static int binarySearch(int pStart, int pEnd, SizeTester pSizeTester,
                                    RectF pAvailableSpace)
    {

        int lastBest = pStart;
        int lo = pStart;
        int hi = pEnd - 1;
        int mid;

        while (lo <= hi) {

            mid = (lo + hi) >>> 1;
            int midValCmp = pSizeTester.onTestSize(mid, pAvailableSpace);
            if (midValCmp < 0) { lastBest = lo; lo = mid + 1; }
            else if (midValCmp > 0) { hi = mid - 1; lastBest = hi; }
            else { return mid; }

        }

        // make sure to return last best
        // this is what should always be returned
        return lastBest;

    }//end of TextFitTextView::binarySearch
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::efficientTextSizeSearch
    //

    private int efficientTextSizeSearch(int pStart, int pEnd, SizeTester pSizeTester,
                                        RectF pAvailableSpace)
    {

        if (!mEnableSizeCache) { return binarySearch(pStart, pEnd, pSizeTester, pAvailableSpace); }

        String text = getText().toString();
        int key = text == null ? 0 : text.length();

        int size = mTextCachedSizes.get(key);
        if (size != 0) { return size; }

        size = binarySearch(pStart, pEnd, pSizeTester, pAvailableSpace);

        mTextCachedSizes.put(key, size);

        return size;

    }//end of TextFitTextView::efficientTextSizeSearch
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::getTextHeight
    //
    // Sets the text size of the text paint object and uses a static layout to
    // render text off screen before measuring.
    //

    private int getTextHeight(CharSequence pSource, TextPaint pPaint, int pWidth, float pTextSize)
    {
        // Update the text paint object
        pPaint.setTextSize(pTextSize);

        // Measure using a static layout
        StaticLayout layout = new StaticLayout(pSource, pPaint, pWidth, Alignment.ALIGN_NORMAL,
                mSpacingMult, mSpacingAdd, true);

        return layout.getHeight();

    }//end of TextFitTextView::getTextHeight
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // TextFitTextView::reAdjust
    //

    private void reAdjust()
    {

        adjustTextSize(getText().toString());

    }//end of TextFitTextView::reAdjust
    //-----------------------------------------------------------------------------

}//end of class TextFitTextView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------