package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private class ArrayDequeIterator implements Iterator<T> {
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

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    public ArrayDeque(T item) {
        items = (T[]) new Object[8];
        items[3] = item;
        size = 1;
        nextFirst = 2;
        nextLast = 4;
    }

    /** Adds an item of type T to the beginning of the deque, assuming that item isn't null. */
    public void addFirst (T item) {
        if (size < items.length) {
            items[nextFirst] = item;
            size += 1;
            if (nextFirst - 1 < 0) {
                nextFirst = items.length - 1;
            } else {
                nextFirst -= 1;
            }
        } else {
            int original_length = items.length;
            enlargeTo(items.length * 2);
            items[nextFirst] = item;
            nextFirst = items.length - 1;
            size += 1;
        }
    }

    /** Resize and enlarge the array to the target capacity, finally return the current capacity. */
    public void enlargeTo(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int index = (nextFirst + 1) % items.length;
        for (int i = 1; i < size + 1; i++) {
            a[i] = items[index];
            index = (index + 1) % items.length;
        }
        items = a;
        nextFirst = 0;
        nextLast = size + 1;
    }

    /** Adds an item of type T to the back of the deque, assuming that item isn't null. */
    public void addLast (T item) {
        if (size < items.length) {
            items[nextLast] = item;
            size += 1;
            if (nextLast + 1 >= items.length) {
                nextLast = 0;
            } else {
                nextLast += 1;
            }
        } else {
            int original_length = items.length;
            enlargeTo(items.length * 2);
            items[nextLast] = item;
            nextLast += 1;
            size += 1;
        }
    }

    /** Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line. */
    public void printDeque () {
        int index = (nextFirst + 1) % items.length;
        while (index != nextLast) {
            System.out.print(items[index] + " ");
            index = (index + 1) % items.length;
        }
        System.out.println();
    }

    /** Construct a new item list with original elements copied into that new list in order. */
    public void shrink() {
        if (size >= 16) {
            int capacity = size * 2;
            T[] a = (T[]) new Object[capacity];
            int i = 1;
            int index = (nextFirst + 1) % items.length;
            while (index != nextLast) {
                a[i] = items[index];
                index = (index + 1) % items.length;
                i += 1;
            }
            items = a;
            nextFirst = 0;
            nextLast = size + 1;
        }
    }


    /** Removes and returns the item at the front of the deque. If no such item exists, returns null. */
    public T removeFirst() {
        if (items.length / 4 >= size - 1 && size >= 16) {
            shrink();
            T value = items[nextFirst + 1];
            items[nextFirst + 1] = null;
            size -= 1;
            nextFirst += 1;
            return value;
        }
        if (size == 0) {
            return null;
        }
        int index = (nextFirst + 1) % items.length;
        T value = items[index];
        items[index] = null;
        size -= 1;
        nextFirst = (nextFirst + 1) % items.length;
        return value;
    }

    /** Removes and returns the item at the back of the deque. If no such item exists, returns null. */
    public T removeLast (){
        if (items.length / 4 >= size - 1 && size >= 16) {
            shrink();
            T value = items[nextLast - 1];
            items[nextFirst + 1] = null;
            size -= 1;
            nextLast -= 1;
            return value;
        }
        if (size == 0) {
            return null;
        }
        int index;
        if (nextLast - 1 < 0) {
            index = items.length - 1;
        } else {
            index = nextLast - 1;
        }
        T value = items[index];
        items[index] = null;
        size -= 1;
        if (nextLast - 1 < 0) {
            nextLast = items.length - 1;
        } else {
            nextLast = nextLast - 1;
        }
        return value;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    /** Gets the item at the given index, where O is the front, 1 is the next item, and so forth.
     * If no such item exists, returns null. */
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return items[(nextFirst + 1 + index) % items.length];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Deque) {
            Deque<T> target = (Deque<T>) o;
            if (target.size() != size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!target.get(i).equals(this.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
