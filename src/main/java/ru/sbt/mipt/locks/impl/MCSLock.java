package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicReference;


public class MCSLock implements SpinLock {

    class Node {
        volatile boolean locked = false;
        volatile Node next = null;
    }

    AtomicReference<Node> tail;
    ThreadLocal<Node> node;

    public MCSLock() {
        tail = new AtomicReference<Node>(null);
        node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };
    }

    public void lock() {
        Node qnode = node.get();
        Node pred = tail.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode;
            while (qnode.locked) {
                System.out.print("");
            }
        }
    }

    public void unlock() {
        Node qnode = node.get();
        if (qnode.next == null) {
            if (tail.compareAndSet(qnode, null))
                return;
            while (qnode.next == null) {
            }
        }
        qnode.next.locked = false;
        qnode.next = null;
    }

    public boolean isLocked() {
        Node tail = this.tail.get();
        if (tail == null)
            return false;
        else
            return true;
    }

}