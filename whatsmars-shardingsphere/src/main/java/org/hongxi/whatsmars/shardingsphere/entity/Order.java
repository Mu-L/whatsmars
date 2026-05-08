package org.hongxi.whatsmars.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")  // 逻辑表名，与配置中的逻辑表一致
public class Order {

    @TableId
    private Long id;            // 分布式ID，由ShardingSphere自动生成

    private String orderNo;     // 订单编号

    private Long userId;        // 用户ID（分片键）

    private BigDecimal amount;  // 订单金额

    private LocalDateTime createTime;  // 创建时间
}
