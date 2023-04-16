package ru.sbt.mipt.locks;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.TASLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountingTest {
    @Getter
    @Setter
    private static class Counter {
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
    public void parallelCountTest() {
        SystemPropertyParser parser = new SystemPropertyParser();
        List<SpinLock> locks = parser.parseLockType();

        int availableProcessors = getRuntime().availableProcessors();
        assertTrue(availableProcessors > 1);
        ExecutorService executorServiceExecutors = newFixedThreadPool(availableProcessors);

        locks.forEach(lock -> parallelCountExecute(lock, executorServiceExecutors));
    }

    private void parallelCountExecute(SpinLock lock, ExecutorService executorService) {
        //given
        Counter counter = new Counter(100, lock);

        List<CounterIncrementOperation> operations = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            operations.add(new CounterIncrementOperation(counter, 1));
            operations.add(new CounterIncrementOperation(counter, -1));
        }

        //when
        executeOperations(operations, executorService);

        //then
        assertEquals(100, counter.getCount());
    }

    private void executeOperations(List<CounterIncrementOperation> counterIncrementOperations, ExecutorService executorService) {
        counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter.increment(operation.amount), executorService));
    }
}
