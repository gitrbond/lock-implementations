package ru.sbt.mipt.locks.impl;

import ru.sbt.mipt.locks.SpinLock;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements SpinLock {

    static class Node {
        public boolean locked = false;
    }

    AtomicReference<Node> tail;

    ThreadLocal<Node> node, pred;

    public CLHLock() {
        tail = new AtomicReference<Node>(new Node());
        node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };
        pred = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        Node qnode = node.get();
        qnode.locked = true;
        Node pred = tail.getAndSet(qnode);
        this.pred.set(pred);
        while (pred.locked) {
            System.out.print("");
        }
    }

    public void unlock() {
        Node qnode = node.get();
        qnode.locked = false;
        node.set(pred.get());
    }

    public boolean isLocked() {
        if (tail.get() == null)
            return false;
        else {
            Node current = tail.get();
            if (current.locked == true)
                return true;
            else
                return false;
        }
    }
}