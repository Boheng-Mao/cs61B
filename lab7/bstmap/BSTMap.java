package bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class BSTNode {
        K key;
        V val;
        BSTNode leftnode;
        BSTNode rightnode;

        BSTNode(K key, V val, BSTNode leftnode, BSTNode rightnode) {
            this.key = key;
            this.val = val;
            this.leftnode = leftnode;
            this.rightnode = rightnode;
        }

        /** Goes through the whole structure to look for a node with key K. */
        public BSTNode get(K k)  {
            if (k != null && key.equals(k)) {
                return this;
            } else {
                if (key.compareTo(k) < 0 && this.rightnode != null) {
                    return this.rightnode.get(k);
                }
                else if (key.compareTo(k) > 0 && this.leftnode != null) {
                    return this.leftnode.get(k);
                }
                return null;
            }
        }
    }

    private int size = 0;
    private BSTNode item;

    @Override
    public void clear() {
        size = 0;
        item = null;
    }

    @Override
    public boolean containsKey(K key) {
        if (item == null) {
            return false;
        }
        return item.get(key) != null;
    }

    @Override
    public V get(K key) {
        if (item == null) {
            return null;
        }
        BSTNode node = item.get(key);
        if (node != null) {
            return node.val;
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (item != null) {
            BSTNode node = item.get(key);
            if (node != null) {
                node.val = value;
            } else {
                put(key, value, item);
                size += 1;
            }
        } else {
            item = new BSTNode(key, value, null, null);
            size += 1;
        }
    }

    /** Insert the key-value pair as one of the child
     * recursively starting from the current NODE
     * assuming NODE isn't null. */
    private void put(K key, V value, BSTNode node) {
        if (node.key == key) {
            node.val = value;
        }
        else if (node.key.compareTo(key) < 0 && node.rightnode != null) {
            put(key, value, node.rightnode);
        } else if (node.key.compareTo(key) < 0 && node.rightnode == null) {
            node.rightnode = new BSTNode(key, value, null, null);
        } else if (node.key.compareTo(key) > 0 && node.leftnode != null) {
                put(key, value, node.leftnode);
        } else if (node.key.compareTo(key) > 0 && node.leftnode == null) {
                node.leftnode = new BSTNode(key, value, null, null);
        }
    }

    public void printInOrder() {
        if (item != null) {
            printInOrder(item);
        }
    }

    private void printInOrder(BSTNode node) {
        if (node.leftnode != null) {
            printInOrder(node.leftnode);
        }
        System.out.print(node.val + " ");
        if (node.rightnode != null) {
            printInOrder(node.rightnode);
        }
        System.out.println();
    }


    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }
}
