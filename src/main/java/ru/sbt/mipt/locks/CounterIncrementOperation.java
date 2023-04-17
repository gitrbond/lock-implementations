package ru.sbt.mipt.locks;

public record CounterIncrementOperation(SimpleCounter counter, long amount) {
}
