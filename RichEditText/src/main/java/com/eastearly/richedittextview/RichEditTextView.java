package com.eastearly.richedittextview;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by dachen on 9/28/15.
 */
public class RichEditTextView extends LinearLayout implements View.OnClickListener{

    private final float initialAlpha = 0.7f;
    private Context _context;
    private String textContent;
    private int textColor;
    private EditText mMessageContentView;
    private LinearLayout mHtmloptions;
    private ImageButton mImageButton;
    private SpannableStringBuilder mSS;
    private boolean mToolbarClosed = false;
    private final static String Tag = "RichEditTextView";

    public RichEditTextView(Context context) {
        super(context);
    }

    public RichEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.RichEditTextView);
        textColor = (a.getColor(R.styleable.RichEditTextView_text_color,Color.BLACK));
        textContent = a.getString(R.styleable.RichEditTextView_text);
        a.recycle();
        initView();
    }

    public RichEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }


    private void initView(){
        View view = inflate(getContext(), R.layout.richedittext, null);
        mMessageContentView = (EditText)view.findViewById(R.id.body_text);
        addView(view);
        findViewById(R.id.makeBold).setOnClickListener(this);
        findViewById(R.id.makeItalic).setOnClickListener(this);
        findViewById(R.id.makeUnderline).setOnClickListener(this);
        //findViewById(R.id.makeBackground).setOnClickListener(this);
        findViewById(R.id.makeForeground).setOnClickListener(this);
        findViewById(R.id.makeHyperlink).setOnClickListener(this);
        mMessageContentView.setOnClickListener(this);
        mMessageContentView.setTextColor(textColor);
        mMessageContentView.setText(textContent);
        mHtmloptions = (LinearLayout)findViewById(R.id.rich_toolbar);
        mImageButton = (ImageButton)findViewById(R.id.list_toggle);
        mImageButton.setOnClickListener(this);
        setOnClickListener(this);


    }
    @Override
    public void onClick(View view) {

        if (mSS == null) {
            mSS = new SpannableStringBuilder(mMessageContentView.getText());
        } else {
            mSS = new SpannableStringBuilder(mMessageContentView.getText());
        }
        //refresh tool bar status
        getHtmloptionToolButton();

        final int start = mMessageContentView.getSelectionStart();
        final int end = mMessageContentView.getSelectionEnd();

        int viewId = view.getId();
        if (viewId == R.id.body_text) {
            this.refreshHtmloptionBar();
        }

        CharacterStyle span = null;

        if (viewId == R.id.makeBold) {
            if (toggleImageView((ImageView) view))
                span = new StyleSpan(Typeface.BOLD);
            else
                disableStyleSpan(start, end, Typeface.BOLD);

        } else if (viewId == R.id.makeItalic) {
            if (toggleImageView((ImageView) view))
                span = new StyleSpan(Typeface.ITALIC);
            else
                disableStyleSpan(start, end, Typeface.ITALIC);

        } else if (viewId == R.id.makeUnderline) {
            if (toggleImageView((ImageView) view))
                span = new UnderlineSpan();
            else
                disableSpan(start, end, UnderlineSpan.class);

        } else if (viewId == R.id.makeForeground && start!=end) {
            new ColorPickerDialog(_context, new ColorPickerDialog.OnColorChangedListener() {
                @Override
                public void colorChanged(int color) {
                    mSS.setSpan(new ForegroundColorSpan(color),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mMessageContentView.setText(mSS, TextView.BufferType.SPANNABLE);
                    //((TextView)MessageCompose.this.findViewById(R.id.makeForeground)).setTextColor(color);
                }

            }, Color.BLACK).show();

        } else if (viewId == R.id.makeHyperlink && start!= end) {

            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            final EditText urlText = new EditText(_context);
            urlText.setText("http://www.");
            builder.setView(urlText)
                    .setTitle(getResources().getString(R.string.url_entry_title))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (urlText.getText() != null) {
                                String url = urlText.getText().toString();
                                mSS.setSpan(new URLSpan(url),
                                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mMessageContentView.setText(mSS, TextView.BufferType.SPANNABLE);
                            }

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();

        }
        else if( viewId == R.id.list_toggle){
            if(!mToolbarClosed){
                mToolbarClosed = !mToolbarClosed;
                mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(mHtmloptions, "translationX", mHtmloptions.getMeasuredWidth()),
                        ObjectAnimator.ofFloat(mHtmloptions, "alpha", 1, 0)
                );
                set.start();



            }

            else
            {
                mToolbarClosed = !mToolbarClosed;
                mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_right_black_24dp));
                ObjectAnimator objectAnimator = new ObjectAnimator();
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(mHtmloptions, "translationX", 0),
                        ObjectAnimator.ofFloat(mHtmloptions, "alpha", 0, 1)
                );
                set.start();
            }
        }
        if (span != null) {

            if (start == end)
                mSS.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            else
                mSS.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mMessageContentView.setText(mSS, TextView.BufferType.SPANNABLE);
            mMessageContentView.setSelection(end);
        }
    }
    private void refreshHtmloptionBar() {
        int start = mMessageContentView.getSelectionStart();
        int end = mMessageContentView.getSelectionEnd();
        if (start == end && end == 0) return;
        letImageViewOff((ImageView) findViewById(R.id.makeBold));
        letImageViewOff((ImageView) findViewById(R.id.makeItalic));
        letImageViewOff((ImageView) findViewById(R.id.makeUnderline));
        Object[] spans = mSS.getSpans(start, end, Object.class);
        for (Object span : spans) {
            ImageView iv = getHtmloptionToolButton(span);
            if (iv != null)
                this.letImageViewOn(iv);
        }
    }
    private boolean imageViewOff(ImageView iv) {

        return (iv.getAlpha() - initialAlpha < 0.01);
    }

    private void letImageViewOn(ImageView iv) {
        iv.setAlpha(1.0f);

    }

    private void letImageViewOff(ImageView iv) {
        iv.setAlpha(initialAlpha);

    }
    private void getHtmloptionToolButton() {
        int start = mMessageContentView.getSelectionStart();
        int end = mMessageContentView.getSelectionEnd();
        if (start == end && end == 0) return;
        letImageViewOff((ImageView) findViewById(R.id.makeBold));
        letImageViewOff((ImageView) findViewById(R.id.makeItalic));
        letImageViewOff((ImageView) findViewById(R.id.makeUnderline));
        Object[] spans = mSS.getSpans(start, end, Object.class);
        for (Object span : spans) {
            ImageView iv = getHtmloptionToolButton(span);
            if (iv != null)
                this.letImageViewOn(iv);
        }
    }
    private ImageView getHtmloptionToolButton(Object span) {
        if (span instanceof StyleSpan) {
            switch (((StyleSpan) span).getStyle()) {
                case Typeface.BOLD:
                    return (ImageView) findViewById(R.id.makeBold);
                case Typeface.ITALIC:
                    return (ImageView) findViewById(R.id.makeItalic);
                default:
                    return null;
            }
        } else if (span instanceof UnderlineSpan) {
            return (ImageView) findViewById(R.id.makeUnderline);
        }
        return null;
    }
    private boolean toggleImageView(ImageView iv) {
        if (imageViewOff(iv)) {
            letImageViewOn(iv);
            return true;
        } else {
            letImageViewOff(iv);
            return false;
        }
    }
    private void disableStyleSpan(int start, int end, int typeFace) {
        StyleSpan[] spans = mSS.getSpans(start, end, StyleSpan.class);
        for (int i = spans.length - 1; i >= 0; i--)
            if (spans[i].getStyle() == typeFace) {
                if (mSS.getSpanStart(spans[i]) <= start)
                    mSS.setSpan(spans[i], mSS.getSpanStart(spans[i]), start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (mSS.getSpanEnd(spans[i]) > start) {

                    mSS.setSpan(spans[i], start, mSS.getSpanEnd(spans[i]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //mSS.removeSpan(spans[i]);
            }
        mMessageContentView.setText(mSS, TextView.BufferType.SPANNABLE);
        mMessageContentView.setSelection(end);
    }

    private void disableSpan(int start, int end, Class<? extends CharacterStyle> clz) {
        CharacterStyle[] spans = mSS.getSpans(start, end, clz);
        for (int i = spans.length - 1; i >= 0; i--)

        {
            if (mSS.getSpanStart(spans[i]) <= start)
                mSS.setSpan(spans[i], mSS.getSpanStart(spans[i]), start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (mSS.getSpanEnd(spans[i]) > start) {

                mSS.setSpan(spans[i], start, mSS.getSpanEnd(spans[i]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //mSS.removeSpan(spans[i]);
        }
        mMessageContentView.setText(mSS, TextView.BufferType.SPANNABLE);
        mMessageContentView.setSelection(end);
    }


}
