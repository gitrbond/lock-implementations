package ru.sbt.mipt.locks;

public interface SpinLock {
    /**
     * Захватить лок
     */
    void lock();

    /**
     * Освободить лок
     */
    void unlock();

    /**
     * Проверить свободен ли лок
     * @return {@code true}, если лок захвачен каким-то потоком
     *
     */
    boolean isLocked();

}
