package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void nonEmptyInstantiationTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>(1);

        assertFalse("Should not be empty", arrayDeque.isEmpty());
        assertEquals("Should have size 1", 1, arrayDeque.size());
    }

    @Test
    public void addTest() {
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();

        assertTrue("Should be empty", arrayDeque.isEmpty());

        arrayDeque.addFirst("front");
        assertEquals("Should have size 1", 1, arrayDeque.size());

        arrayDeque.addLast("middle");
        assertEquals("Should have size 2", 2, arrayDeque.size());

        arrayDeque.addLast("back");
        assertEquals("Should have size 3", 3, arrayDeque.size());

        System.out.println("Printing out deque: ");
        arrayDeque.printDeque();

    }

    @Test
    public void addWithResizingTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }

        assertEquals("Should have size 20", 20, arrayDeque.size());
    }

    @Test
    public void addBigAmountTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        int M = 1000000;

        for (int i = 0; i < M; i++) {
            arrayDeque.addLast(i);
        }

        assertEquals("Should have size 1000000", M, arrayDeque.size());
    }

    @Test
    public void removeTest() {
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();

        arrayDeque.addFirst("front");
        arrayDeque.addLast("middle");
        arrayDeque.addLast("back");

        assertEquals("Should remove last item", "back", arrayDeque.removeLast());
        assertEquals("Should remove first item", "front", arrayDeque.removeFirst());

        assertEquals("Should have size 1", 1, arrayDeque.size());
    }

    @Test
    public void removeWithResizingTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }

        for (int i = 0; i < 20; i++) {
            assertEquals("Should be equal", i, (int) arrayDeque.removeFirst());
        }

        assertTrue("Should be empty", arrayDeque.isEmpty());

        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }

        assertEquals("Should have size 20", 20, arrayDeque.size());
    }

    @Test
    public void getTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }

        for (int i = 0; i < 20; i++) {
            assertEquals("Should be equal", i, (int) arrayDeque.get(i));
        }

        assertNull("Should be null when index out of bound", arrayDeque.get(20));
    }

    @Test
    public void getBigAmountTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        int M = 1000000;

        for (int i = 0; i < M; i++) {
            arrayDeque.addLast(i);
        }

        for (int i = 0; i < M; i++) {
            assertEquals("Should be equal", i, (int) arrayDeque.get(i));
        }
    }

    @Test
    public void testShrinkTo() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }
        arrayDeque.printDeque();
        arrayDeque.shrink();
        arrayDeque.printDeque();
    }

    @Test
    public void removeBigAmountTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        int M = 1000000;

        for (int i = 0; i < M; i++) {
            arrayDeque.addLast(i);
        }

        assertEquals("Should have size 1000000", M, arrayDeque.size());

        for (int i = 0; i < M; i++) {
            assertEquals("Should be equal", i, (int) arrayDeque.removeFirst());
        }

        assertTrue("Should be empty", arrayDeque.isEmpty());
    }


    @Test
    public void basicGet() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        arrayDeque.addLast(0);
        arrayDeque.addLast(1);
        arrayDeque.addFirst(2);
        arrayDeque.addLast(3);
        arrayDeque.addFirst(4);
        arrayDeque.removeLast();
        arrayDeque.addFirst(6);
        arrayDeque.addLast(7);
        arrayDeque.addLast(8);
        arrayDeque.addFirst(9);
        arrayDeque.addFirst(10);
        arrayDeque.get(0);
        arrayDeque.addLast(12);
        arrayDeque.addFirst(13);
        arrayDeque.addFirst(14);
        arrayDeque.removeFirst();
        arrayDeque.removeFirst();
        int value = arrayDeque.get(4);
        assertEquals(2, value);
    }

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        int N = 1000000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 6);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                arrayDeque.addFirst(randVal);
            } else if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 100);
                arrayDeque.addLast(randVal);
            } else if (arrayDeque.size() == 0) {
                assertTrue(arrayDeque.isEmpty());
            } else if (operationNumber == 2) {
                assertTrue(arrayDeque.size() > 0);
            } else if (operationNumber == 3) {
                arrayDeque.removeFirst();
            } else if (operationNumber == 4) {
                arrayDeque.removeLast();
            } else if (operationNumber == 5) {
                int randIndex = StdRandom.uniform(0, arrayDeque.size());
                arrayDeque.get(randIndex);
            }
        }
    }

    @Test
    public void resizingNoNulls() {
        int N = 9;
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        for (int i = 0; i < N; i++) {
            arrayDeque.addFirst(90);
        }
        assertNotNull(arrayDeque.removeFirst());
        assertNotNull(arrayDeque.removeLast());
        assertNotNull(arrayDeque.removeFirst());
        assertNotNull(arrayDeque.removeLast());
        assertNotNull(arrayDeque.removeFirst());
        assertNotNull(arrayDeque.removeLast());
        assertNotNull(arrayDeque.removeFirst());
        assertNotNull(arrayDeque.removeLast());
        assertNotNull(arrayDeque.removeFirst());
    }
}
