package ru.sbt.mipt.locks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimpleCounter {
    public long count;
    private SpinLock lock;

    public long addAndReturnIfAdded(long value) throws InterruptedException {
        lock.lock();
        // critical section
        count += value;
        //
        lock.unlock();
        return value;
    }

    public long getCount() throws InterruptedException {
        long returnValue;
        lock.lock();
        returnValue = count;
        lock.unlock();
        return returnValue;
    }
}