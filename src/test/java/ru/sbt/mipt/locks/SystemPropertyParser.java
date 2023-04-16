package ru.sbt.mipt.locks;

import ru.sbt.mipt.locks.impl.TASLock;
import ru.sbt.mipt.locks.impl.TTASLock;

import java.util.Arrays;
import java.util.List;

public class SystemPropertyParser {
    private static final String LOCK_TYPE_PROP_NAME = "lockType";
    private static final List<SpinLock> LOCK_TYPES_LIST = Arrays.asList(
            new TASLock(),
            new TTASLock());

    public List<SpinLock> parseLockType() {
        // get property value from system properties
        String lockType = System.getProperty(LOCK_TYPE_PROP_NAME);

        // all lock types
        if (lockType.equals("")) {
            return LOCK_TYPES_LIST;
        }

        // return specific lock
        List<SpinLock> result = LOCK_TYPES_LIST.stream()
                .filter(l -> l.getClass().getSimpleName().equals(lockType))
                .toList();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("lock type '" + lockType + "' is not defined");
        }
        return result;
    }
}
