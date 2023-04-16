package edu.mipt.accounts;

//import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import ru.sbt.mipt.locks.SpinLock;
import ru.sbt.mipt.locks.TASLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
//import static org.junit.Assert.assertEquals;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountingTest {
    @Getter
    @Setter
    private class Account {
        private long id;
        private long balance;
        private static SpinLock lock;

        public Account(long id, long balance, SpinLock lock) {
            this.id = id;
            this.balance = balance;
            this.lock = lock;
        }

        private void put(long value) {
            balance += value;
        }

        private void take(long value) {
            balance -= value;
        }

        public static void transfer(Account fromAccount, Account toAccount, long amount) {
            lock.lock();
            try {
                fromAccount.take(amount);
                toAccount.put(amount);
            } finally {
                lock.unlock();
            }
        }
    }

    private record Transfer(Account from, Account to, long amount) {
    }

    @Test
    public void parallelTransfer() {
        //given
        SpinLock lock = new TASLock();
        Account firstAccount = new Account(1, 100, lock);
        Account secondAccount = new Account(2, 200, lock);
        List<Transfer> transfers = new ArrayList<>();
        int availableProcessors = getRuntime().availableProcessors();
        ExecutorService executorServiceExecutors = newFixedThreadPool(availableProcessors);
        for (int i = 0; i < 1_000_000; i++) {
            transfers.add(new Transfer(firstAccount, secondAccount, 1));
            transfers.add(new Transfer(secondAccount, firstAccount, 1));
        }
        //when
        executeTransfers(transfers, executorServiceExecutors);
        //then
        assertEquals(100, firstAccount.getBalance());
        assertEquals(200, secondAccount.getBalance());
    }

    private void executeTransfers(List<Transfer> transfers, ExecutorService executorService) {
        transfers.stream()
                .map(transfer -> runAsync(() ->
                        Account.transfer(transfer.from(), transfer.to(), transfer.amount()), executorService));
    }

//    private List<Transfer> createTransfers() {
//        List<Transfer> transfers = new ArrayList<>();
//        for (int i = 0; i < 1_000_000; i++) {
//            transfers.add(new Transfer(1L, 2L, 1));
//            transfers.add(new Transfer(2L, 1L, 1));
//        }
//        return transfers;
//    }
}
