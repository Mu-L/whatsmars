-- 创建 2 个分库
CREATE DATABASE IF NOT EXISTS order_db_0;
CREATE DATABASE IF NOT EXISTS order_db_1;

-- 订单表 0
CREATE TABLE `t_order_0` (
                             `id` bigint NOT NULL COMMENT '订单ID',
                             `order_no` varchar(64) NOT NULL COMMENT '订单编号',
                             `user_id` bigint NOT NULL COMMENT '用户ID（分片键）',
                             `amount` decimal(10,2) DEFAULT NULL COMMENT '订单金额',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单表 1（复制 t_order_0 结构）
CREATE TABLE `t_order_1` LIKE `t_order_0`;