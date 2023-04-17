package ru.sbt.mipt.locks;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.*;

public class CountingTest {
    // Параметры, дефолтные значения
    private final int nThreads = 20; // число потоков в пуле
    private final long totalOperations = 1_000_000;  // общее кол-во операций (инкрементов), которые нужно произвести
    private final long opsPerTask = 2_042; // кол-во операций на 1 таску
    // TODO Читать параметры извне (из файла/аргументов запуска)

    @Getter
    @Setter
    private static class Counter {

        private long count;
        private static SpinLock lock;

        public Counter(long initial, SpinLock lock) {
            this.count = initial;
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

    // Микротаски - по одной операции на таску
    private void executeOperations(List<CounterIncrementOperation> counterIncrementOperations, ExecutorService executorService) {
        counterIncrementOperations.stream()
                .map(operation -> runAsync(() ->
                        operation.counter.increment(operation.amount), executorService));
    }

    private void parallelCountExecute(SpinLock lock, ExecutorService executorService) {
        //given
        Counter counter = new Counter(100, lock);

        List<CounterIncrementOperation> operations = new ArrayList<>();
        for (long i = 0; i < totalOperations; i++) {
            operations.add(new CounterIncrementOperation(counter, 1));
            operations.add(new CounterIncrementOperation(counter, -1));
        }
        //when
        executeOperations(operations, executorService);
        //then
        assertEquals(100, counter.getCount());
    }

    public void oneLockRun(SpinLock lock) {
        Counter counter = new Counter(0, lock);
        ExecutorService executor = newFixedThreadPool(nThreads);
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        long opsLeft = totalOperations; // Сколько осталось выполнить операций
        while (opsLeft > 0) {
            long opsToDo = Math.min(opsLeft, opsPerTask);  // сколько операций выполнить в текущей таске
            futureList.add(runAsync( () -> {
                                                for (long j = 0; j < opsToDo; j++)
                                                    counter.increment(1L);
                                           }
                                    , executor ));
            opsLeft -= opsToDo;
        }
        futureList.forEach(CompletableFuture::join); // Подождем пока все задачи отработают

        if (lock instanceof NoLock) // Фейковый лок проверим отдельно - в норме тут должно быть второе значение меньше
            assertNotEquals(totalOperations, counter.getCount());
        else
            assertEquals(totalOperations, counter.getCount());
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
    public void parallelCountTest() {
//        SystemPropertyParser parser = new SystemPropertyParser();
//        List<SpinLock> locks = parser.parseLockType();
        List<SpinLock> locks = new ArrayList<>();
        locks.add(new TASLock());
        locks.add(new TTASLock());
        locks.add(new BackoffLock());

        int availableProcessors = getRuntime().availableProcessors();
        assertTrue(availableProcessors > 1);
        ExecutorService executorServiceExecutors = newFixedThreadPool(availableProcessors);

        locks.forEach(lock -> parallelCountExecute(lock, executorServiceExecutors));
    }

}
