package ru.example.rxjava;

import org.junit.jupiter.api.Test;
import ru.example.rxjava.core.Disposable;
import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;
import ru.example.rxjava.schedulers.IOThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DisposableTest {

    @Test
    void shouldReturnDisposableFromSubscribe() {
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

    @Test
    void shouldNotReceiveEventsAfterDispose() throws InterruptedException {
        IOThreadScheduler scheduler = new IOThreadScheduler();

        try {
            List<Integer> result = new ArrayList<>();
            boolean[] completed = {false};

            CountDownLatch sourceFinished = new CountDownLatch(1);

            Observable<Integer> observable = Observable.create(emitter -> {
                Thread.sleep(200);
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onComplete();
                sourceFinished.countDown();
            });

            Disposable disposable = observable
                    .subscribeOn(scheduler)
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
                            completed[0] = true;
                        }
                    });

            disposable.dispose();

            assertTrue(disposable.isDisposed());
            assertTrue(sourceFinished.await(2, TimeUnit.SECONDS));

            assertTrue(result.isEmpty());
            assertFalse(completed[0]);
        } finally {
            scheduler.shutdown();
        }
    }
}