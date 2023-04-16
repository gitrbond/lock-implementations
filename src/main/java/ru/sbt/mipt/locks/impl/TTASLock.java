package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;
import java.util.concurrent.atomic.AtomicBoolean;


public class TTASLock implements SpinLock {
    AtomicBoolean state = new AtomicBoolean(false);

    @Override
    public void lock() {
        while (true) {
            while (state.get()) {
            }
            if (!state.getAndSet(true))
                return;
        }
    }

    @Override
    public void unlock() {
        state.set(false);
    }
}
