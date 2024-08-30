package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private Set<K> keySet = new HashSet<>();
    private int nodeNumber = 0;
    private int tableSize = 16; // the table size(the number of buckets)
    private double loadFactor = 0.75; // the max Load

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(tableSize);

    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
        tableSize = initialSize;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        tableSize = initialSize;
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    @Override
    public void clear() {
        nodeNumber = 0;
        buckets = createTable(tableSize);
        keySet = new HashSet<>();
    }

    @Override
    public boolean containsKey(K key) {
        int index = Math.floorMod(key.hashCode(), tableSize);
        if (buckets[index] != null) {
            for (Node n : buckets[index]) {
                if (key.equals(n.key)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = Math.floorMod(key.hashCode(), tableSize);
        if (buckets[index] != null) {
            for (Node n : buckets[index]) {
                if (n.key.equals(key)) {
                    return n.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
       return nodeNumber;
    }

    @Override
    public void put(K key, V value) {
        if (!containsKey(key)) {
            double factor = (double) (nodeNumber + 1) / tableSize;
            if (factor > loadFactor) {
                resize();
            }
            Node n = createNode(key, value);
            int index = Math.floorMod(key.hashCode(), tableSize);
            buckets[index].add(n);
            keySet.add(n.key);
            nodeNumber += 1;
        }
        else {
            int index = Math.floorMod(key.hashCode(), tableSize);
            for (Node n: buckets[index]) {
                if (n.key.equals(key)) {
                    n.value = value;
                }
            }
        }
    }

    private void resize() {
        Collection<Node>[] a = createTable(tableSize * 2);
        for (int i = 0; i < tableSize; i++) {
            if (buckets[i] != null) {
                for (Node n : buckets[i]) {
                    int newIndex = Math.floorMod(n.key.hashCode(), tableSize * 2);
                    a[newIndex].add(n);
                }
            }
        }
        buckets = a;
        tableSize *= 2;
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new MHMIterator();
    }

    private class MHMIterator implements Iterator {
        private List<Node> lst;

        public MHMIterator() {
            lst = new ArrayList<>();
            for (Collection<Node> items : buckets) {
                for (Node node : items) {
                    lst.add(node);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return lst.size() != 0;
        }

        @Override
        public Object next() {
            return lst.remove(0);
        }
    }

}
