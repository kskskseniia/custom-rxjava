package ru.example.rxjava;

import org.junit.jupiter.api.Test;
import ru.example.rxjava.core.Disposable;
import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void shouldEmitItemsAndComplete() {
        List<Integer> result = new ArrayList<>();
        boolean[] completed = {false};

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
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

        assertEquals(List.of(1, 2, 3), result);
        assertTrue(completed[0]);
    }

    @Test
    void shouldMapItems() {
        List<String> result = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable
                .map(number -> "Number: " + number)
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

        assertEquals(List.of("Number: 1", "Number: 2", "Number: 3"), result);
    }

    @Test
    void shouldFilterItems() {
        List<Integer> result = new ArrayList<>();

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
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
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

        assertEquals(List.of(1, 3, 5), result);
    }

    @Test
    void shouldMapAndFilterItems() {
        List<String> result = new ArrayList<>();

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

        assertEquals(List.of("Number: 1", "Number: 3", "Number: 5"), result);
    }

    @Test
    void shouldCallOnErrorWhenSourceThrowsException() {
        List<String> errors = new ArrayList<>();
        boolean[] completed = {false};

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            throw new RuntimeException("Test error");
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {

            }

            @Override
            public void onError(Throwable throwable) {
                errors.add(throwable.getMessage());
            }

            @Override
            public void onComplete() {
                completed[0] = true;
            }
        });

        assertEquals(List.of("Test error"), errors);
        assertFalse(completed[0]);
    }

    @Test
    void shouldCallOnErrorWhenMapThrowsException() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable
                .map(number -> {
                    if (number == 2) {
                        throw new RuntimeException("Map error");
                    }
                    return number * 10;
                })
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("Map error"), errors);
    }

    @Test
    void shouldCallOnErrorWhenFilterThrowsException() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable
                .filter(number -> {
                    if (number == 2) {
                        throw new RuntimeException("Filter error");
                    }
                    return true;
                })
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        assertEquals(List.of("Filter error"), errors);
    }

    @Test
    void shouldReturnDisposableAfterSubscribe() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        Disposable disposable = observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        assertNotNull(disposable);
        assertTrue(disposable.isDisposed());
    }
