package ru.example.rxjava;

import org.junit.jupiter.api.Test;
import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlatMapTest {

    @Test
    void shouldFlatMapItems() {
        List<String> result = new ArrayList<>();
        boolean[] completed = {false};

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable
                .flatMap(number -> Observable.<String>create(innerEmitter -> {
                    innerEmitter.onNext("A" + number);
                    innerEmitter.onNext("B" + number);
                    innerEmitter.onComplete();
                }))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        result.add(item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail("Error was not expected");
                    }

                    @Override
                    public void onComplete() {
                        completed[0] = true;
                    }
                });

        assertEquals(List.of("A1", "B1", "A2", "B2", "A3", "B3"), result);
        assertTrue(completed[0]);
    }

    @Test
    void shouldFlatMapAndMapItems() {
        List<String> result = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable
                .flatMap(number -> Observable.<Integer>create(innerEmitter -> {
                    innerEmitter.onNext(number * 10);
                    innerEmitter.onNext(number * 100);
                    innerEmitter.onComplete();
                }))
                .map(number -> "Value: " + number)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        result.add(item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail("Error was not expected");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("Value: 10", "Value: 100", "Value: 20", "Value: 200"), result);
    }

    @Test
    void shouldCallOnErrorWhenFlatMapMapperThrowsException() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable
                .flatMap(number -> {
                    if (number == 2) {
                        throw new RuntimeException("FlatMap mapper error");
                    }

                    return Observable.<String>create(innerEmitter -> {
                        innerEmitter.onNext("Number: " + number);
                        innerEmitter.onComplete();
                    });
                })
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("FlatMap mapper error"), errors);
    }

    @Test
    void shouldCallOnErrorWhenInnerObservableThrowsException() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        observable
                .flatMap(number -> Observable.<String>create(innerEmitter -> {
                    innerEmitter.onNext("Number: " + number);
                    throw new RuntimeException("Inner observable error");
                }))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("Inner observable error"), errors);
    }

    @Test
    void shouldCallOnErrorWhenFlatMapReturnsNull() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        observable
                .flatMap(number -> null)
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("mapper returned null"), errors);
    }
}