package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.util.ThreadID;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.random;

public class HBOLock implements SpinLock {
    private static final int FREE = -1;
    AtomicInteger state = new AtomicInteger(FREE);
    // limits in milliseconds
    private long MIN_DELAY = 1;
    private long MAX_DELAY = 64;

    public HBOLock(long MIN_DELAY, long MAX_DELAY) {
        if (MAX_DELAY < MIN_DELAY) {
            throw new IllegalArgumentException("MAX_DELAY must be greater than MIN_DELAY");
        }
        this.MIN_DELAY = MIN_DELAY;
        this.MAX_DELAY = MAX_DELAY;
    }

    public HBOLock() {
    }

    @Override
    public void lock() throws InterruptedException {
        int myCluster = ThreadID.getCluster();
        long delay, currentMaxDelay = MIN_DELAY;
        while (true) {
            while (state.get() != FREE) {
            }
            if (state.compareAndSet(FREE, myCluster)) {
                // lock acquired, exiting
                return;
            }
            currentMaxDelay = Math.min(currentMaxDelay * 2, MAX_DELAY);
            // delay is chosen randomly in [MIN_DELAY, currentMaxDelay) interval
            delay = (long) (MIN_DELAY + (currentMaxDelay - MIN_DELAY) * random());

            int currClaster = state.get();
            if (myCluster == currClaster) {
                Thread.sleep(delay);
            } else {
                Thread.sleep(delay * 2);
            }
        }
    }

    @Override
    public void unlock() {
        state.set(FREE);
    }

    @Override
    public String toString() {
        return "HBOLock{" +
                "MIN_DELAY=" + MIN_DELAY +
                ", MAX_DELAY=" + MAX_DELAY +
                '}';
    }
}
