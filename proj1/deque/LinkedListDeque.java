package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T>  {
    private class Node {
        private T content;
        private Node next;
        private Node prev;

        Node(T item, Node next, Node prev) {
            this.content = item;
            this.next = next;
            this.prev = prev;
        }
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int pos = 0;
        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T res = get(pos);
            pos += 1;
            return res;
        }
    }

    private final Node sentinel;
    private int size;

    public LinkedListDeque() {
        this.sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    /** Adds an item of type T to the beginning of the deque, assuming that item isn't null. */
    public void addFirst(T item) {
        sentinel.next = new Node(item, sentinel.next, sentinel);
        sentinel.next.next.prev = sentinel.next;
        size += 1;
    }

    /** Adds an item of type T to the back of the deque, assuming that item isn't null. */
    public void addLast(T item) {
        sentinel.prev = new Node(item, sentinel, sentinel.prev);
        sentinel.prev.prev.next = sentinel.prev;
        size += 1;
    }

    /** Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line. */
    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.content + " ");
            p = p.next;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null. */
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        T result = sentinel.next.content;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size -= 1;
        return result;
    }

    /** Removes and returns the item at the back of the deque.
     * If no such item exists, returns null. */
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        T result = sentinel.prev.content;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size -= 1;
        return result;
    }

    /** Gets the item at the given index, where O is the front, 1 is the next item, and so forth.
     * If no such item exists, returns null. */
    public T get(int index) {
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            if (p.next != sentinel) {
                p = p.next;
            } else {
                return null;
            }
        }
        return p.content;
    }

    /** Gets the item at the given index, but uses recursion. */
    public T getRecursive(int index) {
        if (index == 0) {
            return sentinel.next.content;
        } else if (index >= size || index < 0) {
            return null;
        } else {
            return getRecursiveHelper(index, sentinel.next);
        }
    }

    private T getRecursiveHelper(int index, Node currentNode) {
        if (index == 0) {
            return currentNode.content;
        }
        return getRecursiveHelper(index - 1, currentNode.next);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    /** Returns whether the parameter o represents a deque with same contents in the same order. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> lo = (Deque<?>) o;
        if (lo.size() == size) {
            for (int i = 0; i < size; i++) {
                if (!(get(i).equals(lo.get(i)))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
