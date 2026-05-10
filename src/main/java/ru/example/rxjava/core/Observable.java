package ru.example.rxjava.core;

import ru.example.rxjava.schedulers.Scheduler;

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

    public Observable<T> subscribeOn(Scheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler is null");
        }

        return Observable.create(emitter ->
                scheduler.execute(() ->
                        this.subscribe(new Observer<>() {
                            @Override
                            public void onNext(T item) {
                                emitter.onNext(item);
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
                )
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler is null");
        }

        return Observable.create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> emitter.onNext(item));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        scheduler.execute(() -> emitter.onError(throwable));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(emitter::onComplete);
                    }
                })
        );
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

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }

        return Observable.create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            Observable<R> innerObservable = mapper.apply(item);

                            if (innerObservable == null) {
                                emitter.onError(new NullPointerException("mapper returned null"));
                                return;
                            }

                            innerObservable.subscribe(new Observer<>() {
                                @Override
                                public void onNext(R innerItem) {
                                    emitter.onNext(innerItem);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    emitter.onError(throwable);
                                }

                                @Override
                                public void onComplete() {
                                }
                            });

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