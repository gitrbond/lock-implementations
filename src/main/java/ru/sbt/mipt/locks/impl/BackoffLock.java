package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicBoolean;
import static java.lang.Math.random;
import static java.lang.Thread.sleep;

public class BackoffLock  implements SpinLock {
    AtomicBoolean locked = new AtomicBoolean(false); // true - лок захвачен, false - свободен
    // Пределы изменения задержки, в миллисекундах
    static final long MIN_DELAY = 1;
    static final long MAX_DELAY = 256;

    @Override
    public void lock() {
        long delay = MIN_DELAY;
        long currentMaxDelay = MIN_DELAY;
        while (true) {
            while (locked.get()) {}
            if (!locked.getAndSet(true)) // лок все еще не захвачен?
                return; // захватили лок, выходим
            currentMaxDelay = Math.min(currentMaxDelay * 2, MAX_DELAY);
            delay = (long) (MIN_DELAY + (currentMaxDelay - MIN_DELAY) * random() ); // задержка выбирается случайно в диапазоне [MIN_DELAY, currentMaxDelay)
            try {
                sleep(delay);
            }
            catch(InterruptedException e) {}
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
