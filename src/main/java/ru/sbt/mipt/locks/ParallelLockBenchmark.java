//package ru.sbt.mipt.locks;
//
//import ru.sbt.mipt.locks.impl.TASLock;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//
//import static java.lang.Runtime.getRuntime;
//import static java.util.concurrent.Executors.newFixedThreadPool;
//
//public class ParallelLockBenchmark {
//    SpinLock lock = new TASLock();
//    SimpleCounter counter = new SimpleCounter(0, lock);
//
//    public void runBenchmark(int nProcesses) {
//        int availableProcessors = getRuntime().availableProcessors();
//        if (availableProcessors < nProcesses) {
//            throw new RuntimeException("could not instantiate " + nProcesses +
//                    " as the avaliable number is less");
//        }
//        ExecutorService executorService = newFixedThreadPool(nProcesses);
//
//        long startTS = System.currentTimeMillis();
//        List<CounterIncrementOperation> operations = createCounterOperations(counter, 100_000);
//        ParallelCountTaskExecutor taskExecutor = new ParallelCountTaskExecutor(executorService);
//        taskExecutor.executeCountOperations(operations);
//        long endTS = System.currentTimeMillis();
//        System.out.println("dt = " + (endTS - startTS) + " ms");
//    }
//
//    private List<CounterIncrementOperation> createCounterOperations(SimpleCounter counter, long nOperations) {
//        List<CounterIncrementOperation> operations = new ArrayList<>();
//        for (int i = 0; i < nOperations; i++) {
//            operations.add(new CounterIncrementOperation(counter, 1));
//        }
//        return operations;
//    }
//}
