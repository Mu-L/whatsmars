package org.hongxi.whatsmars.internal.generic;

import java.util.*;

/**
 * 泛型核心示例：类型擦除、PECS原则、泛型方法、通配符、类型令牌
 */
class GenericExamples {

    public static void main(String[] args) {
        typeErasure();
        pecsPrinciple();
        genericMethod();
        wildcardBounds();
        typeToken();
    }

    // ==================== 1. 类型擦除 ====================

    static void typeErasure() {
        System.out.println("===== 类型擦除 =====");

        // Java 泛型是编译时特性，运行时被擦除为 Object（或上界类型）
        // List<String> 和 List<Integer> 在运行时都是 List

        List<String> strList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 运行时类型相同！
        System.out.println("strList class == intList class: " +
            (strList.getClass() == intList.getClass())); // true

        // 类型擦除的影响：
        // 1. 不能用基本类型作为类型参数（List<int> 非法，要用 List<Integer>）
        // 2. 不能创建泛型数组（new T[] 非法）
        // 3. 不能 new 泛型实例（new T() 非法）
        // 4. 不能 instanceof 泛型类型（obj instanceof List<String> 非法）
        // 5. 静态成员不能使用类的类型参数

        // 擦除后的上界：
        // <T> -> Object
        // <T extends Comparable> -> Comparable
        // <T extends Number & Comparable> -> Number（第一个上界）

        // 桥接方法：编译器自动生成，保证多态正确
        // class MyList implements Comparable<MyList> {
        //     int compareTo(MyList o) {...}
        //     // 编译器生成：int compareTo(Object o) { return compareTo((MyList)o); }
        // }

        // 通过反射可以看到泛型信息（未被完全擦除的场景）：
        // 1. 字段的泛型类型
        // 2. 方法参数的泛型类型
        // 3. 父类的泛型参数
        System.out.println();
    }

    // ==================== 2. PECS 原则 ====================

    static void pecsPrinciple() {
        System.out.println("===== PECS 原则 =====");

        // Producer Extends, Consumer Super
        // - 只读取（生产）-> 用 ? extends T
        // - 只写入（消费）-> 用 ? super T
        // - 既读又写 -> 不用通配符

        // ? extends Number: 只能读，不能写（除了 null）
        List<Integer> intList = Arrays.asList(1, 2, 3);
        double sum = sumOfNumbers(intList); // Integer extends Number
        System.out.println("sumOfNumbers: " + sum);

        List<Double> doubleList = Arrays.asList(1.1, 2.2, 3.3);
        sum = sumOfNumbers(doubleList); // Double extends Number
        System.out.println("sumOfNumbers: " + sum);

        // ? super Integer: 只能写，读出来只能是 Object
        List<Number> numberList = new ArrayList<>();
        addIntegers(numberList); // Number is super of Integer
        System.out.println("addIntegers: " + numberList);

        // 经典案例：Collections.copy
        // public static <T> void copy(List<? super T> dest, List<? extends T> src)
        // src: 生产者 -> extends（只读）
        // dest: 消费者 -> super（只写）

        List<Integer> src = Arrays.asList(10, 20, 30);
        List<Number> dest = new ArrayList<>();
        Collections.copy(dest, src); // 错误！dest size < src size
        // 正确用法：
        List<Number> dest2 = new ArrayList<>(src.size());
        for (int i = 0; i < src.size(); i++) dest2.add(null);
        Collections.copy(dest2, src);
        System.out.println("Collections.copy: " + dest2);
        System.out.println();
    }

    // ==================== 3. 泛型方法 ====================

    static void genericMethod() {
        System.out.println("===== 泛型方法 =====");

        // 泛型方法可以独立于泛型类存在
        // 类型推断：编译器自动推导类型参数

        // 自定义泛型方法
        String maxStr = max("apple", "banana");
        Integer maxInt = max(3, 7);
        System.out.println("max string: " + maxStr);
        System.out.println("max int: " + maxInt);

        // 泛型方法 + 可变参数
        List<String> merged = asList("a", "b", "c");
        System.out.println("asList: " + merged);

        // 泛型工具方法（模拟 Collections 常用方法）
        List<String> empty = emptyList();
        System.out.println("emptyList: " + empty + ", class: " + empty.getClass());

        // 泛型类
        Pair<String, Integer> pair = new Pair<>("age", 25);
        System.out.println("Pair: " + pair.getFirst() + "=" + pair.getSecond());
        System.out.println();
    }

    // ==================== 4. 通配符上下界 ====================

    static void wildcardBounds() {
        System.out.println("===== 通配符上下界 =====");

        // ? extends T: 上界通配符，表示 T 或 T 的子类
        // ? super T:   下界通配符，表示 T 或 T 的父类
        // ?:           无界通配符，表示任意类型

        // 上界示例
        Number n1 = maxBound(Arrays.asList(1, 2, 3));   // List<Integer extends Number>
        Number n2 = maxBound(Arrays.asList(1.1, 2.2));   // List<Double extends Number>
        System.out.println("maxBound(Integer): " + n1);
        System.out.println("maxBound(Double): " + n2);

        // 下界示例
        List<Object> objects = new ArrayList<>();
        addNumbers(objects); // List<? super Number> 接受 List<Object>
        System.out.println("addNumbers: " + objects);

        // 无界通配符
        printSize(Arrays.asList(1, 2, 3));
        printSize(Arrays.asList("a", "b"));
        System.out.println();
    }

    // ==================== 5. 类型令牌（Type Token）====================

    static void typeToken() {
        System.out.println("===== 类型令牌 =====");

        // 由于类型擦除，运行时无法获取泛型参数
        // 解决方案：传递 Class 对象作为"类型令牌"

        // 示例：类型安全的容器
        Favorites favorites = new Favorites();
        favorites.put(String.class, "Java");
        favorites.put(Integer.class, 42);
        favorites.put(Class.class, GenericExamples.class);

        // 取出时自动转型，无需强制转换
        String s = favorites.get(String.class);
        Integer i = favorites.get(Integer.class);
        Class<?> c = favorites.get(Class.class);
        System.out.println("String: " + s);
        System.out.println("Integer: " + i);
        System.out.println("Class: " + c.getSimpleName());

        // 实际应用：
        // - Jackson 的 TypeReference<T>（通过匿名子类获取泛型信息）
        // - Gson 的 TypeToken<T>
        // - Spring 的 ParameterizedTypeReference<T>
        System.out.println();
    }

    // ---------- 辅助方法 ----------

    // PECS: Producer Extends
    static double sumOfNumbers(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) {
            sum += n.doubleValue();
        }
        return sum;
    }

    // PECS: Consumer Super
    static void addIntegers(List<? super Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
    }

    // 泛型方法
    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    // 泛型方法 + 可变参数
    static <T> List<T> asList(T... elements) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }

    // 返回空列表（类型安全）
    static <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    // 上界通配符
    static <T extends Number> T maxBound(List<T> list) {
        T max = list.get(0);
        for (T t : list) {
            if (t.doubleValue() > max.doubleValue()) {
                max = t;
            }
        }
        return max;
    }

    // 下界通配符
    static void addNumbers(List<? super Number> list) {
        list.add(1);
        list.add(2.0);
        list.add(3L);
    }

    // 无界通配符
    static void printSize(List<?> list) {
        System.out.println("size=" + list.size() + ", type=" +
            (list.isEmpty() ? "unknown" : list.get(0).getClass().getSimpleName()));
    }

    // ---------- 辅助类 ----------

    // 泛型类
    static class Pair<F, S> {
        private final F first;
        private final S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        F getFirst() { return first; }
        S getSecond() { return second; }
    }

    // 类型安全容器（类型令牌模式）
    static class Favorites {
        private final Map<Class<?>, Object> map = new HashMap<>();

        <T> void put(Class<T> type, T instance) {
            map.put(Objects.requireNonNull(type), instance);
        }

        <T> T get(Class<T> type) {
            return type.cast(map.get(type));
        }
    }
}
