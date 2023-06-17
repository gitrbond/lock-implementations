package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.random;

public class BackoffLock implements SpinLock {
    AtomicBoolean locked = new AtomicBoolean(false);
    // limits in milliseconds
    private long MIN_DELAY = 1;
    private long MAX_DELAY = 1024;

    public BackoffLock(long MIN_DELAY, long MAX_DELAY) {
        if (MAX_DELAY < MIN_DELAY) {
            throw new IllegalArgumentException("MAX_DELAY must be greater than MIN_DELAY");
        }
        this.MIN_DELAY = MIN_DELAY;
        this.MAX_DELAY = MAX_DELAY;
    }

    public BackoffLock() {
    }

    @Override
    public void lock() throws InterruptedException {
        long delay, currentMaxDelay = MIN_DELAY;
        while (true) {
            while (locked.get()) {}
            if (!locked.getAndSet(true)) {
                // lock acquired, exiting
                return;
            }
            currentMaxDelay = Math.min(currentMaxDelay * 2, MAX_DELAY);
            // delay is chosen randomly in [MIN_DELAY, currentMaxDelay) interval
            delay = (long) (MIN_DELAY + (currentMaxDelay - MIN_DELAY) * random());

            Thread.sleep(delay);
        }
    }

    @Override
    public void unlock() {
        locked.set(false);
    }

    @Override
    public String toString() {
        return "BackoffLock{" +
                "MIN_DELAY=" + MIN_DELAY +
                ", MAX_DELAY=" + MAX_DELAY +
                '}';
    }
}
