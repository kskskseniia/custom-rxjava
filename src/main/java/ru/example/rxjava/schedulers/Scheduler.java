package ru.example.rxjava.schedulers;

public interface Scheduler {

    void execute(Runnable task);

    void shutdown();
}