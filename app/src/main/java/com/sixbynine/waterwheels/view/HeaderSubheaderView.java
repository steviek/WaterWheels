package com.sixbynine.waterwheels.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sixbynine.waterwheels.R;

/**
 * Created by stevenkideckel on 14-12-31.
 */
public class HeaderSubheaderView extends LinearLayout {

  private TextView mTextView;
  private TextView mSubTextView;

  private CharSequence mHeaderText;
  private CharSequence mSubHeaderText;

  public HeaderSubheaderView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setOrientation(VERTICAL);
    setClickable(true);
    LayoutInflater.from(context).inflate(R.layout.view_header_subheader, this);

    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.HeaderSubheaderView,
        0, 0);

    try {
      mHeaderText = a.getString(R.styleable.HeaderSubheaderView_hsvHeaderText);
      mSubHeaderText = a.getString(R.styleable.HeaderSubheaderView_hsvSubHeaderText);
    } finally {
      a.recycle();
    }
  }

  public void setHeaderText(CharSequence cs) {
    mTextView.setText(cs);
  }

  public void setSubheaderText(CharSequence cs) {
    mSubTextView.setText(cs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTextView = (TextView) findViewById(R.id.header);
    mSubTextView = (TextView) findViewById(R.id.subheader);

    if (mHeaderText != null) {
      mTextView.setText(mHeaderText);
    } else if (isInEditMode()) {
      mTextView.setText("Header");
    }

    if (mSubHeaderText != null) {
      mSubTextView.setText(mSubHeaderText);
    } else if (isInEditMode()) {
      mSubTextView.setText("Subheader");
    }
  }
}
