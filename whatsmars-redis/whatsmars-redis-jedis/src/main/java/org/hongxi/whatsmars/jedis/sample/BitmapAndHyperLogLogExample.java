package org.hongxi.whatsmars.jedis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 位图（Bitmap）与 HyperLogLog 使用示例
 *
 * Bitmap（位图）：
 * - 本质是 String 类型，每个 bit 可以表示一个布尔值
 * - 适用场景：签到打卡、在线用户统计、布隆过滤器
 * - 命令：SETBIT / GETBIT / BITCOUNT / BITOP
 *
 * HyperLogLog：
 * - 基数统计（去重计数），误差约 0.81%
 * - 适用场景：UV 统计、独立访客计数
 * - 每个 HyperLogLog key 仅占用 12KB，可统计最多 2^64 个元素
 * - 命令：PFADD / PFCOUNT / PFMERGE
 */
@Component
public class BitmapAndHyperLogLogExample {

    private static final Logger log = LoggerFactory.getLogger(BitmapAndHyperLogLogExample.class);

    private final JedisPool jedisPool;

    public BitmapAndHyperLogLogExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== 位图与 HyperLogLog 示例 ==========");
        bitmapCheckinDemo();
        bitmapOnlineUsersDemo();
        bitmapBitOpDemo();
        hyperLogLogDemo();
        hyperLogLogMergeDemo();
        log.info("========== 位图与 HyperLogLog 示例结束 ==========\n");
    }

    // ==================== Bitmap：用户签到 ====================

    /**
     * 用位图实现用户签到：每个 bit 代表一天
     * - offset 0 = 第 1 天，offset 1 = 第 2 天，...
     */
    private void bitmapCheckinDemo() {
        log.info("--- Bitmap 用户签到 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            String key = "bitmap:checkin:user:1001:202606";

            // 模拟用户在 6 月的第 1, 3, 5, 7, 10 天签到
            jedis.setbit(key, 0, true);  // 6月1日
            jedis.setbit(key, 2, true);  // 6月3日
            jedis.setbit(key, 4, true);  // 6月5日
            jedis.setbit(key, 6, true);  // 6月7日
            jedis.setbit(key, 9, true);  // 6月10日

            // 查询某天是否签到
            log.info("  6月1日签到 = {}", jedis.getbit(key, 0));
            log.info("  6月2日签到 = {}", jedis.getbit(key, 1));
            log.info("  6月3日签到 = {}", jedis.getbit(key, 2));

            // 统计总签到天数
            long checkinDays = jedis.bitcount(key);
            log.info("  本月签到总天数 = {}", checkinDays);

            // 统计前 10 天内的签到天数
            long firstTenDays = jedis.bitcount(key.getBytes(), 0, 1); // 前 2 字节 = 16 bit
            log.info("  前 10 天签到数 = {}", firstTenDays);

            jedis.del(key);
        }
    }

    // ==================== Bitmap：在线用户统计 ====================

    /**
     * 用位图统计在线用户：offset = userId
     * - SETBIT online:20260605 userId 1  → 用户上线
     * - BITCOUNT online:20260605          → 在线总人数
     */
    private void bitmapOnlineUsersDemo() {
        log.info("--- Bitmap 在线用户统计 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            String key = "bitmap:online:20260605";

            // 模拟用户 1001, 2005, 3000, 5000, 8000 在线
            jedis.setbit(key, 1001, true);
            jedis.setbit(key, 2005, true);
            jedis.setbit(key, 3000, true);
            jedis.setbit(key, 5000, true);
            jedis.setbit(key, 8000, true);

            // 在线总人数
            long onlineCount = jedis.bitcount(key);
            log.info("  在线用户总数 = {}", onlineCount);

            // 判断某用户是否在线
            log.info("  用户 1001 在线 = {}", jedis.getbit(key, 1001));
            log.info("  用户 9999 在线 = {}", jedis.getbit(key, 9999));

            jedis.del(key);
        }
    }

    // ==================== Bitmap：位运算 ====================

    /**
     * BITOP：对多个位图做 AND/OR/XOR/NOT 运算
     * 示例：统计连续两天都在线的用户
     */
    private void bitmapBitOpDemo() {
        log.info("--- Bitmap 位运算（BITOP）---");

        try (Jedis jedis = jedisPool.getResource()) {
            String day1 = "bitmap:online:day1";
            String day2 = "bitmap:online:day2";
            String bothDays = "bitmap:online:both";

            // 第 1 天在线用户
            jedis.setbit(day1, 1, true);
            jedis.setbit(day1, 2, true);
            jedis.setbit(day1, 3, true);

            // 第 2 天在线用户
            jedis.setbit(day2, 2, true);
            jedis.setbit(day2, 3, true);
            jedis.setbit(day2, 4, true);

            // AND：两天都在线的用户
            jedis.bitop(redis.clients.jedis.args.BitOP.AND, bothDays, day1, day2);
            long bothOnline = jedis.bitcount(bothDays);
            log.info("  两天都在线的用户数 = {} (用户 2, 3)", bothOnline);

            // OR：两天中任一天在线的用户
            jedis.bitop(redis.clients.jedis.args.BitOP.OR, bothDays, day1, day2);
            long anyOnline = jedis.bitcount(bothDays);
            log.info("  两天中任一天在线的用户数 = {} (用户 1,2,3,4)", anyOnline);

            jedis.del(day1, day2, bothDays);
        }
    }

    // ==================== HyperLogLog：UV 统计 ====================

    /**
     * HyperLogLog 基数统计：统计独立访客数（UV）
     * 即使添加重复元素，也只计数独立值
     */
    private void hyperLogLogDemo() {
        log.info("--- HyperLogLog UV 统计 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            String key = "hll:page:home:20260605";

            // 模拟用户访问（PFADD：添加元素，返回 1 表示新增了独立元素）
            long added1 = jedis.pfadd(key, "user:1001", "user:1002", "user:1003");
            log.info("  PFADD 3 个新用户 -> 返回值: {} (1=有新元素)", added1);

            // 重复访问不计入
            long added2 = jedis.pfadd(key, "user:1001", "user:1002");
            log.info("  PFADD 重复用户 -> 返回值: {} (0=无新元素)", added2);

            // 再来一批新用户
            jedis.pfadd(key, "user:2001", "user:2002", "user:2003", "user:2004");

            // 统计 UV
            long uv = jedis.pfcount(key);
            log.info("  UV (PFCOUNT) = {} (实际独立用户: 7)", uv);

            // 大批量数据测试：模拟 10000 个用户
            String bigKey = "hll:big:test";
            for (int i = 0; i < 10000; i++) {
                jedis.pfadd(bigKey, "visitor:" + i);
            }
            long bigCount = jedis.pfcount(bigKey);
            double error = Math.abs(bigCount - 10000.0) / 10000.0 * 100;
            log.info("  10000 独立用户 -> PFCOUNT = {}, 误差 = {}%", bigCount, String.format("%.2f", error));

            jedis.del(key, bigKey);
        }
    }

    // ==================== HyperLogLog：合并 ====================

    /**
     * PFMERGE：合并多个 HyperLogLog（如合并多天的 UV）
     */
    private void hyperLogLogMergeDemo() {
        log.info("--- HyperLogLog 合并（PFMERGE）---");

        try (Jedis jedis = jedisPool.getResource()) {
            // 周一 UV
            jedis.pfadd("hll:mon", "user:A", "user:B", "user:C", "user:D");
            // 周二 UV
            jedis.pfadd("hll:tue", "user:C", "user:D", "user:E", "user:F");
            // 周三 UV
            jedis.pfadd("hll:wed", "user:A", "user:E", "user:G");

            log.info("  周一 UV = {}", jedis.pfcount("hll:mon"));
            log.info("  周二 UV = {}", jedis.pfcount("hll:tue"));
            log.info("  周三 UV = {}", jedis.pfcount("hll:wed"));

            // 合并三天的 UV
            jedis.pfmerge("hll:week", "hll:mon", "hll:tue", "hll:wed");
            long weekUv = jedis.pfcount("hll:week");
            log.info("  三天合并 UV = {} (实际独立用户: A,B,C,D,E,F,G = 7)", weekUv);

            jedis.del("hll:mon", "hll:tue", "hll:wed", "hll:week");
        }
    }
}
