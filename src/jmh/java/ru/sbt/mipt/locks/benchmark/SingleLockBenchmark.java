package ru.sbt.mipt.locks.benchmark;

import org.openjdk.jmh.annotations.*;
import ru.sbt.mipt.locks.SimpleCounter;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    SimpleCounter cLHCounter;
    SimpleCounter mCSCounter;

    @Setup
    public void setup() {
        lockMap = LockTypes.LOCK_MAP;

        tASCounter = new SimpleCounter(0, new TASLock());
        tTASCounter = new SimpleCounter(0, new TTASLock());
        backoffCounter = new SimpleCounter(0, new BackoffLock());
        cLHCounter = new SimpleCounter(0, new CLHLock());
        mCSCounter = new SimpleCounter(0, new MCSLock());
    }

//    @Benchmark
//    public void benchmarkTAS(Blackhole bh) {
//        bh.consume(tASCounter.addAndReturnIfAdded(1L));
//    }
//
//    @Benchmark
//    public long benchmarkTTAS() {
//        return tTASCounter.addAndReturnIfAdded(1L);
//    }
//
//    @Benchmark
//    public long benchmarkBackoff() {
//        return backoffCounter.addAndReturnIfAdded(1L);
//    }
//
//    @Benchmark
//    public long benchmarkCLH() {
//        return cLHCounter.addAndReturnIfAdded(1L);
//    }
//
//    @Benchmark
//    public long benchmarkMCS() {
//        return mCSCounter.addAndReturnIfAdded(1L);
//    }
}
