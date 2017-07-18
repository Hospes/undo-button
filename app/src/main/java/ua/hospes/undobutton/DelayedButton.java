package ua.hospes.undobutton;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @author Andrew Khloponin
 */
public class DelayedButton extends BaseButton {
    public DelayedButton(@NonNull Context context) {
        super(context);
    }

    public DelayedButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayedButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        boolean result;
        if (isUndo()) {
            result = false;
        } else {
            result = super.performClick();
            startUndo();
        }
        return result;
    }

    @Override
    public void setUndo(boolean undo) {
        super.setUndo(undo);
        setClickable(!undo);
    }
}