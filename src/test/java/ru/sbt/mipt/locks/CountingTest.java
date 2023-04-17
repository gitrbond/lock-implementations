package ru.sbt.mipt.locks;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;
import ru.sbt.mipt.locks.util.LockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.*;
import static ru.sbt.mipt.locks.ParallelCountTaskExecutor.createCounterOperations;

public class CountingTest {
    BenchmarkOptions options = new BenchmarkOptions(10,
            3,
            100_000,
            3,
            100_000);
    // TODO Читать параметры извне (из файла/аргументов запуска)

    // Микротаски - по одной операции на таску
    private void executeOperations(List<CounterIncrementOperation> counterIncrementOperations, ExecutorService executorService) {
        counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter().addAndReturnNewValue(operation.amount()), executorService));
    }

//    private void parallelCountExecute(SpinLock lock, ExecutorService executorService) {
//        //given
//        SimpleCounter counter = new SimpleCounter(100, lock);
//
//        List<CounterIncrementOperation> operations = new ArrayList<>();
//        for (long i = 0; i < totalOperations; i++) {
//            operations.add(new CounterIncrementOperation(counter, 1));
//            operations.add(new CounterIncrementOperation(counter, -1));
//        }
//        //when
//        executeOperations(operations, executorService);
//        //then
//        assertEquals(100, counter.getCount());
//    }

    private void oneLockRun(SpinLock lock) {
        SimpleCounter counter = new SimpleCounter(0, lock);
        ExecutorService executorService = newFixedThreadPool(options.nThreads());
        ParallelCountTaskExecutor taskExecutor = new ParallelCountTaskExecutor(executorService);

        long expectedCount = 0;
        List<CounterIncrementOperation> operations;
        List<CompletableFuture<Void>> futures;

        // warmup
        for (int warmupIter = 1; warmupIter <= options.warmupIterations(); warmupIter++) {
            operations = createCounterOperations(counter, options.nWarmupTotalTasks(), warmupIter);
            futures = taskExecutor.executeCountOperations(operations);
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Подождем пока все задачи отработают

            assertEquals(counter.getCount(), expectedCount += options.nWarmupTotalTasks() * warmupIter);
        }

        // measure
        long avgTimeMillis = 0;
        for (int measureIter = 1; measureIter <= options.measureIterations(); measureIter++) {
            operations = createCounterOperations(counter, options.nMeasureTotalTasks(), -measureIter);

            long tStart = System.currentTimeMillis();
            futures = taskExecutor.executeCountOperations(operations);
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Подождем пока все задачи отработают
            long tEnd = System.currentTimeMillis();
            avgTimeMillis += (tEnd - tStart) / options.measureIterations();

            assertEquals(counter.getCount(), expectedCount -= options.nMeasureTotalTasks() * measureIter);
        }
        System.out.println("avgTimeMillis for lock " + lock.getClass().getSimpleName() + " = " + avgTimeMillis);
    }

    @Test
    public void TASLockTest() {
        oneLockRun(new TASLock());
    }

    @Test
    public void TTASLockTest() {
        oneLockRun(new TTASLock());
    }

    @Test
    public void BackoffLockTest() {
        oneLockRun(new BackoffLock());
    }

    // Фиксация некорректной параллельной обработки без использования синхронизации.
    // Тест должен падать при 1 потоке или если totalOperations достаточно мало (~единицы тысяч).
    @Test
    public void NoLockTest() {
        oneLockRun(new NoLock());
    }

    @Test
    public void AllLocksTest() {
        List<SpinLock> locks = LockTypes.LOCK_LIST;
        locks.forEach(lock -> oneLockRun(new NoLock()));
    }

}
