package ru.sbt.mipt.locks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sbt.mipt.locks.ParallelCountTaskExecutor.createCounterOperations;
import static ru.sbt.mipt.locks.util.SystemPropertyParser.parseBenchmarkOptions;

public class CountingTest {
    // default benchmark parameters
    private static final BenchmarkOptions defaultOptions = new BenchmarkOptions(
            5,          // nThreads
            7,          // warmupIterations
            100_000,  // nWarmupTotalTasks
            3,          // measureIterations
            100_000); // nMeasureTotalTasks
    private static BenchmarkOptions options;

    volatile int cnt;

    // before executing any tests, read benchmark options from systemProperties
    // they can be set as 'gradle test -D<option>=<value>'
    @BeforeAll
    public static void setup() {
        options = parseBenchmarkOptions(defaultOptions);
    }

    private void handWrittenLockBenchmarkAndTest(SpinLock lock) {
        SimpleCounter counter = new SimpleCounter(0, lock);
        ExecutorService executorService = newFixedThreadPool(options.nThreads());
        ParallelCountTaskExecutor taskExecutor = new ParallelCountTaskExecutor(executorService);

        long expectedCount = 0;
        List<CounterIncrementOperation> operations;

        // warmup
        for (int warmupIter = 1; warmupIter <= options.warmupIterations(); warmupIter++) {
            // a unique set of operations so JVM cant optimise it
            operations = createCounterOperations(counter, options.nWarmupTotalTasks(), warmupIter);
            taskExecutor.executeCountOperations(operations);

            assertEquals(counter.getCount(), expectedCount += options.nWarmupTotalTasks() * warmupIter);
        }

        // measure
        long avgTimeMillis = 0;
        for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
            // a unique set of operations
            operations = createCounterOperations(counter, options.nMeasureTotalTasks(), -measureIter);

            long tStart = System.currentTimeMillis();
            taskExecutor.executeCountOperations(operations);
            long tEnd = System.currentTimeMillis();
            avgTimeMillis += (tEnd - tStart) / options.measureIterations();

            assertEquals(counter.getCount(), expectedCount -= options.nMeasureTotalTasks() * measureIter);
        }
        System.out.println("$");
        System.out.println("Benchmark results for " + lock.getClass().getSimpleName() + ":");
        System.out.println(options);
        System.out.println("avgTime to execute one task = " +
                avgTimeMillis * 1_000_000 * options.nThreads() / options.nMeasureTotalTasks() + "ns");
    }

    public void simpleConcurrentTest(SpinLock lock) {
        List<Thread> threads = new ArrayList<>();
        // lock the lock so threads don't start immediately
        lock.lock();

//        volatile int cnt;

//        ThreadLocal<Boolean> finished = new ArrayList<new Boolean(false)>();
//        CounterIncrementOperation couter = new CounterIncrementOperation(lock);

        for (int i = 0; i < options.nThreads(); i++) {
            int finalI = i;
            Thread t = new Thread(() -> {
                System.out.println("i am thread" + finalI);
                for (int iter = 0; iter < options.nWarmupTotalTasks(); iter++) {
                    try {
                        lock.lock();
                        cnt += (finalI) % (iter + 1);
                    } finally {
                        lock.unlock();
                    }

                }
            });
            threads.add(t);
            t.start();
        }

        lock.unlock();


    }

    @Test
    public void TASSimpleLockTest() {
        simpleConcurrentTest(new TASLock());
    }

    @Test
    public void TASLockTest() {
        handWrittenLockBenchmarkAndTest(new TASLock());
    }

    @Test
    public void TTASLockTest() {
        handWrittenLockBenchmarkAndTest(new TTASLock());
    }

    @Test
    public void BackoffLockTest() {
        handWrittenLockBenchmarkAndTest(new BackoffLock());
    }

    @Test
    public void CLHLockTest() {
        handWrittenLockBenchmarkAndTest(new CLHLock());
    }

    @Test
    public void MCSLockTest() {
        handWrittenLockBenchmarkAndTest(new MCSLock());
    }

    // Фиксация некорректной параллельной обработки без использования синхронизации.
    // Тест должен падать при 1 потоке или если totalOperations достаточно мало (~единицы тысяч).
    @Disabled
    @Test
    public void NoLockTest() {
        handWrittenLockBenchmarkAndTest(new NoLock());
    }

    @Disabled
    @Test
    public void AllLocksTest() {
        List<SpinLock> locks = LockTypes.LOCK_LIST;
        locks.forEach(this::handWrittenLockBenchmarkAndTest);
    }

}
