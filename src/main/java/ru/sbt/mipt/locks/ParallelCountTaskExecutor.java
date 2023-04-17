package ru.sbt.mipt.locks;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.runAsync;

@AllArgsConstructor
public class ParallelCountTaskExecutor {
    private final ExecutorService executorService;

    public List<CompletableFuture<Void>> executeCountOperations(List<CounterIncrementOperation> counterIncrementOperations) {
        return counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter().addAndReturnNewValue(operation.amount()), executorService))
                .toList();
    }

    public static List<CounterIncrementOperation> createCounterOperations(SimpleCounter counter, long nOperations, long amount) {
        List<CounterIncrementOperation> operations = new ArrayList<>();
        for (int i = 0; i < nOperations; i++) {
            operations.add(new CounterIncrementOperation(counter, amount));
        }
        return operations;
    }
}
