package ru.sbt.mipt.locks.benchmark;

import org.openjdk.jmh.annotations.*;
import ru.sbt.mipt.locks.CounterIncrementOperation;
import ru.sbt.mipt.locks.ParallelCountTaskExecutor;
import ru.sbt.mipt.locks.SimpleCounter;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.impl.TASLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertTrue;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class SingleLockBenchmark {

//    private static final int FORK_COUNT = 2;
//    private static final int WARMUP_COUNT = 10;
//    private static final int ITERATION_COUNT = 10;
//    private static final int THREAD_COUNT = 2;
    SpinLock lock = new TASLock();
    SimpleCounter counter = new SimpleCounter(0, lock);


//    @Benchmark
//    public void addAndReturnNewValue(Blackhole bh) {
//        SpinLock lock = new TASLock();
//        SimpleCounter counter = new SimpleCounter(0, lock);
//        bh.consume(counter.addAndReturnNewValue(1));
//    }

    @Benchmark
    public long addAndReturnNewValue() {
        return counter.addAndReturnNewValue(1);
    }

//    @Benchmark


//    public static void main(String[] args) {
//
//    }
}
