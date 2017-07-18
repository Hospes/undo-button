package ua.hospes.undobutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * @author Andrew Khloponin
 */
abstract class BaseButton extends FrameLayout {
    private static final DecimalFormat timerFormat = new DecimalFormat("(0.0s)");
    private static final boolean DEFAULT_UNDO = false;
    private static final boolean DEFAULT_TIME_SHOW = false;
    private static final int DEFAULT_DELAY = 3;
    private boolean undo = DEFAULT_UNDO;
    private boolean timeShow = DEFAULT_TIME_SHOW;
    private int delay = -1;

    private String textDefault = "", textUndo = "";
    private TextView text;
    private TextView time;

    protected OnControllerListener onControllerListener = null;

    private CountDownTimer timer = null;


    //region Constructors
    public BaseButton(@NonNull Context context) {
        this(context, null);
    }

    public BaseButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.delayedButtonStyle);
    }

    public BaseButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(context, attrs);
    }
    //endregion

    private void init(Context context) {
        inflate(context, R.layout.button_delayed, this);

        text = (TextView) findViewById(R.id.title);
        time = (TextView) findViewById(R.id.time);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DelayedButton, 0, 0);

        try {
            textDefault = a.getString(R.styleable.DelayedButton_android_text);
            textUndo = a.getString(R.styleable.DelayedButton_delb_textUndo);
            if (TextUtils.isEmpty(textUndo)) textUndo = textDefault;
            text.setText(textDefault);

            delay = a.getInt(R.styleable.DelayedButton_delb_delay, DEFAULT_DELAY);

            setTimeShow(a.getBoolean(R.styleable.DelayedButton_delb_showSeconds, DEFAULT_TIME_SHOW));
        } finally {
            a.recycle();
        }
    }


    //region Public API
    public void setController(OnControllerListener listener) {
        this.onControllerListener = listener;
    }

    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
        if (undo) {
            text.setText(textUndo);
        } else {
            text.setText(textDefault);
        }
    }

    public int getDelay() {
        return delay;
    }

    public boolean isTimeShow() {
        return timeShow;
    }

    public void setTimeShow(boolean timeShow) {
        this.timeShow = timeShow;
        time.setVisibility(timeShow ? VISIBLE : GONE);
    }
    //endregion

    //region Private API
    protected void startUndo() {
        if (onControllerListener != null) onControllerListener.onStartTime(this);
        else {
            if (timer == null) timer = new DelayedTimer(delay * 1000);
            timer.start();
        }
        setUndo(true);
    }

    protected void setTimeToFinishUndo(long millisUntilFinished) {
        time.setText(timerFormat.format((float) millisUntilFinished / 1000));
    }

    protected void dismissUndo() {
        if (onControllerListener != null) onControllerListener.onCancelTime(this);
        else timer.cancel();
        finishUndo();
    }

    protected void finishUndo() {
        time.setText("");
        setUndo(false);
    }
    //endregion

    private class DelayedTimer extends CountDownTimer {
        DelayedTimer(long millisInFuture) {
            super(millisInFuture, 100);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setTimeToFinishUndo(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            finishUndo();
        }
    }
}