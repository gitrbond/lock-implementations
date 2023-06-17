package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicBoolean;

// Фейковый "лок", который по факту отключает синхронизацию. Нужен для отладки и проверки корректности написанных тестов.
public class NoLock implements SpinLock {
    AtomicBoolean locked = new AtomicBoolean(false);

    @Override
    public void lock() {
        // не блокируемся, всех пропускаем
    }

    @Override
    public void unlock() {
        locked.set(false);
    }
}
