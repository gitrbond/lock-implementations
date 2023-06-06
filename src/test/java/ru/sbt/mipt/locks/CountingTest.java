package ru.sbt.mipt.locks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.ArrayList;
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


    // before executing any tests, read benchmark options from systemProperties
    // they can be set as 'gradle test -D<option>=<value>'
    @BeforeAll
    public static void setup() {
        options = parseBenchmarkOptions(defaultOptions);
    }

    private void benchmarkAndTest(SpinLock lock) {
        try {
            String lockName = "[" + lock.getClass().getSimpleName() + "] ";
            long avgThroughput = 0;

            // warmup
            for (int warmupIter = 1; warmupIter <= options.warmupIterations(); warmupIter++) {
                System.out.println(lockName + "warmup iteration " + warmupIter);
                avgThroughput += getOperationPerSec(lock, options.warmupMillisecs());
            }

            // measure
            avgThroughput = 0;
            for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
                System.out.println(lockName + "measure iteration " + measureIter);
                avgThroughput += getOperationPerSec(lock, options.measureMillisecs());
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

    public long getOperationPerSec(SpinLock lock, long testTimeMillis) throws InterruptedException {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";

        SimpleCounter counter = new SimpleCounter(0, lock);
        counter.getLock().lock();
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < options.nThreads(); i++) {
            int threadInd = i;
            Thread t = new Thread(() -> threadRunner(counter, threadInd));
            threadList.add(t);

            t.start();
        }

        // sleep to wait until all threads start
        Thread.sleep(500);
        counter.getLock().unlock();
        // all the threads start counting

        Thread.sleep(testTimeMillis); // 5 sec
        long resultCount = counter.getCount();
        System.out.println(lockName + "counter = " + resultCount);

        // terminate worker-threads
        System.out.println(lockName + "waiting for workers to end");
        threadList.forEach(Thread::interrupt);
        for (int i = 0; i < options.nThreads(); i++) {
            while (threadList.get(i).isAlive()) {}
        }
        System.out.println(lockName + "ended");

        return resultCount * 1000 / testTimeMillis; // op/sec
    }

    private void threadRunner(SimpleCounter counter, int threadInd) {
        String lockName = "[" + counter.getLock().getClass().getSimpleName() + "] ";

        System.out.println(lockName + "thread " + threadInd + " started");

        long internalCnt = 0;
        for (int iter = 0; iter < 10_000_000 && !Thread.interrupted(); iter++) {
            internalCnt = counter.addAndReturnNewValue(1);
        }

        System.out.println(lockName + "thread " + threadInd + " finished execution with counter = " + internalCnt);
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

    //    @Disabled
    @Test
    public void AllLocksTest() {
        List<SpinLock> locks = LockTypes.LOCK_LIST;
        locks.forEach(this::benchmarkAndTest);
    }

}
