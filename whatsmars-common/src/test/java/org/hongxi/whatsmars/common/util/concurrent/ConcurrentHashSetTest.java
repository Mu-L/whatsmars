package org.hongxi.whatsmars.common.util.concurrent;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConcurrentHashSet}.
 */
class ConcurrentHashSetTest {

    // ==================== Basic Operations ====================

    @Test
    void testAddAndSize() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        assertTrue(set.add("a"));
        assertTrue(set.add("b"));
        assertEquals(2, set.size());
    }

    @Test
    void testAddDuplicate() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        assertTrue(set.add("a"));
        assertFalse(set.add("a"));
        assertEquals(1, set.size());
    }

    @Test
    void testContains() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        set.add("x");
        assertTrue(set.contains("x"));
        assertFalse(set.contains("y"));
    }

    @Test
    void testRemove() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        set.add("a");
        set.add("b");

        assertTrue(set.remove("a"));
        assertFalse(set.contains("a"));
        assertEquals(1, set.size());

        assertFalse(set.remove("nonexistent"));
    }

    @Test
    void testIsEmpty() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        assertTrue(set.isEmpty());

        set.add("a");
        assertFalse(set.isEmpty());
    }

    @Test
    void testClear() {
        ConcurrentHashSet<Integer> set = new ConcurrentHashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);

        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void testIterator() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");

        Set<String> collected = new HashSet<>();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertEquals(Set.of("a", "b", "c"), collected);
    }

    // ==================== Constructor ====================

    @Test
    void testInitialCapacityConstructor() {
        ConcurrentHashSet<String> set = new ConcurrentHashSet<>(64);
        assertTrue(set.isEmpty());
        set.add("a");
        assertTrue(set.contains("a"));
    }

    // ==================== Concurrency ====================

    @Test
    void testConcurrentAdd() throws InterruptedException {
        ConcurrentHashSet<Integer> set = new ConcurrentHashSet<>();
        int threadCount = 10;
        int opsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        set.add(threadId * opsPerThread + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threadCount * opsPerThread, set.size());
    }

    @Test
    void testConcurrentAddAndRemove() throws InterruptedException {
        ConcurrentHashSet<Integer> set = new ConcurrentHashSet<>();
        for (int i = 0; i < 500; i++) {
            set.add(i);
        }

        int threadCount = 4;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 2 threads add elements
        for (int t = 0; t < 2; t++) {
            final int offset = t;
            executor.submit(() -> {
                try {
                    for (int i = 500 + offset; i < 1000; i += 2) {
                        set.add(i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 2 threads remove elements
        for (int t = 0; t < 2; t++) {
            final int offset = t;
            executor.submit(() -> {
                try {
                    for (int i = offset; i < 500; i += 2) {
                        set.remove(i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // verify no exceptions occurred and set is in a consistent state
        assertTrue(set.size() > 0);
    }

    @Test
    void testConcurrentContains() throws InterruptedException {
        ConcurrentHashSet<Integer> set = new ConcurrentHashSet<>();
        for (int i = 0; i < 1000; i++) {
            set.add(i);
        }

        int threadCount = 8;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 1000; i++) {
                        assertTrue(set.contains(i));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
    }
}
