package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

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
            int capacity = enlargeTo(items.length * 2);
            nextFirst = (capacity - (original_length - nextLast)) - 1;
            items[nextFirst] = item;
            nextFirst -= 1;
            size += 1;
        }
    }

    /** Resize and enlarge the array to the target capacity, finally return the current capacity. */
    public int enlargeTo(int capacity) {
        T[] a = (T[]) new Object[capacity];
        for (int i = 0; i < nextLast; i++) {
            a[i] = items[i];
        }
        for (int i = capacity - (items.length - nextFirst) + 1; i < capacity; i++) {
            a[i] = items[i + items.length - capacity];
        }
        items = a;
        return capacity;
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
            int capacity = enlargeTo(items.length * 2);
            nextFirst = (capacity - (original_length - nextLast)) - 1;
            items[nextLast] = item;
            nextLast += 1;
            size += 1;
        }
    }

    /** Returns true if  deque is empty, false otherwise. */
    public boolean isEmpty () {
        return size == 0;
    }

    /** Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line. */
    public void printDeque () {
        if (size == items.length) {
            for (int i = nextLast; i < items.length; i++) {
                System.out.print(items[i] + " ");
            }
            for (int i = 0; i < nextLast; i++) {
                System.out.print(items[i] + " ");
            }
            System.out.println();
        }
        else {
            if (nextFirst < nextLast) {
                for (int i = nextFirst + 1; i < nextLast; i++) {
                    System.out.print(items[i] + " ");
                }
                System.out.println();
            }
            else if (nextFirst == nextLast) {
                for (int i = nextFirst + 1; i < items.length; i++) {
                    System.out.print(items[i] + " ");
                }
                for (int i = 0; i < nextFirst; i++) {
                    System.out.print(items[i] + " ");
                }
                System.out.println();
            }
            else {
                for (int i = nextFirst + 1; i < items.length; i++) {
                    System.out.print(items[i] + " ");
                }
                for (int i = 0; i < nextLast; i++) {
                    System.out.print(items[i] + " ");
                }
                System.out.println();
            }
        }
    }

    /** Construct a new item list with original elements copied into that new list in order. */
    public void shrinkTo() {
        if (size >= 16) {
            int capacity = 2 * size;
            T[] a = (T[]) new Object[capacity];
            if (nextFirst < nextLast) {
                for (int i = nextFirst + 1; i < nextLast; i++) {
                    a[1 + i - nextFirst - 1] = items[i];
                }
                items = a;
                nextFirst = 0;
                nextLast = 1 + size;
            } else if (nextFirst == nextLast) {
                int old_length = items.length;
                for (int i = nextFirst + 1; i < old_length; i++) {
                    a[1 + i - nextFirst - 1] = items[i];
                }
                for (int i = 0; i < nextFirst; i++) {
                    a[1 + old_length - nextFirst - 1 + i] = items[i];
                }
                items = a;
                nextFirst = 0;
                nextLast = 1 + size;

            } else {
                int old_length = items.length;
                for (int i = nextFirst + 1; i < old_length; i++) {
                    a[1 + i - nextFirst - 1] = items[i];
                }
                for (int i = 0; i < nextLast; i++) {
                    a[1 + old_length - nextFirst - 1 + i] = items[i];
                }
                items = a;
                nextFirst = 0;
                nextLast = 1 + size;
            }
        }
    }


    /** Removes and returns the item at the front of the deque. If no such item exists, returns null. */
    public T removeFirst() {
        if (items.length / 4 >= size - 1 && size >= 16) {
            shrinkTo();
            T value = items[nextFirst + 1];
            items[nextFirst + 1] = null;
            size -= 1;
            nextFirst += 1;
            return value;
        }
        if (size == 0) {
            return null;
        }
        int index;
        if (nextFirst + 1 >= items.length) {
            index = 0;
        } else {
            index = nextFirst + 1;
        }
        T value = items[index];
        items[index] = null;
        size -= 1;
        nextFirst = (nextFirst + 1) % items.length;
        return value;
    }

    /** Removes and returns the item at the back of the deque. If no such item exists, returns null. */
    public T removeLast (){
        if (items.length / 4 >= size - 1 && size >= 16) {
            shrinkTo();
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

    /** Gets the item at the given index, where O is the front, 1 is the next item, and so forth.
     * If no such item exists, returns null. */
    public T get (int index) {
        int value = (nextFirst + 1) % items.length;
        for (int i = 0; i < index; i++) {
            value = (value + 1) % items.length;
        }
        return items[value];
    }
}
