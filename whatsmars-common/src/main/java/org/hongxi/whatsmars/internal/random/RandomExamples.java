package org.hongxi.whatsmars.internal.random;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * RandomGenerator（java.util.random）核心示例：
 *
 * <p>Java 17 引入的统一随机数生成器接口，将所有 RNG 实现纳入同一套 API。</p>
 *
 * <pre>
 * RandomGenerator（顶层接口）
 * ├── SplittableGenerator          — 可分裂，适用于并行任务
 * ├── StreamableGenerator          — 可流式生成
 * ├── JumpableGenerator            — 可跳跃，保证序列不重叠
 * ├── ArbitrarilyJumpableGenerator — 可任意距离跳跃
 * ├── Random                      — 经典实现（线性同余）
 * ├── ThreadLocalRandom           — 线程局部，高并发首选
 * ├── SecureRandom                — 密码学安全
 * └── 多种新算法（Xoshiro、LXM 等）
 * </pre>
 *
 * <p>核心优势：</p>
 * <ul>
 *   <li>面向接口编程，算法可随时替换，不影响业务代码</li>
 *   <li>工厂模式创建实例，可按需选择统计质量、速度、安全性</li>
 *   <li>向后兼容：Random / ThreadLocalRandom / SecureRandom 均实现此接口</li>
 * </ul>
 */
class RandomExamples {

    public static void main(String[] args) {
        factoryDemo();
        basicGenerationDemo();
        streamGenerationDemo();
        algorithmComparisonDemo();
        splittableDemo();
        jumpableDemo();
        practicalPatternDemo();
    }

    // ==================== 1. RandomGeneratorFactory 工厂用法 ====================

    static void factoryDemo() {
        System.out.println("===== RandomGeneratorFactory =====");

        // 1. 获取默认工厂（等同于 new Random()）
        RandomGeneratorFactory<RandomGenerator> defaultFactory = RandomGeneratorFactory.getDefault();
        System.out.println("默认算法: " + defaultFactory.name());

        // 2. 通过指定算法名创建工厂
        RandomGeneratorFactory<RandomGenerator> factory =
                RandomGeneratorFactory.of("Xoshiro256PlusPlus");
        System.out.println("指定算法: " + factory.name());

        // 3. 工厂属性查询
        System.out.println("  统计质量:   " + factory.isStatistical());   // 是否统计均匀
        System.out.println("  可确定性:   " + factory.isStochastic());   // 是否随机（非确定性）
        System.out.println("  硬件加速:   " + factory.isHardware());      // 是否硬件加速
        System.out.println("  可分裂:     " + factory.isSplittable());   // 是否支持 split()
        System.out.println("  可跳跃:     " + factory.isJumpable());     // 是否支持 jump()
        System.out.println("  状态位数:   " + factory.stateBits());
        System.out.println("  均衡度:     " + factory.equidistribution());

        // 4. 列出所有可用算法
        System.out.println("\n所有可用算法:");
        RandomGeneratorFactory.all()
                .forEach(f -> System.out.printf("  [%s] %s (statistical=%b, splittable=%b)%n",
                        f.group(), f.name(), f.isStatistical(), f.isSplittable()));

        // 5. 创建实例
        RandomGenerator gen = factory.create();
        System.out.println("\n生成实例: " + gen.getClass().getSimpleName());
        System.out.println();
    }

    // ==================== 2. 基本随机数生成 ====================

    static void basicGenerationDemo() {
        System.out.println("===== 基本随机数生成 =====");

        RandomGenerator gen = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create();

        // boolean
        System.out.println("nextBoolean: " + gen.nextBoolean());

        // int
        System.out.println("nextInt:        " + gen.nextInt());
        System.out.println("nextInt(100):   " + gen.nextInt(100));        // [0, 100)
        System.out.println("nextInt(10,20): " + gen.nextInt(10, 20));    // [10, 20)

        // long
        System.out.println("nextLong(1000): " + gen.nextLong(1000));
        System.out.println("nextLong(50,100): " + gen.nextLong(50, 100));

        // double
        System.out.println("nextDouble:     " + gen.nextDouble());        // [0.0, 1.0)
        System.out.println("nextDouble(5.0): " + gen.nextDouble(5.0));   // [0.0, 5.0)
        System.out.println("nextDouble(1.0, 3.0): " + gen.nextDouble(1.0, 3.0));

        // float
        System.out.println("nextFloat:      " + gen.nextFloat());

        // bytes
        byte[] bytes = new byte[8];
        gen.nextBytes(bytes);
        System.out.print("nextBytes(8):   [");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i] + (i < bytes.length - 1 ? ", " : ""));
        }
        System.out.println("]");

        // 高斯分布（均值0，标准差1）
        System.out.println("nextGaussian:   " + gen.nextGaussian());
        System.out.println();
    }

    // ==================== 3. 流式生成 ====================

    static void streamGenerationDemo() {
        System.out.println("===== 流式生成 =====");

        RandomGenerator gen = RandomGeneratorFactory.of("L128X256MixRandom").create();

        // IntStream：生成10个 [0, 100) 的随机整数
        System.out.print("10个随机int: ");
        gen.ints(10, 0, 100).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // LongStream：生成5个随机 long
        System.out.print("5个随机long: ");
        gen.longs(5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // DoubleStream：生成5个 [0.0, 1.0) 的随机 double
        System.out.print("5个随机double: ");
        gen.doubles(5).forEach(d -> System.out.printf("%.4f ", d));
        System.out.println();

        // 无限流 + limit：模拟掷骰子100次统计6出现的次数
        long sixCount = gen.ints(1, 7).limit(10000).filter(n -> n == 6).count();
        System.out.println("掷骰子10000次，6出现次数: " + sixCount + " (期望≈1667)");
        System.out.println();
    }

    // ==================== 4. 算法对比 ====================

    static void algorithmComparisonDemo() {
        System.out.println("===== 算法对比 =====");

        // 不同算法的特点：
        // - Random (LinearCongruential): 最快，统计质量一般，非线程安全
        // - ThreadLocalRandom: 高并发首选，每线程独立实例，无锁竞争
        // - Xoshiro256PlusPlus: 速度快，统计质量好，适合非安全场景
        // - L128X256MixRandom: 高质量混合，适合大多数场景
        // - SecureRandom: 密码学安全，速度慢，用于密钥/令牌生成

        String[] algorithms = {"Random", "Xoshiro256PlusPlus", "L128X256MixRandom"};
        int iterations = 1_000_000;

        for (String algo : algorithms) {
            RandomGenerator g = RandomGeneratorFactory.of(algo).create();

            long start = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < iterations; i++) {
                sum += g.nextInt();
            }
            long elapsed = System.nanoTime() - start;

            System.out.printf("%-20s %d次nextInt耗时: %dms%n",
                    algo, iterations, elapsed / 1_000_000);
        }

        // SecureRandom 单独测试（较慢）
        RandomGenerator secure = new java.security.SecureRandom();
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += secure.nextInt();
        }
        long elapsed = System.nanoTime() - start;
        System.out.printf("%-20s %d次nextInt耗时: %dms%n",
                "SecureRandom", iterations, elapsed / 1_000_000);

        System.out.println("(sum仅作占位，防止JIT优化掉循环)");
        System.out.println();
    }

    // ==================== 5. SplittableGenerator 可分裂 ====================

    static void splittableDemo() {
        System.out.println("===== SplittableGenerator =====");

        // SplittableGenerator 支持 split()：
        // 从一个生成器"分裂"出独立的子生成器，适用于并行/分治任务
        // 每个子生成器产生统计独立的随机序列

        RandomGeneratorFactory<RandomGenerator> factory =
                RandomGeneratorFactory.of("Xoshiro256PlusPlus");

        if (factory.isSplittable()) {
            RandomGenerator.SplittableGenerator parent =
                    (RandomGenerator.SplittableGenerator) factory.create();

            // 分裂出两个子生成器
            RandomGenerator.SplittableGenerator child1 = parent.split();
            RandomGenerator.SplittableGenerator child2 = parent.split();

            System.out.println("父生成器前3个int:");
            for (int i = 0; i < 3; i++) System.out.print("  " + parent.nextInt(100));
            System.out.println();

            System.out.println("子生成器1前3个int:");
            for (int i = 0; i < 3; i++) System.out.print("  " + child1.nextInt(100));
            System.out.println();

            System.out.println("子生成器2前3个int:");
            for (int i = 0; i < 3; i++) System.out.print("  " + child2.nextInt(100));
            System.out.println();

            // 典型用法：并行任务中每个线程持有独立生成器
            // 避免多线程共享同一 Random 实例的锁竞争
            System.out.println("\n典型场景：并行蒙特卡洛模拟（每个子任务独立序列）");
        }
        System.out.println();
    }

    // ==================== 6. JumpableGenerator 可跳跃 ====================

    static void jumpableDemo() {
        System.out.println("===== JumpableGenerator =====");

        // JumpableGenerator 支持 jump()：
        // 将生成器状态"跳跃"一个固定步长，等效于调用 next 2^N 次
        // 适用于需要保证多个生成器序列不重叠的场景

        RandomGeneratorFactory<RandomGenerator> factory =
                RandomGeneratorFactory.of("Xoshiro256PlusPlus");

        if (factory.isJumpable()) {
            RandomGenerator.JumpableGenerator gen1 =
                    (RandomGenerator.JumpableGenerator) factory.create();
            RandomGenerator.JumpableGenerator gen2 = gen1.copy();

            // gen2 跳跃一步（等效于调用 next 2^128 次，Xoshiro256 的跳跃距离）
            gen2.jump();

            System.out.println("gen1 前3个int（原始序列）:");
            for (int i = 0; i < 3; i++) System.out.print("  " + gen1.nextInt(1000));
            System.out.println();

            System.out.println("gen2 前3个int（跳跃后序列，与gen1完全不重叠）:");
            for (int i = 0; i < 3; i++) System.out.print("  " + gen2.nextInt(1000));
            System.out.println();

            // 用法场景：为多个长期运行的并行任务分配不重叠的随机序列
        }
        System.out.println();
    }

    // ==================== 7. 实际开发模式 ====================

    static void practicalPatternDemo() {
        System.out.println("===== 实际开发模式 =====");

        // 模式一：面向接口编程，方便替换和测试
        DiceRoller roller = new DiceRoller(
                RandomGeneratorFactory.of("Xoshiro256PlusPlus").create()
        );
        System.out.print("掷骰子10次: ");
        for (int i = 0; i < 10; i++) {
            System.out.print(roller.roll() + " ");
        }
        System.out.println();

        // 模式二：高并发场景用 ThreadLocalRandom（已是 RandomGenerator 实现）
        System.out.print("ThreadLocalRandom生成5个int: ");
        java.util.concurrent.ThreadLocalRandom tlr = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            System.out.print(tlr.nextInt(100) + " ");
        }
        System.out.println();

        // 模式三：安全场景用 SecureRandom
        System.out.print("SecureRandom生成5个int: ");
        java.security.SecureRandom sr = new java.security.SecureRandom();
        for (int i = 0; i < 5; i++) {
            System.out.print(sr.nextInt(100) + " ");
        }
        System.out.println();

        // 模式四：用 RandomGenerator 作为方法参数，便于注入和Mock
        String token = generateToken(
                RandomGeneratorFactory.of("L128X256MixRandom").create(), 16
        );
        System.out.println("生成随机Token: " + token);
        System.out.println();
    }

    // ---------- 辅助类 ----------

    /**
     * 面向接口编程示例：骰子生成器
     * 构造时注入 RandomGenerator，测试时可替换为确定性Mock
     */
    static class DiceRoller {
        private final RandomGenerator random;

        DiceRoller(RandomGenerator random) {
            this.random = random;
        }

        int roll() {
            return random.nextInt(1, 7); // [1, 6]
        }
    }

    /**
     * 生成随机字母数字Token
     */
    static String generateToken(RandomGenerator random, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
