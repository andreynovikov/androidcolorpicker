package info.andreynovikov.androidcolorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.util.Arrays;

public class ColorPreference extends Preference {
    private static final int[] DEFAULT_COLORS = {
            Color.BLACK,
            Color.WHITE,
            Color.BLUE,
            Color.CYAN,
            Color.DKGRAY,
            Color.GRAY,
            Color.LTGRAY,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.YELLOW
    };
    private final int[] mColors;
    @ColorInt int mDefaultColor;

    public ColorPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ColorPreference, defStyleAttr, defStyleRes);

        mDefaultColor = a.getInt(R.styleable.ColorPreference_android_defaultValue, Color.BLACK);
        int resId = a.getResourceId(R.styleable.ColorPreference_android_entryValues, 0);
        if (resId != 0)
            mColors = context.getResources().getIntArray(resId);
        else
            mColors = DEFAULT_COLORS;

        a.recycle();

        setWidgetLayoutResource(R.layout.pref_color_layout);
    }

    public ColorPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public ColorPreference(@NonNull Context context) {
        this(context, null);
    }

    private int mColor;

    /**
     * Saves the color to the current data storage.
     *
     * @param color The color to save
     */
    public void setColor(int color) {
        final boolean wasBlocking = shouldDisableDependents();

        mColor = color;

        persistInt(color);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    /**
     * Gets the color from the current data storage.
     *
     * @return The current preference value
     */
    public int getColor() {
        return mColor;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ColorPickerSwatch colorSwatch = (ColorPickerSwatch) holder.findViewById(R.id.color_swatch);
        if (colorSwatch != null)
            colorSwatch.setColor(mColor);
    }

    @Override
    protected void onClick() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            ColorPickerDialog dialog = new ColorPickerDialog();
            int[] colors = mColors;
            if (missing(colors, mDefaultColor)) {
                colors = Arrays.copyOf(colors, colors.length + 1);
                colors[colors.length - 1] = mDefaultColor;
            }
            if (missing(colors, mColor)) {
                colors = Arrays.copyOf(colors, colors.length + 1);
                colors[colors.length - 1] = mColor;
            }
            dialog.setColors(colors, mColor);
            dialog.setArguments(R.string.color_picker_default_title, 4, ColorPickerDialog.SIZE_SMALL);
            dialog.setOnColorSelectedListener(this::setColor);
            dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ColorPreferenceColorPickerDialog");
        }
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, Color.BLACK);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = mDefaultColor;
        }
        setColor(getPersistedInt((Integer) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final ColorPreference.SavedState myState = new ColorPreference.SavedState(superState);
        myState.mColor = getColor();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(ColorPreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        ColorPreference.SavedState myState = (ColorPreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setColor(myState.mColor);
    }

    private boolean missing(final int[] array, final int key) {
        for (final int i : array) {
            if (i == key) {
                return false;
            }
        }
        return true;
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        int mColor;

        SavedState(Parcel source) {
            super(source);
            mColor = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mColor);
        }
    }
}
