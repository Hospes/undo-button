package ua.hospes.undobutton;

import android.support.v7.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

import rx.subjects.PublishSubject;

/**
 * @author Andrew Khloponin
 */
@SuppressWarnings("WeakerAccess")
abstract class RxScrollListener extends RecyclerView.OnScrollListener {
    protected static final long SKIP_RECALCULATION_DURATION = 300;
    private final PublishSubject<RecyclerView> subject = PublishSubject.create();


    RxScrollListener() {
        RxUtils.manage(this, subject.debounce(SKIP_RECALCULATION_DURATION, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applySchedulers())
                .subscribe(this::onScrolled));
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        subject.onNext(recyclerView);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            subject.onNext(recyclerView);
        }
    }


    abstract void onScrolled(RecyclerView rv);


    public void forceUpdate(RecyclerView recyclerView) {
        subject.onNext(recyclerView);
    }

    public void release() {
        RxUtils.unsubscribe(this);
    }
}