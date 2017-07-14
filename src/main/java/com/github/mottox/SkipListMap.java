package com.github.mottox;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * An implementation of {@link java.util.Map} based on skip lists, a data structure first described in 1989
 * by William Pugh.
 * <p>
 * Note that this implementation is not thread-safe which means it can NOT be used without synchronization.
 *
 * @author Robin Wang
 */
public class SkipListMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, java.io.Serializable {

    private static final long serialVersionUID = -5425203423233739581L;

    /**
     * The maximum level of the skip list.
     */
    private static final int MAX_LEVEL = 32;

    /**
     * The comparator used to maintain order in this skip list map, or
     * null if it uses the natural ordering of its keys.
     */
    private final Comparator<? super K> comparator;

    /**
     * The head of this skip list. This node is a sentinel node.
     */
    private final Node<K, V> head = new Node<>(null, null, MAX_LEVEL);

    /**
     * The tail node of this skip list. This node is a sentinel node like head node.
     */
    private final Node<K, V> tail = new Node<>(null, null, MAX_LEVEL);

    /**
     * Random for determining level of a skip list node.
     */
    private final Random random = new Random();

    /**
     * The current level of this skip list map.
     */
    private int level;

    /**
     * The size of this skip list map. This value should be changed after any modification like insertion, removal, etc.
     */
    private int size;

    /**
     * The entry set view of this skip list map.
     */
    private transient EntrySet entrySet;

    /**
     * The key set view of this skip list map.
     */
    private transient KeySet keySet;

    /**
     * The value collection view of this skip list map.
     */
    private transient Values values;

    /**
     * Constructs a new, empty skip list map, using the natural ordering of its
     * keys. All keys inserted into the map must implement the {@link
     * Comparable} interface. Furthermore, all such keys must be
     * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw
     * a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the map. If the user attempts to put a key into the
     * map that violates this constraint (for example, the user attempts to
     * put a string key into a map whose keys are integers),
     * the {@code put(Object key, Object value)} call will throw a {@code ClassCastException}.
     */
    public SkipListMap() {
        this(null);
    }

    /**
     * Constructs a new, empty skip list map, ordered according to the given
     * comparator.All keys inserted into the map must be <em>mutually
     * comparable</em> by the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys
     * {@code k1} and {@code k2} in the map. If the user attempts to put
     * a key into the map that violates this constraint, the {@code put(Object
     * key, Object value)} call will throw a {@code ClassCastException}.
     *
     * @param comparator the comparator that will be used to order this map.
     *                   If {@code null}, the {@linkplain Comparable natural
     *                   ordering} of the keys will be used.
     */
    public SkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        for (int i = 0; i < MAX_LEVEL; i++) {
            linkNode(head, tail, i);
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Returns <tt>true</tt> if this skip list map includes a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested
     *
     * @return <tt>true</tt> if this map includes a mapping for the specified
     * key
     *
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return findClosestNode((K) key, Relation.EQ) != null;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this skip list map includes no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     *
     * @return the value to which the specified key is mapped, or
     * {@code null} if this map includes no mapping for the key
     *
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Node<K, V> node = findClosestNode((K) key, Relation.EQ);
        return node == null ? null : node.value;
    }

    /**
     * Associates the specified value with the specified key in this skip list map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     *
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with {@code key}.)
     *
     * @throws ClassCastException   if the specified key cannot be compared
     *                              with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *                              and this map uses natural ordering, or its comparator
     *                              does not permit null keys
     */
    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        Node<K, V>[] update = new Node[MAX_LEVEL];

        Node<K, V> node = findClosestNode(key, Relation.EQ, update);
        if (node != null) {
            V oldValue = node.value;
            node.value = value;
            return oldValue;
        }

        // insert a new node to this skip list map
        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level; i < newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        // create a new node and link it with existing nodes
        Node<K, V> newNode = new Node<>(key, value, newLevel);

        for (int i = 0; i < newLevel; i++) {
            // link new node and nodes with greater keys
            linkNode(newNode, update[i].next[i], i);

            // link new node and nodes with less keys
            linkNode(update[i], newNode, i);
        }
        size++;
        return null;
    }

    /**
     * Removes the mapping for a key from this skip list map if it is present
     * (optional operation).
     * Returns the value to which this map previously associated the key,
     * or <tt>null</tt> if the map contained no mapping for the key.
     *
     * @param key key whose mapping is to be removed from the map
     *
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     * @throws NullPointerException if the specified key is null and this
     *                              map does not permit null keys
     */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        Node<K, V> node = findClosestNode((K) key, Relation.EQ);
        if (node != null) {
            deleteNode(node);
            return node.value;
        }
        return null;
    }

    /**
     * Removes all of the mappings from this skip list map.
     * The skip list map will be empty after this call returns.
     */
    @Override
    public void clear() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            Node<K, V> node = head;
            // help GC
            while (node != null) {
                Node<K, V> next = node.next[i];
                node.next[i] = null;
                node = next;
            }
        }
        size = 0;
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this skip list map.
     *
     * @return a set view of the mappings contained in this skip list map,
     * sorted in ascending key order
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet == null ? entrySet = new EntrySet() : entrySet;
    }

    /**
     * Returns a {@link Set} view of keys contained in this skip list map.
     * The result contains all keys in ascending order corresponding to the given comparator
     * or natural order by default.
     *
     * @return a set view of keys contained in this skip list map.
     */
    @Override
    public Set<K> keySet() {
        return keySet == null ? keySet = new KeySet() : keySet;
    }

    /**
     * Returns a collection view of all values contained in this skip list map.
     * The values are given in ascending order corresponding to the given comparator or natural order by default.
     *
     * @return a collection view of values contained in this skip list map.
     */
    @Override
    public Collection<V> values() {
        return values == null ? values = new Values() : values;
    }

    private Node<K, V> firstNode() {
        return dataNodeOrNull(head.next[0]);
    }

    private Node<K, V> lastNode() {
        return dataNodeOrNull(tail.prev[0]);
    }

    @SuppressWarnings("unchecked")
    private int compare(Comparator comparator, Node node, K key) {
        // special judge for head/tail
        if (node == head || node == tail) {
            return node == head ? -1 : 1;
        }
        return (comparator != null) ? comparator.compare(node.key, key) : ((Comparable) node.key).compareTo(key);
    }

    private boolean isDataNode(Node<K, V> node) {
        return node != null && node != head && node != tail;
    }

    private Node<K, V> dataNodeOrNull(Node<K, V> node) {
        return isDataNode(node) ? node : null;
    }

    private boolean checkEquality(Object key, Node<K, V> node) {
        return isDataNode(node) && Objects.equals(node.key, key);
    }

    private void deleteNode(Node<K, V> node) {
        if (node == null) {
            return;
        }

        for (int i = 0; i < node.next.length; i++) {
            linkNode(node.prev[i], node.next[i], i);

            node.prev[i] = null;
            node.next[i] = null;
        }
        while (level > 0 && head.next[level] == null) {
            level--;
        }
        size--;
    }

    private void linkNode(Node<K, V> prevNode, Node<K, V> nextNode, int level) {
        if (prevNode != null) {
            prevNode.next[level] = nextNode;
        }
        if (nextNode != null) {
            nextNode.prev[level] = prevNode;
        }
    }

    private Node<K, V> findClosestNode(K key, Relation relation) {
        return findClosestNode(key, relation, null);
    }

    private Node<K, V> findClosestNode(K key, Relation relation, Node<K, V>[] update) {
        Node<K, V> node = head;
        for (int i = level - 1; i >= 0; i--) {
            while (i < node.next.length
                    && node.next[i] != null
                    && compare(comparator, node.next[i], key) < 0) {
                node = node.next[i];
            }
            if (update != null) {
                update[i] = node;
            }
        }
        if (relation.includes(Relation.GT)) {
            return dataNodeOrNull(node.next[0]);
        }
        if (relation == Relation.LT) {
            return dataNodeOrNull(node);
        }
        if (relation == Relation.EQ) {
            return checkEquality(key, node.next[0]) ? node.next[0] : null;
        }
        // LE
        return checkEquality(key, node.next[0]) ? node.next[0] : dataNodeOrNull(node);
    }

    private int randomLevel() {
        // the following implementation is basically as same as Redis ZSET implementation
        // see https://github.com/antirez/redis/blob/4.0/src/t_zset.c

        int newLevel = 1;
        while ((random.nextInt() & 0xFFFF) < (0xFFFF >> 2)) {
            newLevel++;
        }
        return (newLevel < MAX_LEVEL) ? newLevel : MAX_LEVEL;
    }

    private EntryIterator entryIterator() {
        return new EntryIterator(firstNode());
    }

    private Iterator<K> keyIterator() {
        return new KeyIterator(entryIterator());
    }

    private Iterator<V> valueIterator() {
        return new ValueIterator(entryIterator());
    }

    private class EntryIterator implements Iterator<Entry<K, V>> {
        Node<K, V> nextNode;
        Entry<K, V> prevEntry;

        EntryIterator(Node<K, V> first) {
            this.nextNode = first;
        }

        private Entry<K, V> prevEntry() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            prevEntry = this.nextNode;
            nextNode = this.nextNode.prev[0];

            return prevEntry;
        }

        @Override
        public boolean hasNext() {
            return nextNode != null && nextNode != tail;
        }

        @Override
        public Entry<K, V> next() {
            return nextEntry();
        }

        private Entry<K, V> nextEntry() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            prevEntry = this.nextNode;
            nextNode = this.nextNode.next[0];

            return prevEntry;
        }
    }

    /**
     * Internal skip list node to store key-value pair.
     */
    @SuppressWarnings("unchecked")
    private static final class Node<K, V> implements Entry<K, V> {
        final K key;
        V value;
        final Node<K, V>[] next;
        final Node<K, V>[] prev;

        Node(K key, V value, int level) {
            this.key = key;
            this.value = value;
            this.next = new Node[level];
            this.prev = new Node[level];
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    /**
     * Relation enum for searching nodes.
     */
    private enum Relation {
        EQ(1),
        GT(2),
        GE(EQ.value | GT.value),
        LT(4),
        LE(EQ.value | LT.value);

        private final int value;

        Relation(int value) {
            this.value = value;
        }

        boolean includes(Relation other) {
            return (value & other.value) != 0;
        }
    }

    private class KeyIterator implements Iterator<K> {
        private final EntryIterator entryIterator;

        KeyIterator(EntryIterator entryIterator) {
            this.entryIterator = entryIterator;
        }

        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        @Override
        public K next() {
            return entryIterator.next().getKey();
        }
    }

    private class ValueIterator implements Iterator<V> {
        private final EntryIterator entryIterator;

        ValueIterator(EntryIterator entryIterator) {
            this.entryIterator = entryIterator;
        }

        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        @Override
        public V next() {
            return entryIterator.next().getValue();
        }
    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return entryIterator();
        }

        @Override
        public int size() {
            return SkipListMap.this.size();
        }
    }

    private class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return keyIterator();
        }

        @Override
        public int size() {
            return size;
        }
    }

    private class Values extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return valueIterator();
        }

        @Override
        public int size() {
            return size;
        }
    }

}
