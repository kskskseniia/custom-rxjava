package ru.example.rxjava;

import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;
import ru.example.rxjava.core.Disposable;
import ru.example.rxjava.schedulers.IOThreadScheduler;
import ru.example.rxjava.schedulers.SingleThreadScheduler;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();

        System.out.println("=== map/filter + schedulers demo ===");

        Observable<Integer> observable = Observable.create(emitter -> {
            System.out.println("Source thread: " + Thread.currentThread().getName());

            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onNext(4);
            emitter.onNext(5);
            emitter.onComplete();
        });

        observable
                .filter(number -> number % 2 != 0)
                .map(number -> "Number: " + number)
                .subscribeOn(ioScheduler)
                .observeOn(singleThreadScheduler)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("onNext: " + item
                                + " | thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("onError: " + throwable.getMessage()
                                + " | thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete"
                                + " | thread: " + Thread.currentThread().getName());
                    }
                });

        Thread.sleep(1000);

        System.out.println();
        System.out.println("=== flatMap demo ===");

        Observable<Integer> flatMapObservable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        flatMapObservable
                .flatMap(number -> Observable.<String>create(innerEmitter -> {
                    innerEmitter.onNext("A" + number);
                    innerEmitter.onNext("B" + number);
                    innerEmitter.onComplete();
                }))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("flatMap onNext: " + item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("flatMap onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("flatMap onComplete");
                    }
                });

        System.out.println();
        System.out.println("=== error handling demo ===");

        Observable<Integer> errorObservable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            throw new RuntimeException("Test error from source");
        });

        errorObservable
                .map(number -> "Value: " + number)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("error demo onNext: " + item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("error demo onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("error demo onComplete");
                    }
                });

        System.out.println();
        System.out.println("=== disposable demo ===");

        Observable<String> disposableObservable = Observable.create(emitter -> {
            Thread.sleep(300);
            emitter.onNext("This item should not be printed");
            emitter.onComplete();
        });

        Disposable disposable = disposableObservable
                .subscribeOn(ioScheduler)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("disposable onNext: " + item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("disposable onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("disposable onComplete");
                    }
                });

        disposable.dispose();

        System.out.println("Subscription disposed: " + disposable.isDisposed());

        Thread.sleep(500);

        ioScheduler.shutdown();
        singleThreadScheduler.shutdown();
    }
}