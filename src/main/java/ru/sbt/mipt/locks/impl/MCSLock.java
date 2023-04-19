package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicReference;


public class MCSLock implements SpinLock {

    static class Node {
        boolean locked = false;
        Node next = null;
    }

    AtomicReference<Node> queue;
    ThreadLocal<Node> node;

    public MCSLock() {
        queue = new AtomicReference<Node>(null);
        node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };
    }

    public void lock() {
        Node qnode = node.get();
        Node pred = queue.getAndSet(qnode);
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
            if (queue.compareAndSet(qnode, null))
                return;
            while (qnode.next == null) {
            }
        }
        qnode.next.locked = false;
        qnode.next = null;
    }

    public boolean isLocked() {
        Node tail = queue.get();
        if (tail == null)
            return false;
        else
            return true;
    }

}