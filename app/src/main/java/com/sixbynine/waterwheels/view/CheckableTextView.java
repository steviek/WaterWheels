package com.sixbynine.waterwheels.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sixbynine.waterwheels.R;


/**
 * Created by stevenkideckel on 14-12-31.
 */
@SuppressWarnings("ResourceType")
public class CheckableTextView extends RelativeLayout implements Checkable, View.OnClickListener {
    HeaderSubheaderView mHeaderSubheaderView;
    TextView mOneLineTextView;
    CheckBox mCheckbox;

    private int mNumLines;
    private String mText;
    private String mSubtext;
    private boolean mChecked;

    private static final int[] ATTRS = new int[]{
            android.R.attr.text,
            android.R.attr.checked,
            R.attr.hsvHeaderText,
            R.attr.hsvSubHeaderText
    };

    public CheckableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_checkable_textview, this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CheckableTextView,
                0, 0);

        mNumLines = a.getInt(R.styleable.CheckableTextView_ctvLines, 1);

        a.recycle();

        a = context.obtainStyledAttributes(attrs, ATTRS);

        mText = a.getString(0);
        mChecked = a.getBoolean(1, false);

        String text = a.getString(2);
        if (text != null) {
            mText = text;
        }
        mSubtext = a.getString(3);

        a.recycle();


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        setOnClickListener(this);
        //Roboguice doesn't work in edit mode
        mHeaderSubheaderView = (HeaderSubheaderView) findViewById(R.id.two_line_view);
        mOneLineTextView = (TextView) findViewById(R.id.one_line_view);
        mCheckbox = (CheckBox) findViewById(R.id.checkbox);

        mCheckbox.setEnabled(isEnabled());

        if (mNumLines == 1) {
            mHeaderSubheaderView.setVisibility(View.GONE);
            mOneLineTextView.setVisibility(View.VISIBLE);
            if (mText != null) {
                mOneLineTextView.setText(mText);
            } else if (isInEditMode()) {
                mOneLineTextView.setText("Your text here");
            }
        } else {
            mHeaderSubheaderView.setVisibility(View.VISIBLE);
            mOneLineTextView.setVisibility(View.GONE);
            if (mText != null) {
                mHeaderSubheaderView.setHeaderText(mText);
            }
            if (mSubtext != null) {
                mHeaderSubheaderView.setSubheaderText(mSubtext);
            }
        }

        mCheckbox.setChecked(mChecked);

    }

    /**
     * Register a callback to be invoked when the checked state of this button changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mCheckbox.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setChecked(boolean checked) {
        mCheckbox.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mCheckbox.isChecked();
    }

    @Override
    public void toggle() {
        mCheckbox.toggle();
    }

    @Override
    public void onClick(View v) {
        toggle();
    }
}