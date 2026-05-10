# **Custom RxJava**

Учебный проект по дисциплине **«Многопоточное и асинхронное программирование на Java»**.

Проект представляет собой собственную упрощённую реализацию библиотеки, похожей на **RxJava**.  
В работе реализованы базовые компоненты реактивного потока, операторы преобразования данных, управление потоками выполнения через `Schedulers`, обработка ошибок и механизм отмены подписки через `Disposable`.

---

## **Описание проекта**

Цель проекта — реализовать собственную мини-библиотеку реактивного программирования на Java с использованием паттерна **Observer**.

Реализованная система позволяет:

- создавать реактивный поток через `Observable.create(...)`;
- подписываться на поток через `subscribe(...)`;
- получать элементы через `onNext(...)`;
- обрабатывать ошибки через `onError(...)`;
- получать событие завершения через `onComplete()`;
- преобразовывать элементы через `map(...)`;
- фильтровать элементы через `filter(...)`;
- преобразовывать элементы во вложенные `Observable` через `flatMap(...)`;
- управлять потоком выполнения через `subscribeOn(...)` и `observeOn(...)`;
- отменять подписку через `Disposable`.

---

## **Требования**

Для запуска проекта необходимо:

- **Java 17** или выше;
- **Maven 3.8+**;
- IntelliJ IDEA или другая IDE для работы с Java-проектом.

---

## **Структура проекта**

```text
src/main/java/ru/example/rxjava
├── Main.java
├── core
│   ├── Disposable.java
│   ├── Emitter.java
│   ├── Observable.java
│   ├── ObservableOnSubscribe.java
│   └── Observer.java
└── schedulers
    ├── ComputationScheduler.java
    ├── IOThreadScheduler.java
    ├── Scheduler.java
    └── SingleThreadScheduler.java
```

Тесты:

```text
src/test/java/ru/example/rxjava
├── DisposableTest.java
├── FlatMapTest.java
├── ObservableTest.java
└── SchedulerTest.java
```

---

## **Основные компоненты**

### **Observer**

`Observer<T>` — это интерфейс наблюдателя, который получает события из реактивного потока.

Он содержит три метода:

```java
void onNext(T item);
void onError(Throwable throwable);
void onComplete();
```

Назначение методов:

| Метод | Назначение |
|---|---|
| `onNext(T item)` | Получает очередной элемент потока |
| `onError(Throwable throwable)` | Получает ошибку, если она произошла |
| `onComplete()` | Вызывается при успешном завершении потока |

---

### **Emitter**

`Emitter<T>` используется внутри `Observable.create(...)`.

Через него источник данных отправляет события подписчику:

```java
Observable<Integer> observable = Observable.create(emitter -> {
    emitter.onNext(1);
    emitter.onNext(2);
    emitter.onNext(3);
    emitter.onComplete();
});
```

---

### **Observable**

`Observable<T>` — основной класс библиотеки.

Он отвечает за:

- создание потока данных;
- подписку наблюдателя;
- передачу событий;
- обработку ошибок;
- работу операторов `map`, `filter`, `flatMap`;
- управление потоками через `subscribeOn` и `observeOn`.

Создание `Observable` выполняется через статический метод:

```java
Observable.create(...)
```

Подписка выполняется через:

```java
subscribe(...)
```

---

### **Disposable**

`Disposable` используется для отмены подписки.

Интерфейс содержит методы:

```java
void dispose();
boolean isDisposed();
```

Если подписка отменена, новые события больше не должны передаваться подписчику.

Пример:

```java
Disposable disposable = observable.subscribe(observer);
disposable.dispose();
```

---

## **Операторы преобразования данных**

### **map**

Оператор `map(...)` преобразует каждый элемент потока.

Пример:

```java
observable
        .map(number -> "Number: " + number)
        .subscribe(observer);
```

Если исходный поток содержит:

```text
1, 2, 3
```

то после `map` получится:

```text
Number: 1
Number: 2
Number: 3
```

---

### **filter**

Оператор `filter(...)` пропускает только те элементы, которые соответствуют условию.

Пример:

```java
observable
        .filter(number -> number % 2 != 0)
        .subscribe(observer);
```

Если исходный поток содержит:

```text
1, 2, 3, 4, 5
```

то после фильтрации получится:

```text
1, 3, 5
```

---

### **flatMap**

Оператор `flatMap(...)` преобразует каждый элемент исходного потока в новый `Observable`, а затем передаёт элементы из внутренних потоков дальше.

Пример:

```java
observable
        .flatMap(number -> Observable.<String>create(innerEmitter -> {
            innerEmitter.onNext("A" + number);
            innerEmitter.onNext("B" + number);
            innerEmitter.onComplete();
        }))
        .subscribe(observer);
```

Если исходный поток содержит:

```text
1, 2, 3
```

то результат будет:

```text
A1
B1
A2
B2
A3
B3
```

---

## **Schedulers**

Для управления потоками выполнения реализован интерфейс `Scheduler`.

```java
public interface Scheduler {

    void execute(Runnable task);

    void shutdown();
}
```

По заданию основной метод — `execute(Runnable task)`.  
Метод `shutdown()` добавлен для корректного завершения потоков в демонстрации и тестах.

---

### **IOThreadScheduler**

`IOThreadScheduler` использует:

```java
Executors.newCachedThreadPool()
```

Он подходит для задач, связанных с ожиданием:

- сетевые запросы;
- работа с файлами;
- операции ввода-вывода.

---

### **ComputationScheduler**

`ComputationScheduler` использует фиксированный пул потоков:

```java
Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
```

Он подходит для вычислительных задач, где количество потоков обычно связано с количеством доступных ядер процессора.

---

### **SingleThreadScheduler**

`SingleThreadScheduler` использует:

```java
Executors.newSingleThreadExecutor()
```

Он выполняет все задачи последовательно в одном отдельном потоке.

Такой scheduler удобно использовать, когда важен порядок обработки событий.

---

## **subscribeOn и observeOn**

### **subscribeOn**

Метод `subscribeOn(...)` задаёт поток, в котором выполняется источник данных, то есть код внутри `Observable.create(...)`.

Пример:

```java
observable
        .subscribeOn(ioScheduler)
        .subscribe(observer);
```

В этом случае генерация данных будет выполняться не в `main`, а в потоке scheduler-а.

---

### **observeOn**

Метод `observeOn(...)` задаёт поток, в котором подписчик получает события.

Пример:

```java
observable
        .observeOn(singleThreadScheduler)
        .subscribe(observer);
```

В этом случае методы `onNext`, `onError` и `onComplete` будут вызываться в потоке указанного scheduler-а.

---

### **Совместное использование**

В проекте показана работа `subscribeOn` и `observeOn` вместе:

```java
observable
        .filter(number -> number % 2 != 0)
        .map(number -> "Number: " + number)
        .subscribeOn(ioScheduler)
        .observeOn(singleThreadScheduler)
        .subscribe(observer);
```

В этом примере:

- источник данных выполняется в потоке `IOThreadScheduler`;
- обработчик событий выполняется в потоке `SingleThreadScheduler`.

---

## **Обработка ошибок**

Ошибки передаются в метод:

```java
onError(Throwable throwable)
```

Ошибки обрабатываются в следующих случаях:

- ошибка возникает внутри `Observable.create(...)`;
- ошибка возникает внутри `map(...)`;
- ошибка возникает внутри `filter(...)`;
- ошибка возникает внутри `flatMap(...)`;
- ошибка возникает во внутреннем `Observable` внутри `flatMap(...)`.

Пример:

```java
Observable<Integer> errorObservable = Observable.create(emitter -> {
    emitter.onNext(1);
    emitter.onNext(2);
    throw new RuntimeException("Test error from source");
});
```

При возникновении ошибки будет вызван `onError(...)`.

---

## **Демонстрационная программа**

В классе `Main` показаны основные возможности библиотеки.

Демонстрируются:

- `map` и `filter`;
- `subscribeOn` и `observeOn`;
- `flatMap`;
- обработка ошибок через `onError`;
- отмена подписки через `Disposable`.

Пример вывода:

```text
=== map/filter + schedulers demo ===
Source thread: pool-1-thread-1
onNext: Number: 1 | thread: pool-2-thread-1
onNext: Number: 3 | thread: pool-2-thread-1
onNext: Number: 5 | thread: pool-2-thread-1
onComplete | thread: pool-2-thread-1

=== flatMap demo ===
flatMap onNext: A1
flatMap onNext: B1
flatMap onNext: A2
flatMap onNext: B2
flatMap onNext: A3
flatMap onComplete

=== error handling demo ===
error demo onNext: Value: 1
error demo onNext: Value: 2
error demo onError: Test error from source

=== disposable demo ===
Subscription disposed: true
```

---

## **Тестирование**

Для проверки работы библиотеки написаны JUnit-тесты.

### **ObservableTest**

Проверяет базовую работу `Observable` и операторов:

- создание потока через `create`;
- получение элементов через `onNext`;
- завершение через `onComplete`;
- оператор `map`;
- оператор `filter`;
- совместную работу `map` и `filter`;
- обработку ошибок из source;
- обработку ошибок внутри `map`;
- обработку ошибок внутри `filter`;
- возврат `Disposable` после подписки.

---

### **SchedulerTest**

Проверяет работу scheduler-ов и переключение потоков:

- `IOThreadScheduler` выполняет задачу не в `main`;
- `ComputationScheduler` выполняет задачу не в `main`;
- `SingleThreadScheduler` выполняет задачи в одном потоке;
- `subscribeOn` переносит выполнение источника в другой поток;
- `observeOn` переносит обработку событий в другой поток;
- `subscribeOn` и `observeOn` корректно работают вместе.

---

### **FlatMapTest**

Проверяет оператор `flatMap`:

- преобразование элементов во вложенные `Observable`;
- совместную работу `flatMap` и `map`;
- обработку ошибки в mapper-функции;
- обработку ошибки во внутреннем `Observable`;
- обработку ситуации, когда mapper возвращает `null`.

---

### **DisposableTest**

Проверяет отмену подписки:

- `subscribe()` возвращает объект `Disposable`;
- после вызова `dispose()` подписчик больше не получает события.

---

## **Сборка проекта**

Для компиляции проекта выполните:

```bash
mvn clean compile
```

Для запуска тестов:

```bash
mvn clean test
```

Для сборки проекта:

```bash
mvn clean package
```

---

## **Запуск проекта**

### **Запуск из IntelliJ IDEA**

Откройте файл:

```text
src/main/java/ru/example/rxjava/Main.java
```

И запустите метод `main`.

---

### **Запуск через Maven**

Если используется запуск через Maven:

```bash
mvn exec:java -Dexec.mainClass="ru.example.rxjava.Main"
```

---

## **Итог**

В результате работы была реализована собственная мини-библиотека, похожая на RxJava.

В проекте реализованы:

- базовые компоненты реактивного потока;
- паттерн Observer;
- создание `Observable`;
- подписка на поток;
- операторы `map`, `filter`, `flatMap`;
- обработка ошибок через `onError`;
- управление потоками через `Schedulers`;
- методы `subscribeOn` и `observeOn`;
- отмена подписки через `Disposable`;
- JUnit-тесты для ключевых компонентов.

Проект демонстрирует основные принципы реактивного программирования и асинхронной обработки событий на Java.
