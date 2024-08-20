package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    /** Returns the maximum element in the deque as governed by the previously given Comparator.
     * If the MaxArrayDeque is empty, returns null. */
    public T max() {
        if (size() == 0) {
            return null;
        }
        T maximum = get(0);
        for (int i = 1; i < size(); i++) {
            if (comparator.compare(get(i), maximum) > 0) {
                maximum = get(i);
            }
        }
        return maximum;
    }

    /** returns the maximum element in the deque as governed by the parameter Comparator c.
     * If the MaxArrayDeque is empty, returns null. */
    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T maximum = get(0);
        for (int i = 1; i < size(); i++) {
            if (c.compare(get(i), maximum) > 0) {
                maximum = get(i);
            }
        }
        return maximum;
    }
}
