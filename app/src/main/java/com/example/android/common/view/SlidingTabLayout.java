/*
 * Copyright 2014 Chris Banes
 * Copyright 2015 Christophe Beyls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.common.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.republica.R;

import java.util.Locale;

/**
 */
public class SlidingTabLayout extends HorizontalScrollView {

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 14;
    private final SlidingTabStrip mTabStrip;
    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private boolean mDistributeEvenly;

    private ColorStateList mTextColor;
    private int mItemBackground;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private TabListener mTabListener;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(themifyContext(context, attrs, defStyle), attrs, defStyle);
        // Ensure we use the wrapped Context
        context = getContext();

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        TypedValue outValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

        if (a.hasValue(R.styleable.SlidingTabLayout_indicatorHeight)) {
            mTabStrip.setSelectedIndicatorHeight(
                    a.getDimensionPixelSize(R.styleable.SlidingTabLayout_indicatorHeight, 0));
        }

        if (a.hasValue(R.styleable.SlidingTabLayout_indicatorColor)) {
            mTabStrip.setSelectedIndicatorColor(
                    a.getColor(R.styleable.SlidingTabLayout_indicatorColor, 0));
        }

        if (a.hasValue(R.styleable.SlidingTabLayout_textColor)) {
            setTextColor(a.getColorStateList(R.styleable.SlidingTabLayout_textColor));
        } else {
            context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
            setTextColor(outValue.data);
        }

        mTitleOffset = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_contentInsetStart,
                mTitleOffset);

        mTabStrip.setPadding(mTitleOffset, 0, 0, 0);

        mDistributeEvenly = a.getBoolean(R.styleable.SlidingTabLayout_distributeEvenly, false);

        a.recycle();

        // Use the Support Library Theme's
        // selectableItemBackground to ensure that the View has a pressed state
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        mItemBackground = outValue.resourceId;
    }

    /**
     * Allows us to emulate the {@code android:theme} attribute for devices before L.
     */
    private static Context themifyContext(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar,
                defStyleAttr, 0);
        final int themeId = a.getResourceId(R.styleable.Toolbar_theme, 0);
        if (themeId != 0) {
            context = new ContextThemeWrapper(context, themeId);
        }
        a.recycle();
        return context;
    }

    public void setDistributeEvenly(boolean distributeEvenly) {
        mDistributeEvenly = distributeEvenly;
    }

    /**
     * Sets the color to be used for indicating the selected tab.
     * This will override the style color.
     */
    public void setSelectedIndicatorColor(int color) {
        mTabStrip.setSelectedIndicatorColor(color);
    }

    public void setTextColor(ColorStateList color) {
        mTextColor = color;
    }

    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    public void setTabListener(TabListener tabListener) {
        mTabListener = tabListener;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabStrip.removeAllViews();
        if (mViewPager != null) {
            populateTabStrip();
        }
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(mTextColor);

        if (mItemBackground != 0) {
            textView.setBackgroundResource(mItemBackground);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return textView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final View.OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView;
            TextView tabTitleView;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                        false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
                if (tabTitleView == null) {
                    tabTitleView = (TextView) tabView;
                }
            } else {
                tabTitleView = createDefaultTabView(getContext());
                tabView = tabTitleView;
            }

            if (mDistributeEvenly) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                lp.width = 0;
                lp.weight = 1;
            }

            CharSequence tabText = adapter.getPageTitle(i);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // Emulate allCaps
                tabText = tabText.toString().toUpperCase(Locale.getDefault());
            }
            tabTitleView.setText(tabText);
            tabView.setFocusable(true);
            tabView.setOnClickListener(tabClickListener);

            mTabStrip.addView(tabView);
            if (i == mViewPager.getCurrentItem()) {
                tabView.setSelected(true);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, float positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() +
                    Math.round(positionOffset * selectedChild.getWidth()) - mTitleOffset;

            scrollTo(targetScrollX, 0);
        }
    }

    public interface TabListener {

        void onTabSelected(int pos);

        void onTabReSelected(int pos);

    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);


            scrollToTab(position, positionOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(position == i);
            }
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }
    }

    private class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    final int previousPos = mViewPager.getCurrentItem();
                    mViewPager.setCurrentItem(i);

                    if (mTabListener != null) {
                        if (previousPos != i) {
                            mTabListener.onTabSelected(i);
                        } else {
                            mTabListener.onTabReSelected(i);
                        }
                    }

                    return;
                }
            }
        }
    }
}