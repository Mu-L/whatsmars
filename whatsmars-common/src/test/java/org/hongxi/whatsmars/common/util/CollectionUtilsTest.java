package org.hongxi.whatsmars.common.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CollectionUtils 集合操作单元测试
 */
public class CollectionUtilsTest {

    // ==================== isEmpty / isNotEmpty ====================

    @Test
    public void testIsEmpty() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(new ArrayList<>()));
        assertFalse(CollectionUtils.isEmpty(List.of("a")));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(new ArrayList<>()));
        assertTrue(CollectionUtils.isNotEmpty(List.of("a")));
    }

    // ==================== isEmptyMap / isNotEmptyMap ====================

    @Test
    public void testIsEmptyMap() {
        assertTrue(CollectionUtils.isEmptyMap(null));
        assertTrue(CollectionUtils.isEmptyMap(new HashMap<>()));
        assertFalse(CollectionUtils.isEmptyMap(Map.of("k", "v")));
    }

    @Test
    public void testIsNotEmptyMap() {
        assertFalse(CollectionUtils.isNotEmptyMap(null));
        assertFalse(CollectionUtils.isNotEmptyMap(new HashMap<>()));
        assertTrue(CollectionUtils.isNotEmptyMap(Map.of("k", "v")));
    }

    // ==================== sort ====================

    @Test
    public void testSort() {
        List<String> list = new ArrayList<>(List.of("banana", "apple", "cherry"));
        List<String> sorted = CollectionUtils.sort(list);

        assertEquals(List.of("apple", "banana", "cherry"), sorted);
    }

    @Test
    public void testSortNullAndEmpty() {
        assertNull(CollectionUtils.sort(null));
        List<String> empty = new ArrayList<>();
        assertSame(empty, CollectionUtils.sort(empty));
    }

    // ==================== sortSimpleName ====================

    @Test
    public void testSortSimpleName() {
        List<String> list = new ArrayList<>(List.of(
                "org.hongxi.Beta",
                "com.example.Alpha",
                "org.hongxi.Gamma"
        ));
        CollectionUtils.sortSimpleName(list);

        assertEquals("com.example.Alpha", list.get(0));
        assertEquals("org.hongxi.Beta", list.get(1));
        assertEquals("org.hongxi.Gamma", list.get(2));
    }

    @Test
    public void testSortSimpleNameWithNull() {
        List<String> list = new ArrayList<>();
        list.add("org.hongxi.B");
        list.add(null);
        list.add("org.hongxi.A");
        CollectionUtils.sortSimpleName(list);

        assertNull(list.get(0)); // null 排在最前面
    }

    // ==================== split / join ====================

    @Test
    public void testSplit() {
        List<String> list = List.of("key1=value1", "key2=value2", "noValue");
        Map<String, String> result = CollectionUtils.split(list, "=");

        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        assertEquals("", result.get("noValue"));
    }

    @Test
    public void testSplitNull() {
        assertNull(CollectionUtils.split(null, "="));
        assertTrue(CollectionUtils.split(new ArrayList<>(), "=").isEmpty());
    }

    @Test
    public void testJoinMapToString() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("keyOnly", "");

        List<String> result = CollectionUtils.join(map, "=");
        assertEquals(3, result.size());
        assertTrue(result.contains("key1=value1"));
        assertTrue(result.contains("key2=value2"));
        assertTrue(result.contains("keyOnly"));
    }

    @Test
    public void testJoinListToString() {
        List<String> list = List.of("a", "b", "c");
        String result = CollectionUtils.join(list, ",");
        assertEquals("a,b,c", result);
    }

    @Test
    public void testJoinListToStringSingleElement() {
        List<String> list = List.of("only");
        assertEquals("only", CollectionUtils.join(list, ","));
    }

    // ==================== mapEquals ====================

    @Test
    public void testMapEquals_bothNull() {
        assertTrue(CollectionUtils.mapEquals(null, null));
    }

    @Test
    public void testMapEquals_oneNull() {
        assertFalse(CollectionUtils.mapEquals(null, new HashMap<>()));
        assertFalse(CollectionUtils.mapEquals(new HashMap<>(), null));
    }

    @Test
    public void testMapEquals_sameContent() {
        Map<String, String> map1 = Map.of("a", "1", "b", "2");
        Map<String, String> map2 = Map.of("a", "1", "b", "2");
        assertTrue(CollectionUtils.mapEquals(map1, map2));
    }

    @Test
    public void testMapEquals_differentContent() {
        Map<String, String> map1 = Map.of("a", "1");
        Map<String, String> map2 = Map.of("a", "2");
        assertFalse(CollectionUtils.mapEquals(map1, map2));
    }

    @Test
    public void testMapEquals_differentSize() {
        Map<String, String> map1 = Map.of("a", "1");
        Map<String, String> map2 = Map.of("a", "1", "b", "2");
        assertFalse(CollectionUtils.mapEquals(map1, map2));
    }

    // ==================== toStringMap / toMap ====================

    @Test
    public void testToStringMap() {
        Map<String, String> map = CollectionUtils.toStringMap("k1", "v1", "k2", "v2");
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
    }

    @Test
    public void testToStringMapOddPairs() {
        assertThrows(IllegalArgumentException.class, () ->
                CollectionUtils.toStringMap("k1", "v1", "k2"));
    }

    @Test
    public void testToStringMapEmpty() {
        Map<String, String> map = CollectionUtils.toStringMap();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToMap() {
        Map<String, Integer> map = CollectionUtils.toMap("a", 1, "b", 2, "c", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    public void testToMapNullAndEmpty() {
        assertTrue(CollectionUtils.toMap().isEmpty());
        assertTrue(CollectionUtils.toMap((Object[]) null).isEmpty());
    }

    @Test
    public void testToMapOddPairs() {
        assertThrows(IllegalArgumentException.class, () ->
                CollectionUtils.toMap("a", 1, "b"));
    }

    // ==================== splitAll / joinAll ====================

    @Test
    public void testSplitAllAndJoinAll() {
        Map<String, List<String>> original = new HashMap<>();
        original.put("group1", List.of("a=1", "b=2"));
        original.put("group2", List.of("x=10"));

        Map<String, Map<String, String>> splitResult = CollectionUtils.splitAll(original, "=");
        assertEquals("1", splitResult.get("group1").get("a"));
        assertEquals("2", splitResult.get("group1").get("b"));
        assertEquals("10", splitResult.get("group2").get("x"));

        Map<String, List<String>> joinResult = CollectionUtils.joinAll(splitResult, "=");
        assertEquals(2, joinResult.get("group1").size());
        assertEquals(1, joinResult.get("group2").size());
    }

    @Test
    public void testSplitAllNull() {
        assertNull(CollectionUtils.splitAll(null, "="));
    }

    @Test
    public void testJoinAllNull() {
        assertNull(CollectionUtils.joinAll(null, "="));
    }
}
