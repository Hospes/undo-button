package ua.hospes.undobutton;

import java.util.HashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Andrew Khloponin
 */
class RxUtils {
    @SuppressWarnings("unchecked")
    static <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static final HashMap<Object, CompositeSubscription> sSubscriptions = new HashMap<Object, CompositeSubscription>();

    static void manage(Object tag, Subscription subscription) {
        CompositeSubscription subscriptions = sSubscriptions.get(tag);
        if (subscriptions == null) {
            subscriptions = new CompositeSubscription();
            sSubscriptions.put(tag, subscriptions);
        }

        subscriptions.add(subscription);
    }

    static void unsubscribe(Object tag) {
        CompositeSubscription subscriptions = sSubscriptions.get(tag);
        if (subscriptions != null) {
            subscriptions.unsubscribe();
            sSubscriptions.remove(tag);
        }
    }
}