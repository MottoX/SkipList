package com.github.mottox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link SkipListMap}
 *
 * @author Robin Wang
 */
public class SkipListMapTest {

    private Map<Integer, String> map;

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
}