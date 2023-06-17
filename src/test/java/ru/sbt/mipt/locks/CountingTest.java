package ru.sbt.mipt.locks;

import org.junit.jupiter.api.BeforeAll;
import ru.sbt.mipt.locks.impl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static ru.sbt.mipt.locks.util.SystemPropertyParser.parseBenchmarkOptions;

public class CountingTest {
    // default benchmark parameters
    private static final BenchmarkOptions defaultOptions = new BenchmarkOptions(
//            5,          // nThreads
//            5,          // warmupIterations
//            5000,          // warmupMillisecs
//            10,          // measureIterations
//            5000);          // measureMillisecs
            "4",          // nThreads
            1,          // warmupIterations
            5000,          // warmupMillisecs
            3,          // measureIterations
            5000);          // measureMillisecs
    private static BenchmarkOptions options;
    private static final boolean verbose = System.getProperty("verbose") != null;


    // before executing any tests, read benchmark options from systemProperties
    // they can be set as 'gradle test -D<option>=<value>'
    @BeforeAll
    public static void setup() {
        options = parseBenchmarkOptions(defaultOptions);
    }

    private static long benchmark(SpinLock lock, Integer nThreads) {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";
        long avgThroughput = 0;
        try {
            for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
                if (verbose) System.out.println(lockName + "measure iteration " + measureIter);
                avgThroughput += getOperationPerSec(lock, nThreads, options.measureMillisecs());
            }
            avgThroughput /= options.measureIterations();

            System.out.println(lockName + nThreads + " threads, average throughput " + avgThroughput + " op/sec");
        } catch (InterruptedException e) {
            System.out.println("failed to benchmark " + lock.getClass().getSimpleName() + " due to error: " + e.getMessage());
        }
        return avgThroughput;
    }

    public static long getOperationPerSec(SpinLock lock, int nThreads, long testTimeMillis) throws InterruptedException {
        String lockName = "[" + lock.getClass().getSimpleName() + "] ";
        SimpleCounter counter = new SimpleCounter(0, lock);
        counter.getLock().lock();
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            int threadId = i;
            Thread t = new Thread(() -> {
                try {
                    threadRunner(counter, threadId);
                } catch (InterruptedException e) {
                    // that is intended for BackoffLock to exit with exception as it has sleep() inside
                    if (verbose)  System.out.println(lockName + "thread " + threadId + " stopped with exception");
                }            });
            threadList.add(t);
            t.start();
        }

        // sleep to wait until all threads start
        Thread.sleep(500);
        // all the threads now start counting:
        counter.getLock().unlock();

        Thread.sleep(testTimeMillis);
        long resultCount = counter.getCount();
        if (verbose) System.out.println(lockName + "counter = " + resultCount);

        // terminate worker-threads
        if (verbose) System.out.println(lockName + "waiting for workers to end");
        threadList.forEach(Thread::interrupt);
        for (int i = 0; i < nThreads; i++) {
//            while (threadList.get(i).isAlive()) { }
            threadList.get(i).join();
        }
        if (verbose) System.out.println(lockName + "ended");
        return resultCount * 1000 / testTimeMillis / nThreads; // op/sec
    }

    private static void threadRunner(SimpleCounter counter, int threadId) throws InterruptedException {
        long internalCnt = 0;
        for (int iter = 0; iter < 1_000_000_000 && !Thread.interrupted(); iter++) {
            internalCnt += counter.addAndReturnIfAdded(1);
        }
        if (verbose) System.out.println("thread " + threadId + " finished after counting to " + internalCnt);
    }

//    Вообще они не нужны теперь, пока не решил что с ними делать..
//    @Test
//    public void TASLockTest() {
//        benchmark(new TASLock());
//    }
//
//    @Test
//    public void TTASLockTest() {
//        benchmark(new TTASLock());
//    }
//
//    @Test
//    public void BackoffLockTest() {
//        benchmark(new BackoffLock());
//    }
//
//    @Test
//    public void CLHLockTest() {
//        benchmark(new CLHLock());
//    }
//
//    @Test
//    public void MCSLockTest() {
//        benchmark(new MCSLock());
//    }
//
//    // Фиксация некорректной параллельной обработки без использования синхронизации.
//    // Тест должен падать при 1 потоке или если totalOperations достаточно мало (~единицы тысяч).
//    @Disabled
//    @Test
//    public void NoLockTest() {
//        benchmark(new NoLock());
//    }
//
//    //    @Disabled
//    @Test
//    public void AllLocksTest() {
//        List<SpinLock> locks = LockTypes.LOCK_LIST;
//        locks.forEach(CountingTest::benchmark);
//    }
    public static void main(String[] args) {
        setup();
        List<SpinLock> lockList = new ArrayList<>();
        // Парсим список локов во входном параметре, напр TAS,TTAS,Backoff;CLH;MCS
        Arrays.asList(System.getProperty("lockTypes").split("[\\s,;]+")).forEach(lockType -> {
            switch (lockType) {
                case "TAS" -> lockList.add(new TASLock());
                case "TTAS" -> lockList.add(new TTASLock());
                case "Backoff" -> lockList.add(new BackoffLock());
                case "CLH" -> lockList.add(new CLHLock());
                case "MCS" -> lockList.add(new MCSLock());
                default -> throw new IllegalArgumentException("Lock type '" + lockType + "' is incorrect, should be subset of TAS, TTAS, Backoff, CLH, MCS");
            }
        });
        // Парсим список количеств потоков, для которых нужно прогнать тесты, напр 1 2, 3 4;8   16
        List<Integer> nThreadsList = Stream.of(System.getProperty("nThreads").split("[\\s,;]+")).map(Integer::parseInt).toList();
        for (SpinLock lock : lockList) {
            String lockName = "[" + lock.getClass().getSimpleName() + "] ";
            System.out.println("\n========> Benchmarking " + lockName);
            System.out.println(options);

            // прогрев JVM
            int warmThreads = Collections.max(nThreadsList);
            System.out.println(lockName + "warming up JVM using " + warmThreads + " threads during " + options.warmupIterations() + " x " + options.warmupMillisecs() + " milliseconds...");
            try {
                for (int i = 0; i < options.warmupIterations(); i++) {
                    getOperationPerSec(lock, warmThreads, options.warmupMillisecs());
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            // бенчмаркинг
            List<Long> throughputsList = new ArrayList<>();
            nThreadsList.forEach(nThreads -> throughputsList.add(benchmark(lock, nThreads)));
            System.out.println(lockName + "Summary results:\n\tThreads = " + nThreadsList + "\n\tThroughputs = " + throughputsList);

        }
    }
}
