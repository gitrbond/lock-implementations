package ru.sbt.mipt.locks.util;

import ru.sbt.mipt.locks.BenchmarkOptions;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.impl.TASLock;
import ru.sbt.mipt.locks.impl.TTASLock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SystemPropertyParser {
    private static final String LOCK_TYPE_PROP = "lockType";
    private static final String BENCH_THREAD_NUM_PROP = "nThreads";
    private static final String BENCH_WARMUP_ITER_PROP = "warmupIters";
    private static final String BENCH_WARMUP_TASKS_PROP = "warmupTasks";
    private static final String BENCH_MEASURE_ITER_PROP = "measureIters";
    private static final String BENCH_MEASURE_TASKS_PROP = "measureTasks";
    private static final List<SpinLock> LOCK_TYPES_LIST = LockTypes.LOCK_LIST;
    private static final SpinLock DEFAULT_LOCK = new TASLock();

    public static SpinLock parseLockType() {
        // get property value from system properties
        String lockType = System.getProperty(LOCK_TYPE_PROP);

        System.out.println("lockType = " + lockType);

        // default lock type
        if (lockType.equals("")) {
            return DEFAULT_LOCK;
        }

        // return specific lock
        return LOCK_TYPES_LIST.stream()
                .filter(l -> l.getClass().getSimpleName().equals(lockType)).findAny().orElseThrow(
                        () -> new IllegalArgumentException("lock type '" + lockType + "' is not defined"));
    }

    // gets options, overrides its fields with ones from sysProperties and returns new exemplar
    public static BenchmarkOptions parseBenchmarkOptions(BenchmarkOptions current) {
        String pNThreads = System.getProperty(BENCH_THREAD_NUM_PROP);
        String pWarmupIterations = System.getProperty(BENCH_WARMUP_ITER_PROP);
        String pNWarmupTotalTasks = System.getProperty(BENCH_WARMUP_TASKS_PROP);
        String pMeasureIterations = System.getProperty(BENCH_MEASURE_ITER_PROP);
        String pNMeasureTotalTasks = System.getProperty(BENCH_MEASURE_TASKS_PROP);

        int nThreads = pNThreads.equals("") ? current.nThreads() : Integer.parseInt(pNThreads);
        long warmupIterations = pWarmupIterations.equals("")
                ? current.warmupIterations() : Long.parseLong(pWarmupIterations);
        long nWarmupTotalTasks = pNWarmupTotalTasks.equals("")
                ? current.nWarmupTotalTasks() : Long.parseLong(pNWarmupTotalTasks);
        long measureIterations = pMeasureIterations.equals("")
                ? current.measureIterations() : Long.parseLong(pMeasureIterations);
        long nMeasureTotalTasks = pNMeasureTotalTasks.equals("")
                ? current.nWarmupTotalTasks() : Long.parseLong(pNMeasureTotalTasks);

        return new BenchmarkOptions(nThreads,
                warmupIterations,
                nWarmupTotalTasks,
                measureIterations,
                nMeasureTotalTasks);
    }
}
