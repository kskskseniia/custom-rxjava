package ru.example.rxjava;

import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;
import ru.example.rxjava.schedulers.IOThreadScheduler;
import ru.example.rxjava.schedulers.SingleThreadScheduler;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();

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

        ioScheduler.shutdown();
        singleThreadScheduler.shutdown();
    }
}