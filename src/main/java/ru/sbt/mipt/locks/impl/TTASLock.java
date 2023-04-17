package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock implements SpinLock {
    AtomicBoolean locked = new AtomicBoolean(false); // true - лок захвачен, false - свободен

    @Override
    public void lock() {
        while (true) {
            while (locked.get()) {};
            if (!locked.getAndSet(true)) // лок все еще не захвачен?
                return; // захватили лок, выходим
        }
    }

    @Override
    public void unlock() {
        locked.set(false);
    }

    @Override
    public boolean isLocked() {
        return locked.get();
    }

}
