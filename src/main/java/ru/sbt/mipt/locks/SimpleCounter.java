package ru.sbt.mipt.locks;

import lombok.Getter;

@Getter
public class SimpleCounter {
    private long count;
    private SpinLock lock;

    public SimpleCounter(long count, SpinLock lock) {
        this.count = count;
        this.lock = lock;
    }

    public long addAndReturnNewValue(long value) {
        lock.lock();
        long returnValue;
        try {
            count += value;
            returnValue = count;
        } finally {
            lock.unlock();
        }
        // returning a value does not allow JVM to optimise adding
        return returnValue;
    }
}
