package ru.sbt.mipt.locks;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.runAsync;

@AllArgsConstructor
public class ParallelCountTaskExecutor {
    private final ExecutorService executorService;

    public void executeCountOperations(List<CounterIncrementOperation> counterIncrementOperations) {
        counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter().addAndReturnNewValue(operation.amount()), executorService));
    }
}
