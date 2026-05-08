package ru.example.rxjava;

import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;

public class Main {

    public static void main(String[] args) {
        Observable<Integer> observable = Observable.create(emitter -> {
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
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("onNext: " + item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
    }
}