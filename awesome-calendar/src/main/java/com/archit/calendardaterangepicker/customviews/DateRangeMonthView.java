package com.archit.calendardaterangepicker.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.archit.calendardaterangepicker.R;
import com.archit.calendardaterangepicker.models.CalendarStyleAttributes;
import com.archit.calendardaterangepicker.models.DayContainer;
import com.archit.calendardaterangepicker.timepicker.AwesomeTimePickerDialog;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by archit.shah on 08/09/2017.
 */

class DateRangeMonthView extends LinearLayout {

    private static final String LOG_TAG = DateRangeMonthView.class.getSimpleName();
    private LinearLayout llDaysContainer;
    private LinearLayout llTitleWeekContainer;

    private Calendar currentCalendarMonth;

    private CalendarStyleAttributes calendarStyleAttr;

    private DateRangeCalendarView.CalendarListener calendarListener;

    private DateRangeCalendarManager dateRangeCalendarManager;

    private final static PorterDuff.Mode FILTER_MODE = PorterDuff.Mode.SRC_IN;

    public void setCalendarListener(final DateRangeCalendarView.CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    public DateRangeMonthView(final Context context) {
        super(context);
        initView(context, null);
    }

    public DateRangeMonthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public DateRangeMonthView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DateRangeMonthView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    /**
     * To initialize child views
     *
     * @param context      - App context
     * @param attributeSet - Attr set
     */
    private void initView(final Context context, final AttributeSet attributeSet) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final LinearLayout mainView = (LinearLayout) layoutInflater.inflate(R.layout.layout_calendar_month, this, true);
        llDaysContainer = mainView.findViewById(R.id.llDaysContainer);
        llTitleWeekContainer = mainView.findViewById(R.id.llTitleWeekContainer);

        setListeners();
    }

    /**
     * To set listeners.
     */
    private void setListeners() {
    }

    private OnClickListener dayClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {

            if (calendarStyleAttr.isEditable()) {
                final int key = (int) view.getTag();
                final Calendar selectedCal = Calendar.getInstance();
                Date date = new Date();
                try {
                    date = DateRangeCalendarManager.SIMPLE_DATE_FORMAT.parse(String.valueOf(key));
                } catch (final ParseException e) {
                    e.printStackTrace();
                }
                selectedCal.setTime(date);

                Calendar minSelectedDate = dateRangeCalendarManager.getMinSelectedDate();
                Calendar maxSelectedDate = dateRangeCalendarManager.getMaxSelectedDate();

                if (minSelectedDate != null && maxSelectedDate == null) {
                    maxSelectedDate = selectedCal;

                    final int startDateKey = DayContainer.GetContainerKey(minSelectedDate);
                    final int lastDateKey = DayContainer.GetContainerKey(maxSelectedDate);

                    if (startDateKey == lastDateKey) {
                        minSelectedDate = maxSelectedDate;
                    } else if (startDateKey > lastDateKey) {
                        final Calendar temp = (Calendar) minSelectedDate.clone();
                        minSelectedDate = maxSelectedDate;
                        maxSelectedDate = temp;
                    }
                } else if (maxSelectedDate == null) {
                    //This will call one time only
                    minSelectedDate = selectedCal;
                } else {
                    minSelectedDate = selectedCal;
                    maxSelectedDate = null;
                }

                dateRangeCalendarManager.setMinSelectedDate(minSelectedDate);
                dateRangeCalendarManager.setMaxSelectedDate(maxSelectedDate);
                drawCalendarForMonth(currentCalendarMonth);

                if (calendarStyleAttr.isShouldEnabledTime()) {
                    final Calendar finalMinSelectedDate = minSelectedDate;
                    final Calendar finalMaxSelectedDate = maxSelectedDate;
                    final AwesomeTimePickerDialog awesomeTimePickerDialog = new AwesomeTimePickerDialog(getContext(), getContext().getString(R.string.select_time), new AwesomeTimePickerDialog.TimePickerCallback() {
                        @Override
                        public void onTimeSelected(final int hours, final int mins) {
                            selectedCal.set(Calendar.HOUR, hours);
                            selectedCal.set(Calendar.MINUTE, mins);

                            Log.i(LOG_TAG, "Time: " + selectedCal.getTime().toString());
                            if (calendarListener != null) {

                                if (finalMaxSelectedDate != null) {
                                    calendarListener.onDateRangeSelected(finalMinSelectedDate, finalMaxSelectedDate);
                                } else {
                                    calendarListener.onFirstDateSelected(finalMinSelectedDate);
                                }
                            }
                        }

                        @Override
                        public void onCancel() {
                            DateRangeMonthView.this.resetAllSelectedViews();
                        }
                    });
                    awesomeTimePickerDialog.showDialog();
                } else {
                    Log.i(LOG_TAG, "Time: " + selectedCal.getTime().toString());
                    if (maxSelectedDate != null) {
                        calendarListener.onDateRangeSelected(minSelectedDate, maxSelectedDate);
                    } else {
                        calendarListener.onFirstDateSelected(minSelectedDate);
                    }
                }
            }
        }
    };

    /**
     * To draw calendar for the given month. Here calendar object should start from date of 1st.
     *
     * @param calendarStyleAttr        Calendar style attributes
     * @param month                    Month to be drawn
     * @param dateRangeCalendarManager Calendar data manager
     */
    public void drawCalendarForMonth(final CalendarStyleAttributes calendarStyleAttr, final Calendar month, final DateRangeCalendarManager dateRangeCalendarManager) {
        this.calendarStyleAttr = calendarStyleAttr;
        this.currentCalendarMonth = (Calendar) month.clone();
        this.dateRangeCalendarManager = dateRangeCalendarManager;
        setConfigs();
        setWeekTitleColor(calendarStyleAttr.getWeekColor());
        drawCalendarForMonth(currentCalendarMonth);
    }

    /**
     * To draw calendar for the given month. Here calendar object should start from date of 1st.
     *
     * @param month Calendar month
     */
    private void drawCalendarForMonth(final Calendar month) {

        currentCalendarMonth = (Calendar) month.clone();
        currentCalendarMonth.set(Calendar.DATE, 1);
        currentCalendarMonth.set(Calendar.HOUR, 0);
        currentCalendarMonth.set(Calendar.MINUTE, 0);
        currentCalendarMonth.set(Calendar.SECOND, 0);

        final String[] weekTitle = getContext().getResources().getStringArray(R.array.week_sun_sat);

        //To set week day title as per offset
        for (int i = 0; i < 7; i++) {
            final CustomTextView textView = (CustomTextView) llTitleWeekContainer.getChildAt(i);
            final String weekStr = weekTitle[(i + calendarStyleAttr.getWeekOffset()) % 7];
            textView.setText(weekStr);
        }

        int startDay = month.get(Calendar.DAY_OF_WEEK) - calendarStyleAttr.getWeekOffset();

        //To ratate week day according to offset
        if (startDay < 1) {
            startDay = startDay + 7;
        }
        month.add(Calendar.DATE, -startDay + 1);

        for (int i = 0; i < llDaysContainer.getChildCount(); i++) {
            final LinearLayout weekRow = (LinearLayout) llDaysContainer.getChildAt(i);
            for (int j = 0; j < 7; j++) {
                final RelativeLayout rlDayContainer = (RelativeLayout) weekRow.getChildAt(j);
                final DayContainer container = new DayContainer(rlDayContainer);
                container.tvDate.setText(String.valueOf(month.get(Calendar.DATE)));
                if (calendarStyleAttr.getFonts() != null) {
                    container.tvDate.setTypeface(calendarStyleAttr.getFonts());
                }
                drawDayContainer(container, month);
                month.add(Calendar.DATE, 1);
            }
        }
    }

    /**
     * To draw specific date container according to past date, today, selected or from range.
     *
     * @param container - Date container
     * @param calendar  - Calendar obj of specific date of the month.
     */
    private void drawDayContainer(final DayContainer container, final Calendar calendar) {

        final Calendar today = Calendar.getInstance();

        final int date = calendar.get(Calendar.DATE);

        if (currentCalendarMonth.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
            hideDayContainer(container);
        } else if (today.after(calendar) && (today.get(Calendar.DAY_OF_YEAR) != calendar.get(Calendar.DAY_OF_YEAR))
                && !calendarStyleAttr.isEnabledPastDates()) {
            disableDayContainer(container);
            container.tvDate.setText(String.valueOf(date));
        } else {
            @DateRangeCalendarManager.RANGE_TYPE final int type = dateRangeCalendarManager.checkDateRange(calendar);
            if (type == DateRangeCalendarManager.RANGE_TYPE.START_DATE || type == DateRangeCalendarManager.RANGE_TYPE.LAST_DATE) {
                makeAsSelectedDate(container, type);
            } else if (type == DateRangeCalendarManager.RANGE_TYPE.MIDDLE_DATE) {
                makeAsRangeDate(container);
            } else {
                enabledDayContainer(container);
            }

            container.tvDate.setText(String.valueOf(date));
            container.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarStyleAttr.getTextSizeDate());
        }

        container.rootView.setTag(DayContainer.GetContainerKey(calendar));
    }

    /**
     * To hide date if date is from previous month.
     *
     * @param container - Container
     */
    private void hideDayContainer(final DayContainer container) {
        container.tvDate.setText("");
        container.tvDate.setBackgroundColor(Color.TRANSPARENT);
        container.strip.setBackgroundColor(Color.TRANSPARENT);
        container.rootView.setBackgroundColor(Color.TRANSPARENT);
        container.rootView.setVisibility(INVISIBLE);
        container.rootView.setOnClickListener(null);
    }

    /**
     * To disable past date. Click listener will be removed.
     *
     * @param container - Container
     */
    private void disableDayContainer(final DayContainer container) {
        container.tvDate.setBackgroundColor(Color.TRANSPARENT);
        container.strip.setBackgroundColor(Color.TRANSPARENT);
        container.rootView.setBackgroundColor(Color.TRANSPARENT);
        container.tvDate.setTextColor(calendarStyleAttr.getDisableDateColor());
        container.rootView.setVisibility(VISIBLE);
        container.rootView.setOnClickListener(null);
    }

    /**
     * To enable date by enabling click listeners.
     *
     * @param container - Container
     */
    private void enabledDayContainer(final DayContainer container) {
        container.tvDate.setBackgroundColor(Color.TRANSPARENT);
        container.strip.setBackgroundColor(Color.TRANSPARENT);
        container.rootView.setBackgroundColor(Color.TRANSPARENT);
        container.tvDate.setTextColor(calendarStyleAttr.getDefaultDateColor());
        container.rootView.setVisibility(VISIBLE);
        container.rootView.setOnClickListener(dayClickListener);
    }

    /**
     * To draw date container as selected as end selection or middle selection.
     *
     * @param container - Container
     * @param stripType - Right end date, Left end date or middle
     */
    private void makeAsSelectedDate(final DayContainer container, @DateRangeCalendarManager.RANGE_TYPE final int stripType) {
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.strip.getLayoutParams();

        final Calendar minDate = dateRangeCalendarManager.getMinSelectedDate();
        final Calendar maxDate = dateRangeCalendarManager.getMaxSelectedDate();

        if (stripType == DateRangeCalendarManager.RANGE_TYPE.START_DATE && maxDate != null &&
                minDate.compareTo(maxDate) != 0) {
            final Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.range_bg_left);
            mDrawable.setColorFilter(new PorterDuffColorFilter(calendarStyleAttr.getRangeStripColor(), FILTER_MODE));

            container.strip.setBackground(mDrawable);
            layoutParams.setMargins(20, 0, 0, 0);
        } else if (stripType == DateRangeCalendarManager.RANGE_TYPE.LAST_DATE) {
            final Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.range_bg_right);
            mDrawable.setColorFilter(new PorterDuffColorFilter(calendarStyleAttr.getRangeStripColor(), FILTER_MODE));
            container.strip.setBackground(mDrawable);
            layoutParams.setMargins(0, 0, 20, 0);
        } else {
            container.strip.setBackgroundColor(Color.TRANSPARENT);
            layoutParams.setMargins(0, 0, 0, 0);
        }
        container.strip.setLayoutParams(layoutParams);
        final Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.green_circle);
        mDrawable.setColorFilter(new PorterDuffColorFilter(calendarStyleAttr.getSelectedDateCircleColor(), FILTER_MODE));
        container.tvDate.setBackground(mDrawable);
        container.rootView.setBackgroundColor(Color.TRANSPARENT);
        container.tvDate.setTextColor(calendarStyleAttr.getSelectedDateColor());
        container.rootView.setVisibility(VISIBLE);
        container.rootView.setOnClickListener(dayClickListener);
    }

    /**
     * To draw date as middle date
     *
     * @param container - Container
     */
    private void makeAsRangeDate(final DayContainer container) {
        container.tvDate.setBackgroundColor(Color.TRANSPARENT);
        final Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.range_bg);
        mDrawable.setColorFilter(new PorterDuffColorFilter(calendarStyleAttr.getRangeStripColor(), FILTER_MODE));
        container.strip.setBackground(mDrawable);
        container.rootView.setBackgroundColor(Color.TRANSPARENT);
        container.tvDate.setTextColor(calendarStyleAttr.getRangeDateColor());
        container.rootView.setVisibility(VISIBLE);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.strip.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        container.strip.setLayoutParams(layoutParams);
        container.rootView.setOnClickListener(dayClickListener);
    }


    /**
     * To remove all selection and redraw current calendar
     */
    public void resetAllSelectedViews() {

        dateRangeCalendarManager.setMinSelectedDate(null);
        dateRangeCalendarManager.setMaxSelectedDate(null);

        drawCalendarForMonth(currentCalendarMonth);

    }


    /**
     * To set week title color
     *
     * @param color - resource color value
     */
    public void setWeekTitleColor(@ColorInt final int color) {
        for (int i = 0; i < llTitleWeekContainer.getChildCount(); i++) {
            final CustomTextView textView = (CustomTextView) llTitleWeekContainer.getChildAt(i);
            textView.setTextColor(color);
        }
    }

    /**
     * To apply configs to all the text views
     */
    private void setConfigs() {
        drawCalendarForMonth(currentCalendarMonth);
        for (int i = 0; i < llTitleWeekContainer.getChildCount(); i++) {
            final CustomTextView textView = (CustomTextView) llTitleWeekContainer.getChildAt(i);
            textView.setTypeface(calendarStyleAttr.getFonts());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarStyleAttr.getTextSizeWeek());
        }
    }
}
