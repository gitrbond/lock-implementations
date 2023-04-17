package ru.sbt.mipt.locks;

import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.util.SystemPropertyParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountingTest {

//    private record CounterIncrementOperation(SimpleCounter counter, long amount) {
//    }

//    @Test
//    public void parallelCountTest() {
//        // given
////        SystemPropertyParser parser = new SystemPropertyParser();
//        List<SpinLock> locks = SystemPropertyParser.parseLockTypes();
//
//        int availableProcessors = getRuntime().availableProcessors();
//        assertTrue(availableProcessors > 1);
//        ExecutorService executorService = newFixedThreadPool(availableProcessors);
//
//        ParallelCountTaskExecutor taskExecutor = new ParallelCountTaskExecutor(executorService);
//
//        locks.forEach(lock -> {
//            SimpleCounter counter = new SimpleCounter(100, lock);
//            List<CounterIncrementOperation> operations = createCounterOperations(counter);
//
//            // when
//            taskExecutor.executeCountOperations(operations);
//
//            // then
//            assertEquals(100, counter.getCount());
//        });
//    }

//    private void parallelCountExecute(SimpleCounter counter, ExecutorService executorService) {
//        //given
////        SimpleCounter counter = new SimpleCounter(100, lock);
//
//
//
//        //when
//
//        //then
//    }

    private static List<CounterIncrementOperation> createCounterOperations(SimpleCounter counter) {
        List<CounterIncrementOperation> operations = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            operations.add(new CounterIncrementOperation(counter, 1));
            operations.add(new CounterIncrementOperation(counter, -1));
        }
        return operations;
    }

//    private void executeOperations(List<CounterIncrementOperation> counterIncrementOperations, ExecutorService executorService) {
//        counterIncrementOperations.stream()
//                .map(operation -> runAsync(() ->
//                        operation.counter().addAndReturnNewValue(operation.amount()), executorService));
//    }
}
