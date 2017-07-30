package ua.hospes.undobutton;

import java.util.HashMap;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * @author Andrew Khloponin
 */
class RxUtils {
    @SuppressWarnings("unchecked")
    static <T> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static final HashMap<Object, CompositeDisposable> sSubscriptions = new HashMap<Object, CompositeDisposable>();

    static void manage(Object tag, Disposable disposable) {
        CompositeDisposable disposables = sSubscriptions.get(tag);
        if (disposables == null) {
            disposables = new CompositeDisposable();
            sSubscriptions.put(tag, disposables);
        }

        disposables.add(disposable);
    }

    static void unsubscribe(Object tag) {
        CompositeDisposable disposables = sSubscriptions.get(tag);
        if (disposables != null) {
            disposables.dispose();
            sSubscriptions.remove(tag);
        }
    }
}