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
            21,          // nThreads
            5,          // warmupIterations
            5000,          // warmupMillisecs
            5,          // measureIterations
            10000);          // measureMillisecs
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

    private void benchmark(SpinLock lock) {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";
        try {
            out.print("Benchmarking " + lock.toString());
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
            out.print("Benchmark results for " + lock.toString() + ":");
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
                    out.print("thread " + threadId + " stopped with exception. " + e.getMessage());
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

        // terminate worker-threads
//        out.print(lockName + "waiting for workers to end");
        threadList.forEach(Thread::interrupt);

        long resultCount = counter.getCount();
        out.print(lockName + "final counter = " + resultCount);

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
            try {
                internalCnt += counter.addAndReturnIfAdded(1);
            } catch (InterruptedException e) {
                throw new InterruptedException("counter = " + internalCnt);
            }
        }
        out.print("thread " + threadId + " finished after counting to " + internalCnt);
    }

    @Test
    public void TASLockTest() {
        benchmark(new TASLock());
    }

    @Test
    public void TTASLockTest() {
        benchmark(new TTASLock());
    }

    @Test
    public void BackoffLockTest() {
        benchmark(new BackoffLock());
    }

    @Test
    public void CustomLockTest() {
        int[] nThreadsArray = new int[]{1, 2, 4, 6, 8, 16, 32, 64};
//        int[] nThreadsArray = new int[]{16};
        BenchmarkOptions[] optionsArray = new BenchmarkOptions[nThreadsArray.length];
        for (int i = 0; i < optionsArray.length; i++) {
            int nThreads = nThreadsArray[i];
            long time = options.measureMillisecs();
            if (nThreads >= 20) {
                time = 20000;
            }
            if (nThreads >= 32) {
                time = 30000;
            }
            if (nThreads >= 64) {
                time = 60000;
            }
            optionsArray[i] = new BenchmarkOptions(nThreads,
                    options.warmupIterations(),
                    options.warmupMillisecs(),
                    options.measureIterations(),
                    time);
        }
        for (BenchmarkOptions benchmarkOptions : optionsArray) {
            options = benchmarkOptions;
//            benchmark(new TASLock());
//            benchmark(new TTASLock());
            benchmark(new BackoffLock(1, 64));
            benchmark(new HBOLock(1, 64));
//            benchmark(new CLHLock());
//            benchmark(new MCSLock());
        }
    }

    @Test
    public void MCSLockTest() {
        benchmark(new MCSLock());
    }

    // Фиксация некорректной параллельной обработки без использования синхронизации.
    // Тест должен падать при 1 потоке или если totalOperations достаточно мало (~единицы тысяч).
    @Disabled
    @Test
    public void NoLockTest() {
        benchmark(new NoLock());
    }

    @Disabled
    @Test
    public void AllLocksTest() {
        List<SpinLock> locks = LockTypes.LOCK_LIST;
        locks.forEach(this::benchmark);
    }

}
