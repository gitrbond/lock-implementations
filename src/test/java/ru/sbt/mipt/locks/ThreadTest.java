package ru.sbt.mipt.locks;

import org.junit.jupiter.api.Test;

public class ThreadTest {
    @Test
    public void testWait() throws InterruptedException {
        Thread t = new Thread(()-> {
//            int i = 0;
//            i++;
//            Thread.sleep(100);
            System.out.println("thread");
        });
        t.start();
        Thread.sleep(5000);
        Thread.currentThread().join();
        System.out.println("end");
    }
}
