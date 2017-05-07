/*******************************************************************************
 * Copyright 2013 Gabriele Mariotti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fsck.k9.preferences.color_preference;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fsck.k9.K9.Theme;
import com.fsck.k9.R;
import com.fsck.k9.helper.Utility;

/**
 * ColorPreference based on ColorPickerDialog of Stock Calendar
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class ColorPickerPreference extends Preference {

    private int[] mColorChoices = {};
    private String[][] mColorNames = {};
    private String[] colorNameValues = {};
    private String[] colorNameEntries = {};
    private int mValue = 0;
    private int mItemLayoutId = R.layout.preference_grid_item_color;
    private int mNumColumns = 5;
    private View mPreviewView;

    public ColorPickerPreference(Context context) {
        super(context);
        initAttrs(null, 0);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ColorPickerPreference, defStyle, defStyle);

        colorNameValues = getContext().getResources().getStringArray(R.array.settings_theme_color_values);
        colorNameEntries = getContext().getResources().getStringArray(R.array.settings_theme_color_entries);

        try {
            mItemLayoutId = a.getResourceId(R.styleable.ColorPickerPreference_cal_itemLayout, mItemLayoutId);
            mNumColumns = a.getInteger(R.styleable.ColorPickerPreference_cal_numColumns, mNumColumns);
            int choicesResId = a.getResourceId(R.styleable.ColorPickerPreference_cal_choices,
                    R.array.default_color_choice_values);
            if (choicesResId > 0) {
                String[] choices = a.getResources().getStringArray(choicesResId);
                mColorChoices = new int[choices.length];
                mColorNames = new String[choices.length][2];
                for (int i = 0; i < choices.length; i++) {
                    mColorChoices[i] = Color.parseColor(choices[i]);
                    mColorNames[i][0] = getThemeColorName(Theme.values()[i].name);
                    mColorNames[i][1] = Theme.values()[i].name;
                }
            }

        } finally {
            a.recycle();
        }
        setWidgetLayoutResource(mItemLayoutId);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mPreviewView = view.findViewById(R.id.calendar_color_view);
        setColorViewValue(mPreviewView, mValue);
    }

    public void setValue(int value) {
        if (callChangeListener(value)) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();

        ColorPickerDialog colordialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColorChoices, getValue(), mNumColumns, Utility.isTablet(getContext()) ? ColorPickerDialog.SIZE_LARGE :
                        ColorPickerDialog.SIZE_SMALL);

        Activity activity = (Activity) getContext();
        activity.getFragmentManager().beginTransaction()
                .add(colordialog, getFragmentTag())
                .commit();

        colordialog.setOnColorSelectedListener(listener);
    }

    /**
     * Implement listener to get selected color value
     */
    ColorPickerSwatch.OnColorSelectedListener listener = new ColorPickerSwatch.OnColorSelectedListener() {

        @Override
        public void onColorSelected(int color) {
            setValue(color);
            setSummary(getColorSummaryName());
        }
    };

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        Activity activity = (Activity) getContext();
        ColorPickerDialog colordialog = (ColorPickerDialog) activity
                .getFragmentManager().findFragmentByTag(getFragmentTag());
        if (colordialog != null) {
            // re-bind listener to fragment
            colordialog.setOnColorSelectedListener(listener);
        }
        setSummary(getColorSummaryName());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(0) : (Integer) defaultValue);
    }

    private String getThemeColorName(String colorName) {
        if (colorNameValues.length == colorNameEntries.length) {
            for (int i = 0; i < colorNameValues.length; i++) {
                if (colorNameValues[i].equals(colorName)) {
                    return colorNameEntries[i];
                }
            }
        }
        return colorName;
    }

    private static void setColorViewValue(View view, int color) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Resources res = imageView.getContext().getResources();

            Drawable currentDrawable = imageView.getDrawable();
            GradientDrawable colorChoiceDrawable;
            if (currentDrawable != null && currentDrawable instanceof GradientDrawable) {
                // Reuse drawable
                colorChoiceDrawable = (GradientDrawable) currentDrawable;
            } else {
                colorChoiceDrawable = new GradientDrawable();
                colorChoiceDrawable.setShape(GradientDrawable.OVAL);
            }

            // Set stroke to dark version of color
            int darkenedColor = Color.rgb(
                    Color.red(color) * 192 / 256,
                    Color.green(color) * 192 / 256,
                    Color.blue(color) * 192 / 256);

            colorChoiceDrawable.setColor(color);
            colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);
            imageView.setImageDrawable(colorChoiceDrawable);

        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    public String getFragmentTag() {
        return "color_" + getKey();
    }

    public int getValue() {
        return mValue;
    }

    /**
     * Returns the name from strings.xml
     */
    public String getColorSummaryName() {
        for (int i = 0; i < mColorChoices.length; i++) {
            if (mValue == mColorChoices[i]) {
                return mColorNames[i][0];
            }
        }
        return "";
    }

    /**
     * Returns the internal name from K9.Theme
     */
    public String getColorName() {
        for (int i = 0; i < mColorChoices.length; i++) {
            if (mValue == mColorChoices[i]) {
                return mColorNames[i][1];
            }
        }
        return "";
    }
}
