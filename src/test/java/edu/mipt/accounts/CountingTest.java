package edu.mipt.accounts;

//import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.TASLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountingTest {
    @Getter
    @Setter
    private class Counter {
        private long count;
        private static SpinLock lock;

        public Counter(long count, SpinLock lock) {
            this.count = count;
            Counter.lock = lock;
        }

        public void increment(long value) {
            lock.lock();
            try {
                count += value;
            } finally {
                lock.unlock();
            }
        }
    }

    private record CounterIncrementOperation(Counter counter, long amount) {
    }

    @Test
    public void parallelTransfer() {
        //given
        SpinLock lock = new TASLock();
        Counter counter = new Counter(100, lock);
        int availableProcessors = getRuntime().availableProcessors();
        ExecutorService executorServiceExecutors = newFixedThreadPool(availableProcessors);
        List<CounterIncrementOperation> operations = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            operations.add(new CounterIncrementOperation(counter, 1));
            operations.add(new CounterIncrementOperation(counter, -1));
        }
        //when
        executeOperations(operations, executorServiceExecutors);
        //then
        assertEquals(100, counter.getCount());
    }

    private void executeOperations(List<CounterIncrementOperation> counterIncrementOperations, ExecutorService executorService) {
        counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter.increment(operation.amount), executorService));
    }
}
