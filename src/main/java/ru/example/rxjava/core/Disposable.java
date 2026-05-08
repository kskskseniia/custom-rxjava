package ru.example.rxjava.core;

public interface Disposable {

    void dispose();

    boolean isDisposed();
}