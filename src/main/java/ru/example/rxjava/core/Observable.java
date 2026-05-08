package ru.example.rxjava.core;

import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {

    private final ObservableOnSubscribe<T> source;

    private Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        if (source == null) {
            throw new NullPointerException("source is null");
        }

        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<T> observer) {
        if (observer == null) {
            throw new NullPointerException("observer is null");
        }

        SimpleDisposable disposable = new SimpleDisposable();

        Emitter<T> emitter = new Emitter<>() {
            private boolean completed = false;

            @Override
            public void onNext(T item) {
                if (!disposable.isDisposed() && !completed) {
                    observer.onNext(item);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (!disposable.isDisposed() && !completed) {
                    completed = true;
                    observer.onError(throwable);
                    disposable.dispose();
                }
            }

            @Override
            public void onComplete() {
                if (!disposable.isDisposed() && !completed) {
                    completed = true;
                    observer.onComplete();
                    disposable.dispose();
                }
            }
        };

        try {
            source.subscribe(emitter);
        } catch (Throwable throwable) {
            emitter.onError(throwable);
        }

        return disposable;
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }

        return Observable.create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            R mappedItem = mapper.apply(item);
                            emitter.onNext(mappedItem);
                        } catch (Throwable throwable) {
                            emitter.onError(throwable);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        if (predicate == null) {
            throw new NullPointerException("predicate is null");
        }

        return Observable.create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                emitter.onNext(item);
                            }
                        } catch (Throwable throwable) {
                            emitter.onError(throwable);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                })
        );
    }

    private static class SimpleDisposable implements Disposable {

        private volatile boolean disposed = false;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}