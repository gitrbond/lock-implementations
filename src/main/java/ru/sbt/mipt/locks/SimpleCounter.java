package ru.sbt.mipt.locks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleCounter {
    private long count;
    private SpinLock lock;

    public SimpleCounter(long count, SpinLock lock) {
        this.count = count;
        this.lock = lock;
    }

    public long addAndReturnIfAdded(long value) throws InterruptedException {
        lock.lock();
        count += value;
        lock.unlock();
        return value;
    }
}
