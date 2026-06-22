package org.hongxi.whatsmars.internal.collection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 集合框架核心示例：HashMap原理、ArrayList vs LinkedList、排序、fail-fast、Stream操作
 */
class CollectionExamples {

    public static void main(String[] args) {
        hashMapDemo();
        arrayListVsLinkedList();
        sortingDemo();
        failFastDemo();
        streamOperations();
    }

    // ==================== 1. HashMap 核心要点 ====================

    static void hashMapDemo() {
        System.out.println("===== HashMap =====");

        // 底层结构：数组 + 链表 + 红黑树（JDK 8+）
        // 当链表长度 >= 8 且数组长度 >= 64 时，链表转红黑树
        // 当红黑树节点 <= 6 时，退化回链表

        HashMap<String, Integer> map = new HashMap<>();

        // put 流程：
        // 1. 计算 key 的 hash（高16位异或低16位，减少碰撞）
        // 2. (n-1) & hash 确定数组下标
        // 3. 无冲突直接放入，有冲突则链表追加或树节点插入
        // 4. 超过阈值（capacity * loadFactor，默认 0.75）触发扩容（2倍）

        for (int i = 0; i < 20; i++) {
            map.put("key-" + i, i);
        }
        System.out.println("size: " + map.size());

        // get 流程：
        // 1. 计算 hash 定位桶
        // 2. 第一个节点匹配则返回
        // 3. 是树则树查找，否则遍历链表
        // 4. key 比较：先 == 再 equals

        System.out.println("get key-5: " + map.get("key-5"));
        System.out.println("get null: " + map.get("non-exist"));

        // 默认容量 16，负载因子 0.75
        // 预估元素数 / 0.75 作为初始容量，避免频繁扩容
        HashMap<String, String> preSized = new HashMap<>((int) (1000 / 0.75) + 1);

        // HashMap vs Hashtable vs ConcurrentHashMap：
        // - HashMap: 非线程安全，允许 null key/value
        // - Hashtable: 线程安全（全表锁），已过时
        // - ConcurrentHashMap: 线程安全（分段锁/CAS），推荐

        // key 需要正确实现 hashCode + equals
        System.out.println();
    }

    // ==================== 2. ArrayList vs LinkedList ====================

    static void arrayListVsLinkedList() {
        System.out.println("===== ArrayList vs LinkedList =====");

        // ArrayList: 动态数组，随机访问 O(1)，中间插入删除 O(n)
        // LinkedList: 双向链表，随机访问 O(n)，头尾插入删除 O(1)

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        // 尾部添加（两者都很快）
        for (int i = 0; i < 10000; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // 随机访问（ArrayList 完胜）
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.get(i);
        }
        long arrayListTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            linkedList.get(i);
        }
        long linkedListTime = System.nanoTime() - start;

        System.out.printf("随机访问1000次 - ArrayList: %dμs, LinkedList: %dμs%n",
            arrayListTime / 1000, linkedListTime / 1000);

        // 实际开发建议：
        // - 99% 的场景用 ArrayList（CPU缓存友好，内存连续）
        // - 只在频繁头尾操作时用 LinkedList（Deque 接口）
        // - ArrayDeque 做栈/队列比 LinkedList 更优

        // ArrayDeque 作为栈使用
        Deque<String> stack = new ArrayDeque<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        System.out.println("栈顶弹出: " + stack.pop()); // LIFO

        // ArrayDeque 作为队列使用
        Deque<String> queue = new ArrayDeque<>();
        queue.offer("x");
        queue.offer("y");
        queue.offer("z");
        System.out.println("队头出队: " + queue.poll()); // FIFO
        System.out.println();
    }

    // ==================== 3. 排序 ====================

    static void sortingDemo() {
        System.out.println("===== 排序 =====");

        // Comparable: 自然排序（类自身实现，如 String、Integer）
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted); // 自然排序
        System.out.println("自然排序: " + sorted);

        // Comparator: 定制排序（不修改原类）
        List<User> users = Arrays.asList(
            new User("Alice", 30),
            new User("Bob", 25),
            new User("Charlie", 35)
        );

        // 按年龄排序
        users.sort(Comparator.comparingInt(u -> u.age));
        System.out.println("按年龄: " + users);

        // 多条件排序：先按年龄，再按名字
        users.sort(Comparator.comparingInt((User u) -> u.age)
            .thenComparing(u -> u.name));
        System.out.println("多条件: " + users);

        // 逆序
        users.sort(Comparator.comparingInt((User u) -> u.age).reversed());
        System.out.println("逆序:   " + users);

        // Collections.unmodifiableList: 不可变视图
        List<String> immutable = Collections.unmodifiableList(new ArrayList<>(names));
        try {
            immutable.add("fail");
        } catch (UnsupportedOperationException e) {
            System.out.println("不可变列表: 添加失败(符合预期)");
        }
        System.out.println();
    }

    // ==================== 4. fail-fast 机制 ====================

    static void failFastDemo() {
        System.out.println("===== fail-fast =====");

        // 集合在迭代过程中被修改 -> 抛出 ConcurrentModificationException
        // 原理：modCount（修改次数） != expectedModCount（期望值）

        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));

        // 错误做法：在 for-each 中直接修改
        try {
            for (String s : list) {
                if ("b".equals(s)) {
                    list.remove(s); // ConcurrentModificationException!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("for-each中直接remove: " + e.getClass().getSimpleName());
        }

        // 正确做法一：使用 Iterator.remove()
        List<String> list2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list2.iterator();
        while (it.hasNext()) {
            if ("b".equals(it.next())) {
                it.remove(); // 安全删除
            }
        }
        System.out.println("Iterator.remove后: " + list2);

        // 正确做法二：使用 removeIf（JDK 8+）
        List<String> list3 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        list3.removeIf("b"::equals);
        System.out.println("removeIf后: " + list3);

        // 正确做法三：使用普通 for 循环（倒序遍历）
        List<String> list4 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        for (int i = list4.size() - 1; i >= 0; i--) {
            if ("b".equals(list4.get(i))) {
                list4.remove(i);
            }
        }
        System.out.println("倒序for后: " + list4);
        System.out.println();
    }

    // ==================== 5. Stream 操作 ====================

    static void streamOperations() {
        System.out.println("===== Stream =====");

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // filter + map + collect
        List<Integer> evenSquares = numbers.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .collect(Collectors.toList());
        System.out.println("偶数平方: " + evenSquares);

        // reduce
        int sum = numbers.stream().reduce(0, Integer::sum);
        System.out.println("求和: " + sum);

        // groupingBy
        List<User> users = Arrays.asList(
            new User("Alice", 30),
            new User("Bob", 25),
            new User("Charlie", 30),
            new User("David", 25)
        );
        Map<Integer, List<User>> byAge = users.stream()
            .collect(Collectors.groupingBy(u -> u.age));
        System.out.println("按年龄分组: " + byAge);

        // parallelStream（并行流，利用多核）
        long count = numbers.parallelStream()
            .filter(n -> n > 5)
            .count();
        System.out.println("并行流计数(>5): " + count);

        // 注意：
        // - Stream 不会修改源集合
        // - 有状态操作（sorted, distinct）在并行流中开销较大
        // - 短操作适合串行流，大量数据适合并行流
        System.out.println();
    }

    // ---------- 辅助类 ----------

    static class User {
        final String name;
        final int age;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }
}
