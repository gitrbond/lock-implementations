package ru.sbt.mipt.locks.util;

import ru.sbt.mipt.locks.BenchmarkOptions;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.impl.TASLock;

import java.util.List;

public class SystemPropertyParser {
    private static final String LOCK_TYPE_PROP = "lockType";
    private static final String BENCH_THREAD_NUM_PROP = "nThreads";
    private static final String BENCH_WARMUP_ITER_PROP = "warmupIters";
    private static final String BENCH_WARMUP_TIME_PROP = "warmupMillisecs";
    private static final String BENCH_MEASURE_ITER_PROP = "measureIters";
    private static final String BENCH_MEASURE_TIME_PROP = "measureMillisecs";
    private static final List<SpinLock> LOCK_TYPES_LIST = LockTypes.LOCK_LIST;
    private static final SpinLock DEFAULT_LOCK = new TASLock();

    public static SpinLock parseLockType() {
        // get property value from system properties
        String lockType = System.getProperty(LOCK_TYPE_PROP);
//        System.out.println("lockType = " + lockType);

        // default lock type
        if (lockType.equals("")) {
            return DEFAULT_LOCK;
        }

        // return specific lock
        return LOCK_TYPES_LIST.stream()
                .filter(l -> l.getClass().getSimpleName().equals(lockType)).findAny().orElseThrow(
                        () -> new IllegalArgumentException("lock type '" + lockType + "' is unknown"));
    }

    // gets options, overrides its fields with ones from sysProperties and returns new exemplar
    public static BenchmarkOptions parseBenchmarkOptions(BenchmarkOptions current) {
        String pNThreads = System.getProperty(BENCH_THREAD_NUM_PROP);
        String pWarmupIterations = System.getProperty(BENCH_WARMUP_ITER_PROP);
        String pWarmupMillisecs = System.getProperty(BENCH_WARMUP_TIME_PROP);
        String pMeasureIterations = System.getProperty(BENCH_MEASURE_ITER_PROP);
        String pMeasureMillisecs = System.getProperty(BENCH_MEASURE_TIME_PROP);

//        int nThreads = pNThreads == null ? current.nThreads() : 0 ;//Integer.parseInt(pNThreads);
        long warmupIterations = pWarmupIterations == null ? current.warmupIterations() : Long.parseLong(pWarmupIterations); // "".equals(pWarmupIterations)
        long nWarmupTotalTasks = pWarmupMillisecs == null ? current.warmupMillisecs() : Long.parseLong(pWarmupMillisecs);
        long measureIterations = pMeasureIterations == null ? current.measureIterations() : Long.parseLong(pMeasureIterations);
        long nMeasureTotalTasks = pMeasureMillisecs == null ? current.measureMillisecs() : Long.parseLong(pMeasureMillisecs);

        return new BenchmarkOptions(pNThreads,
                warmupIterations,
                nWarmupTotalTasks,
                measureIterations,
                nMeasureTotalTasks);
    }
}
