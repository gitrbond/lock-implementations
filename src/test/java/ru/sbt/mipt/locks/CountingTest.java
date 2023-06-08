package ru.sbt.mipt.locks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.FastBufferedPrinter;
import ru.sbt.mipt.locks.util.LockTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.sbt.mipt.locks.util.SystemPropertyParser.parseBenchmarkOptions;

public class CountingTest {
    // default benchmark parameters
    private static final BenchmarkOptions defaultOptions = new BenchmarkOptions(
            8,          // nThreads
            5,          // warmupIterations
            5000,          // warmupMillisecs
            10,          // measureIterations
            5000);          // measureMillisecs
    private static BenchmarkOptions options;
    // a fast way to print logs
    static FastBufferedPrinter out;


    // before executing any tests, read benchmark options from systemProperties
    // they can be set as 'gradle test -D<option>=<value>'
    @BeforeAll
    public static void setup() {
        options = parseBenchmarkOptions(defaultOptions);
        out = new FastBufferedPrinter();
    }

    private void benchmarkAndTest(SpinLock lock) {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";
        try {
        out.print("Benchmarking " + lockName);
            long avgThroughput = 0;

            // warmup
            for (int warmupIter = 1; warmupIter <= options.warmupIterations(); warmupIter++) {
                out.print(lockName + "warmup iteration " + warmupIter + "...");
                out.flush();
                avgThroughput += getOperationPerSec(lock, options.warmupMillisecs());
            }

            // measure
            avgThroughput = 0;
            for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
                out.print(lockName + "measure iteration " + measureIter + "...");
                out.flush();
                avgThroughput += getOperationPerSec(lock, options.measureMillisecs());
            }

            avgThroughput /= options.measureIterations();

            out.print("$");
            out.print("Benchmark results for " + lock.getClass().getSimpleName() + ":");
            out.print(options.toString());
            out.print("avgThroughput = " + avgThroughput + " op/sec");
            out.print("$");
        } catch (InterruptedException | IOException e) {
            out.print("failed to benchmark " + lock.getClass().getSimpleName() + " due to error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    public long getOperationPerSec(SpinLock lock, long testTimeMillis) throws InterruptedException, IOException {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";

        SimpleCounter counter = new SimpleCounter(0, lock);
        counter.getLock().lock();
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < options.nThreads(); i++) {
            int threadId = i;
            Thread t = new Thread(() -> {
                try {
                    threadRunner(counter, threadId);
                } catch (InterruptedException | IOException e) {
                    // that is intended for BackoffLock to exit with exception as it has sleep() inside
                    out.print("thread " + threadId + " stopped with exception");
                }
            });
            threadList.add(t);

            t.start();
        }

        // sleep to wait until all threads start
        Thread.sleep(500);
        // all the threads now start counting:
        counter.getLock().unlock();

        Thread.sleep(testTimeMillis);
        long resultCount = counter.count;
        out.print(lockName + "counter = " + resultCount);

        // terminate worker-threads
//        out.print(lockName + "waiting for workers to end");
        threadList.forEach(Thread::interrupt);
        for (int i = 0; i < options.nThreads(); i++) {
            while (threadList.get(i).isAlive()) {
            }
        }
//        out.print(lockName + "ended");

        return resultCount * 1000 / testTimeMillis / options.nThreads(); // op/sec
    }

    private void threadRunner(SimpleCounter counter, int threadId) throws InterruptedException, IOException {
        long internalCnt = 0;
        while (!Thread.interrupted()) {
            internalCnt += counter.addAndReturnIfAdded(1);
        }
        out.print("thread " + threadId + " finished after counting to " + internalCnt);
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
