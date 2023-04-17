package ru.sbt.mipt.locks;

public record BenchmarkOptions(int nThreads,
                               long warmupIterations,
                               long nWarmupTotalTasks,
                               long measureIterations,
                               long nMeasureTotalTasks) {
}
