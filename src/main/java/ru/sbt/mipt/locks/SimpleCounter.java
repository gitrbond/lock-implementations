package ru.sbt.mipt.locks;

import lombok.Getter;

@Getter
public class SimpleCounter {
    private long count;
    private static SpinLock lock;

    public SimpleCounter(long count, SpinLock lock) {
        this.count = count;
        SimpleCounter.lock = lock;
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
