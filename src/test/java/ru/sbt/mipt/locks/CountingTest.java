package ru.sbt.mipt.locks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.List;

import static ru.sbt.mipt.locks.util.SystemPropertyParser.parseBenchmarkOptions;

public class CountingTest {
    // default benchmark parameters
    private static final BenchmarkOptions defaultOptions = new BenchmarkOptions(
            5,          // nThreads
            7,          // warmupIterations
            5000,          // warmupMillisecs
            3,          // measureIterations
            5000);          // measureMillisecs
    private static BenchmarkOptions options;

//    volatile int cnt;

    // before executing any tests, read benchmark options from systemProperties
    // they can be set as 'gradle test -D<option>=<value>'
    @BeforeAll
    public static void setup() {
        options = parseBenchmarkOptions(defaultOptions);
    }

    private void benchmarkAndTest(SpinLock lock) {
        SimpleCounter counter = new SimpleCounter(0, lock);

        try {
            long avgThroughput = 0;

            // warmup
            for (int warmupIter = 1; warmupIter <= options.warmupIterations(); warmupIter++) {
                avgThroughput += getOperationPerSec(counter, options.warmupMillisecs());
            }

            // measure
            avgThroughput = 0;
            for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
                avgThroughput += getOperationPerSec(counter, options.measureMillisecs());
            }

            avgThroughput /= options.measureIterations();

            System.out.println("$");
            System.out.println("Benchmark results for " + lock.getClass().getSimpleName() + ":");
            System.out.println(options);
            System.out.println("avgThroughput = " + avgThroughput + " op/sec");
        } catch (InterruptedException e) {
            System.out.println("failed to benchmark " + lock.getClass().getSimpleName() + " due to error: " + e.getMessage());
        }
    }

    public long getOperationPerSec(SimpleCounter counter, long testTimeMillis) throws InterruptedException {
        counter.getLock().lock();

        for (int i = 0; i < options.nThreads(); i++) {
            int threadInd = i;
            Thread t = new Thread(() -> threadRunner(counter, threadInd));

            t.start();
        }

        // sleep to wait until all threads start
        Thread.sleep(500);
        counter.getLock().unlock();
        // all the threads start counting

        Thread.sleep(testTimeMillis); // 5 sec
        long resultCount = counter.getCount();
        System.out.println("counter = " + resultCount);
        Thread.sleep(15_000); // 15 sec
        return resultCount * 1000 / testTimeMillis; // op/sec
    }

    private void threadRunner(SimpleCounter counter, int threadInd) {
        System.out.println("thread " + threadInd + " started");

        long internalCnt = 0;
        for (int iter = 0; iter < 10_000_000; iter++) {
            internalCnt = counter.addAndReturnNewValue(1);
        }

        System.out.println("thread " + threadInd + " finished execution with counter = " + internalCnt);
    }

    @Test
    public void TASLockTest() {
        benchmarkAndTest(new TASLock());
    }

    @Test
    public void TTASLockTest() {
        benchmarkAndTest(new TTASLock());
    }

    @Test
    public void BackoffLockTest() {
        benchmarkAndTest(new BackoffLock());
    }

    @Test
    public void CLHLockTest() {
        benchmarkAndTest(new CLHLock());
    }

    @Test
    public void MCSLockTest() {
        benchmarkAndTest(new MCSLock());
    }

    // Фиксация некорректной параллельной обработки без использования синхронизации.
    // Тест должен падать при 1 потоке или если totalOperations достаточно мало (~единицы тысяч).
    @Disabled
    @Test
    public void NoLockTest() {
        benchmarkAndTest(new NoLock());
    }

    @Disabled
    @Test
    public void AllLocksTest() {
        List<SpinLock> locks = LockTypes.LOCK_LIST;
        locks.forEach(this::benchmarkAndTest);
    }

}
