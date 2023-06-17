package ru.sbt.mipt.locks;

public record BenchmarkOptions(String nThreads,
                               long warmupIterations,
                               long warmupMillisecs,
                               long measureIterations,
                               long measureMillisecs) {
}
