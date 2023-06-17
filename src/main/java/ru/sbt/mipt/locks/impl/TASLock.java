package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicBoolean;

public class TASLock implements SpinLock {
    AtomicBoolean locked = new AtomicBoolean(false); // true - лок захвачен, false - свободен

    @Override
    public void lock() {
        while (locked.getAndSet(true)) {}
    }

    @Override
    public void unlock() {
        locked.set(false);
    }

    @Override
    public String toString() {
        return "TASLock";
    }
}
