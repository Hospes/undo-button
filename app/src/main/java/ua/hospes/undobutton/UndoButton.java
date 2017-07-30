package ua.hospes.undobutton;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * @author Andrew Khloponin
 */
public class UndoButton extends BaseButton {
    private ColorFilter defaultFilter;
    private View.OnClickListener onUndoClickListener = null;

    public UndoButton(@NonNull Context context) {
        super(context);
    }

    public UndoButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UndoButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean performClick() {
        boolean result;
        if (isUndo()) {
            result = performUndoClick();
        } else {
            result = super.performClick();
            startUndo();
        }
        return result;
    }

    public boolean performUndoClick() {
        final boolean result;
        if (onUndoClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            onUndoClickListener.onClick(this);

            dismissUndo();

            result = true;
        } else {
            result = false;
        }

        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
        return result;
    }

    @Override
    public void setUndo(boolean undo) {
        super.setUndo(undo);
        if (getBackground() == null) return;
        if (undo) {
            if (defaultFilter == null) defaultFilter = getBackground().getColorFilter();
            getBackground().setColorFilter(getResources().getColor(R.color.undo), PorterDuff.Mode.SRC);
        } else {
            getBackground().setColorFilter(defaultFilter);
        }
    }

    public void setOnUndoClickListener(OnClickListener onUndoClickListener) {
        this.onUndoClickListener = onUndoClickListener;
    }

    
}