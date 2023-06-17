package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements SpinLock {
    private static class Node {
        private volatile boolean locked = false;
    }
    private final AtomicReference<Node> tail;
    private final ThreadLocal<Node> node, pred;
    public CLHLock() {
        tail = new AtomicReference<Node>(new Node());
        this.node = ThreadLocal.withInitial(() -> new Node());
        this.pred = ThreadLocal.withInitial(() -> null);
    }

    public void lock() {
        Node qnode = this.node.get();
        qnode.locked = true;
        Node newPred = this.tail.getAndSet(qnode);
        this.pred.set(newPred);
        while(newPred.locked) {
            Thread.yield();
        }
    }

    public void unlock() {
        Node qnode = this.node.get();
        qnode.locked = false;
        this.node.set(this.pred.get());
    }

    @Override
    public String toString() {
        return "CLHLock";
    }
}