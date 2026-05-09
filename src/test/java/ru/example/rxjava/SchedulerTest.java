package ru.example.rxjava;

import org.junit.jupiter.api.Test;
import ru.example.rxjava.core.Observable;
import ru.example.rxjava.core.Observer;
import ru.example.rxjava.schedulers.ComputationScheduler;
import ru.example.rxjava.schedulers.IOThreadScheduler;
import ru.example.rxjava.schedulers.Scheduler;
import ru.example.rxjava.schedulers.SingleThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void shouldExecuteTaskOnIOThreadScheduler() throws InterruptedException {
        IOThreadScheduler scheduler = new IOThreadScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();
            List<String> threadNames = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                latch.countDown();
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, threadNames.size());
            assertNotEquals(mainThreadName, threadNames.get(0));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void shouldExecuteTaskOnComputationScheduler() throws InterruptedException {
        ComputationScheduler scheduler = new ComputationScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();
            List<String> threadNames = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                latch.countDown();
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, threadNames.size());
            assertNotEquals(mainThreadName, threadNames.get(0));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void shouldExecuteTaskOnSingleThreadScheduler() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();
            List<String> threadNames = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(2);

            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                latch.countDown();
            });

            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                latch.countDown();
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(2, threadNames.size());
            assertNotEquals(mainThreadName, threadNames.get(0));
            assertEquals(threadNames.get(0), threadNames.get(1));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void shouldSubscribeOnDifferentThread() throws InterruptedException {
        IOThreadScheduler scheduler = new IOThreadScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();
            List<String> sourceThreadNames = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            Observable<Integer> observable = Observable.create(emitter -> {
                sourceThreadNames.add(Thread.currentThread().getName());
                emitter.onNext(1);
                emitter.onComplete();
            });

            observable
                    .subscribeOn(scheduler)
                    .subscribe(new Observer<>() {
                        @Override
                        public void onNext(Integer item) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            fail("Error was not expected");
                        }

                        @Override
                        public void onComplete() {
                            latch.countDown();
                        }
                    });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, sourceThreadNames.size());
            assertNotEquals(mainThreadName, sourceThreadNames.get(0));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void shouldObserveOnDifferentThread() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();
            List<String> observerThreadNames = new ArrayList<>();
            List<Integer> result = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            Observable<Integer> observable = Observable.create(emitter -> {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            });

            observable
                    .observeOn(scheduler)
                    .subscribe(new Observer<>() {
                        @Override
                        public void onNext(Integer item) {
                            result.add(item);
                            observerThreadNames.add(Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            fail("Error was not expected");
                        }

                        @Override
                        public void onComplete() {
                            observerThreadNames.add(Thread.currentThread().getName());
                            latch.countDown();
                        }
                    });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(List.of(1, 2, 3), result);
            assertFalse(observerThreadNames.isEmpty());

            for (String threadName : observerThreadNames) {
                assertNotEquals(mainThreadName, threadName);
                assertEquals(observerThreadNames.get(0), threadName);
            }
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void shouldUseSubscribeOnAndObserveOnTogether() throws InterruptedException {
        IOThreadScheduler subscribeScheduler = new IOThreadScheduler();
        SingleThreadScheduler observeScheduler = new SingleThreadScheduler();

        try {
            String mainThreadName = Thread.currentThread().getName();

            List<String> sourceThreadNames = new ArrayList<>();
            List<String> observerThreadNames = new ArrayList<>();
            List<String> result = new ArrayList<>();

            CountDownLatch latch = new CountDownLatch(1);

            Observable<Integer> observable = Observable.create(emitter -> {
                sourceThreadNames.add(Thread.currentThread().getName());
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            });

            observable
                    .map(number -> "Number: " + number)
                    .subscribeOn(subscribeScheduler)
                    .observeOn(observeScheduler)
                    .subscribe(new Observer<>() {
                        @Override
                        public void onNext(String item) {
                            result.add(item);
                            observerThreadNames.add(Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            fail("Error was not expected");
                        }

                        @Override
                        public void onComplete() {
                            observerThreadNames.add(Thread.currentThread().getName());
                            latch.countDown();
                        }
                    });

            assertTrue(latch.await(2, TimeUnit.SECONDS));

            assertEquals(List.of("Number: 1", "Number: 2", "Number: 3"), result);

            assertEquals(1, sourceThreadNames.size());
            assertNotEquals(mainThreadName, sourceThreadNames.get(0));

            assertFalse(observerThreadNames.isEmpty());

            for (String threadName : observerThreadNames) {
                assertNotEquals(mainThreadName, threadName);
                assertEquals(observerThreadNames.get(0), threadName);
            }

            assertNotEquals(sourceThreadNames.get(0), observerThreadNames.get(0));
        } finally {
            subscribeScheduler.shutdown();
            observeScheduler.shutdown();
        }
    }
}