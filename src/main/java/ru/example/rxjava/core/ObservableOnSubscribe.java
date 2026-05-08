package ru.example.rxjava.core;

@FunctionalInterface
public interface ObservableOnSubscribe<T> {

    void subscribe(Emitter<T> emitter) throws Exception;
}