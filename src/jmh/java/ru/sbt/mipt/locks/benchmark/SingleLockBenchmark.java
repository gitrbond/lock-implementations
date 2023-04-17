package ru.sbt.mipt.locks.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.mipt.locks.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertTrue;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class SingleLockBenchmark {
    Map<String, SpinLock> lockMap;
    SimpleCounter tASCounter;
    SimpleCounter tTASCounter;
    SimpleCounter backoffCounter;

    @Setup
    public void setup() {
        lockMap = LockTypes.LOCK_MAP;

        tASCounter = new SimpleCounter(0, lockMap.get("TASLock"));
        tTASCounter = new SimpleCounter(0, lockMap.get("TTASLock"));
        backoffCounter = new SimpleCounter(0, lockMap.get("BackoffLock"));
    }

    @Benchmark
    public void benchmarkTAS(Blackhole bh) {
        bh.consume(tASCounter.addAndReturnNewValue(1L));
    }

    @Benchmark
    public long benchmarkTTAS() {
        return tTASCounter.addAndReturnNewValue(1L);
    }

    @Benchmark
    public long benchmarkBackoff() {
        return backoffCounter.addAndReturnNewValue(1L);
    }
}
