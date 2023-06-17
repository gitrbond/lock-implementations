package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicReference;


public class MCSLock implements SpinLock {
    class Node {
        volatile boolean locked = false;
        volatile Node next = null;
    }

    private final AtomicReference<Node> tail;
    private final ThreadLocal<Node> node;

    public MCSLock() {
        tail = new AtomicReference<Node>(null);
        this.node = ThreadLocal.withInitial(() -> new Node());
    }

    public void lock() {
        Node qnode = this.node.get();
        Node pred = this.tail.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode;
            while (qnode.locked) {
                Thread.yield();
            }
        }
    }

    public void unlock() {
        Node qnode = this.node.get();
        if (qnode.next == null) {
            if (this.tail.compareAndSet(qnode, null)) {
                return;
            }
            while (qnode.next == null) {
            }
        }
        qnode.next.locked = false;
        qnode.next = null;
    }

    @Override
    public String toString() {
        return "MCSLock";
    }
}