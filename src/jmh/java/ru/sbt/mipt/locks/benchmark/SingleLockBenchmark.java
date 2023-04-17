package ru.sbt.mipt.locks.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.mipt.locks.*;
import ru.sbt.mipt.locks.impl.TASLock;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
    Map<String, SpinLock> lockMap;
//    Map<String, SimpleCounter> counterMap; // = new SimpleCounter(0, lock);
    SimpleCounter TASCounter;
    SimpleCounter TTASCounter;

    @Setup
    public void setup() {
        lockMap = LockTypes.LOCK_MAP;
        // for every <String name, SpinLock lock> entry creates a <String name, SimpleCounter(0, lock)> entry
//        counterMap = lockMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
//                e -> new SimpleCounter(0, e.getValue())));
        TASCounter = new SimpleCounter(0, lockMap.get("TASLock"));
        TTASCounter = new SimpleCounter(0, lockMap.get("TTASLock"));
    }

    @Benchmark
    public void benchmarkTAS(Blackhole bh) {
        bh.consume(TASCounter.addAndReturnNewValue(1L));
    }

    @Benchmark
    public long benchmarkTTAS() {
        return TTASCounter.addAndReturnNewValue(1L);
    }
}
