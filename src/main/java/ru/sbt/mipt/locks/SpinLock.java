package ru.sbt.mipt.locks;

public interface SpinLock {
    void lock();
    void unlock();
}
