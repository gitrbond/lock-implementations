package ru.sbt.mipt.locks.util;

import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.impl.BackoffLock;
import ru.sbt.mipt.locks.impl.TASLock;
import ru.sbt.mipt.locks.impl.TTASLock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LockTypes {
    public static List<SpinLock> LOCK_LIST= Arrays.asList(
            new TASLock(),
            new TTASLock(),
            new BackoffLock());

    public static Map<String, SpinLock> LOCK_MAP = createLockMap();

    private static Map<String, SpinLock> createLockMap() {
        return LOCK_LIST.stream().collect(Collectors
                .toMap(l -> l.getClass().getSimpleName(), l -> l));
    }
}
