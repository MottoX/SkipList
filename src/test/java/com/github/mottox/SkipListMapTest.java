package com.github.mottox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link SkipListMap}
 *
 * @author Robin Wang
 */
public class SkipListMapTest {

    private SortedMap<Integer, String> map;

    @Before
    public void setUp() throws Exception {
        map = new SkipListMap<>();
    }

    @Test
    public void testPut() throws Exception {
        map.put(1, "a");
        map.put(2, "b");
        Assert.assertEquals("a", map.get(1));
        Assert.assertEquals("b", map.get(2));
        map.put(1, "c");
        Assert.assertEquals("c", map.get(1));
    }

    @Test
    public void testRemove() throws Exception {
        map.put(1, "a");
        map.put(2, "b");
        Assert.assertEquals("a", map.get(1));

        Assert.assertEquals("a", map.remove(1));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("b", map.remove(2));
        Assert.assertEquals(null, map.remove(2));
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testClear() throws Exception {
        for (int i = 0; i < 10000; i++) {
            map.put(i, String.valueOf(i));
        }
        Assert.assertEquals(10000, map.size());
        map.clear();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testSize() throws Exception {
        Assert.assertEquals(0, map.size());
        for (int i = 1; i <= 10000; i++) {
            map.put(i, null);
            Assert.assertEquals(i, map.size());
        }

        for (int i = 10000; i >= 1; i--) {
            map.remove(i);
            Assert.assertEquals(i - 1, map.size());
        }
    }

    @Test
    public void testContainsKey() throws Exception {
        for (int i = 0; i < 10000; i++) {
            map.put(i, null);
            Assert.assertEquals(true, map.containsKey(i));
        }

        for (int i = 0; i < 10000; i++) {
            map.remove(i, null);
            Assert.assertEquals(false, map.containsKey(i));
        }
    }

    @Test
    public void testEntrySet() throws Exception {
        Random random = new Random();
        List<Integer> randoms = new ArrayList<>(10000);
        for (int i = 0; i < 10; i++) {
            int r = random.nextInt();
            randoms.add(r);
            map.put(r, "test" + r);
        }
        Collections.sort(randoms);

        int c = 0;
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            Assert.assertEquals(randoms.get(c++), entry.getKey());
        }
    }

    @Test
    public void testKeySet() throws Exception {
        Random random = new Random();
        List<Integer> randoms = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            int r = random.nextInt();
            randoms.add(r);
            map.put(r, null);
        }
        Collections.sort(randoms);

        int c = 0;
        for (Integer x : map.keySet()) {
            Assert.assertEquals(randoms.get(c++), x);
        }
    }

    @Test
    public void testValues() throws Exception {
        Random random = new Random();
        List<Integer> randoms = new ArrayList<>(10000);
        for (int i = 0; i < 10; i++) {
            int r = random.nextInt();
            randoms.add(r);
            map.put(r, "test" + r);
        }
        Collections.sort(randoms);

        int c = 0;
        for (String s : map.values()) {
            Assert.assertEquals("test" + randoms.get(c++), s);
        }
    }

    @Test
    public void testComparator() throws Exception {
        map = new SkipListMap<>(Comparator.<Integer>naturalOrder().reversed());
        for (int i = 0; i < 10000; i++) {
            map.put(i, null);
        }
        int c = 10000;
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            Assert.assertEquals(--c, (int) entry.getKey());
        }
    }

    @Test
    public void testSubMap() throws Exception {
        map.put(2, "2");
        map.put(-5, "-5");
        map.put(12, "12");
        map.put(-22, "-22");
        map.put(7, "7");
        map.put(10, "10");
        map.put(11, "11");
        map.put(32, "32");

        SortedMap<Integer, String> subMap = map.subMap(2, 11);

        Assert.assertEquals(3, subMap.size());
        Assert.assertTrue(subMap.containsKey(2));
        Assert.assertTrue(subMap.containsKey(7));
        Assert.assertTrue(subMap.containsKey(10));

        Assert.assertEquals(2, (int) subMap.firstKey());
        Assert.assertEquals(10, (int) subMap.lastKey());

        SortedMap<Integer, String> subSubMap = subMap.subMap(3, 10);
        Assert.assertEquals(1, subSubMap.size());
        Assert.assertTrue(subMap.containsKey(7));
        Assert.assertEquals(7, (int) subSubMap.firstKey());
        Assert.assertEquals(7, (int) subSubMap.lastKey());
    }

    @Test
    public void testHeadMap() throws Exception {
        for (int i = 0; i < 100; i++) {
            map.put(i, String.valueOf(i));
        }
        SortedMap<Integer, String> headMap = map.headMap(74);
        Assert.assertEquals(74, headMap.size());
        for (int i = 0; i < 74; i++) {
            Assert.assertTrue(headMap.containsKey(i));
        }
        Assert.assertFalse(headMap.containsKey(74));

        SortedMap<Integer, String> headHeadMap = headMap.headMap(53);
        Assert.assertEquals(53, headHeadMap.size());
        for (int i = 0; i < 53; i++) {
            Assert.assertTrue(headHeadMap.containsKey(i));
        }
        Assert.assertFalse(headHeadMap.containsKey(53));

    }

    @Test
    public void testTailMap() throws Exception {
        for (int i = 0; i < 100; i++) {
            map.put(i, String.valueOf(i));
        }
        SortedMap<Integer, String> tailMap = map.tailMap(21);
        Assert.assertEquals(79, tailMap.size());
        for (int i = 21; i < 100; i++) {
            Assert.assertTrue(tailMap.containsKey(i));
        }

        SortedMap<Integer, String> tailTailMap = tailMap.tailMap(59);
        Assert.assertEquals(41, tailTailMap.size());
        for (int i = 59; i < 100; i++) {
            Assert.assertTrue(tailTailMap.containsKey(i));
        }

    }
}