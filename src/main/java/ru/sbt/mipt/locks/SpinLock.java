package ru.sbt.mipt.locks;

public interface SpinLock {
    /**
     * Захватить лок
     */
    void lock() throws InterruptedException;

    /**
     * Освободить лок
     */
    void unlock();
}
