package ua.hospes.undobutton;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrew Khloponin
 */
@SuppressWarnings("WeakerAccess")
public abstract class UndoButtonController<H extends RecyclerView.ViewHolder> extends RxScrollListener implements OnControllerListener {
    private static final float AUTO_PLAY_AREA_START_PADDING_RELATIVE = 0.2f;
    private static final float AUTO_PLAY_AREA_END_PADDING_RELATIVE = 0.2f;
    private final Handler mainHandler;
    private long lastRecalculationTime = -1;
    private Set<BaseButton> visibleItems = new HashSet<>();
    private final SparseArray<State> states = new SparseArray<>();

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    public UndoButtonController(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        executor.scheduleAtFixedRate(new CheckTimeState(), 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    void onScrolled(RecyclerView rv) {
        if (System.currentTimeMillis() < lastRecalculationTime + SKIP_RECALCULATION_DURATION) return;
        lastRecalculationTime = System.currentTimeMillis();

        Set<BaseButton> shouldPlayItems = collectVisibleItems(rv);

        Iterator<BaseButton> iterator = visibleItems.iterator();
        while (iterator.hasNext()) {
            BaseButton next = iterator.next();
            if (!shouldPlayItems.contains(next)) {
                //next.autoPause();
                iterator.remove();
            }
        }
        iterator = shouldPlayItems.iterator();
        while (iterator.hasNext()) {
            BaseButton next = iterator.next();
            if (!visibleItems.contains(next)) {
                checkIfStateExists(next);
                visibleItems.add(next);
            }
        }
        visibleItems = shouldPlayItems;
    }


    public void onBind(int id, BaseButton button) {
        button.setTag(id);
        State state = checkIfStateExists(button);
        button.setUndo(state.undo);
        button.setTimeShow(state.timeShow);
        if (state.timeStart == -1) button.finishUndo();
    }

    public abstract BaseButton[] provideUndos(H holder);

    public boolean defaultUndoState() {
        return false;
    }

    public boolean defaultTimeSHow() {
        return false;
    }

    public int defaultDelay() {
        return 3;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private Set<BaseButton> collectVisibleItems(RecyclerView rv) throws ClassCastException {
        Set<BaseButton> set = new HashSet<>();

        RecyclerView.LayoutManager lm = rv.getLayoutManager();

        int autoPlayAreaStart = (int) (rv.getTop() - rv.getHeight() * AUTO_PLAY_AREA_START_PADDING_RELATIVE);
        int autoPlayAreaEnd = (int) (rv.getBottom() + rv.getHeight() * AUTO_PLAY_AREA_END_PADDING_RELATIVE);

        int count = lm.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = lm.getChildAt(i);
            int viewStart = lm.getDecoratedTop(child);
            int viewEnd = lm.getDecoratedBottom(child);

            boolean visible = false;
            visible = visible || (rv.getTop() <= viewStart && rv.getBottom() >= viewEnd); // completely visible
            visible = visible || !(autoPlayAreaStart > viewEnd || autoPlayAreaEnd < viewStart); // near center;

            if (visible) {
                Collections.addAll(set, provideUndos((H) rv.getChildViewHolder(child)));
            }
        }
        return set;
    }

    private State checkIfStateExists(BaseButton button) {
        if (button.getTag() == null) throw new IllegalStateException("Provide tag for Undo/Delay buttons");
        State state = states.get(button.getTag().hashCode());
        if (state == null) {
            state = newState();
            states.append(button.getTag().hashCode(), state);
        }
        return state;
    }


    @Override
    public void onStartTime(BaseButton button) {
        State state = states.get(button.getTag().hashCode());
        if (state == null) return;

        state.undo = true;
        state.timeStart = System.currentTimeMillis();
    }

    @Override
    public void onCancelTime(BaseButton button) {
        State state = states.get(button.getTag().hashCode());
        if (state == null) return;

        state.undo = false;
        state.timeStart = -1;
    }

    @Override
    public void release() {
        super.release();
        executor.shutdownNow();
    }

    private State newState() {
        State state = new State();
        state.undo = defaultUndoState();
        state.timeShow = defaultTimeSHow();
        state.timeDuration = defaultDelay();
        return state;
    }

    private class State {
        boolean undo = false, timeShow = false;
        long timeStart = -1, timeDuration = -1;

        @Override
        public String toString() {
            return "State{" +
                    "undo=" + undo +
                    ", timeStart=" + timeStart +
                    ", timeDuration=" + timeDuration +
                    '}';
        }
    }

    private class CheckTimeState implements Runnable {
        @SuppressWarnings("WhileLoopReplaceableByForEach")
        @Override
        public void run() {
            Iterator<BaseButton> iterator = visibleItems.iterator();
            while (iterator.hasNext()) {
                BaseButton button = iterator.next();

                State state = states.get(button.getTag().hashCode());
                if (state == null || !state.undo || state.timeStart == -1 || state.timeDuration <= 0) continue;

                //Log.d(TAG, "timer | id: " + button.getTag().hashCode() + ", " + state);

                long currentTime = System.currentTimeMillis();
                long millisToFinish = (state.timeStart + state.timeDuration * 1000) - currentTime;
                if (millisToFinish > 0) {
                    mainHandler.post(() -> button.setTimeToFinishUndo(millisToFinish));
                } else {
                    state.undo = false;
                    state.timeStart = -1;

                    mainHandler.post(button::finishUndo);
                }
            }
        }
    }
}