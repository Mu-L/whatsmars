package org.hongxi.whatsmars.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@TableName("t_order")  // 逻辑表名，与配置中的逻辑表一致
public class Order {

    @TableId
    private Long id;            // 分布式ID，由ShardingSphere自动生成

    private String orderNo;     // 订单编号

    private Long userId;        // 用户ID（分片键）

    private BigDecimal amount;  // 订单金额

    private LocalDateTime createTime;  // 创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order that = (Order) o;
        return Objects.equals(id, that.id)
                && Objects.equals(orderNo, that.orderNo)
                && Objects.equals(userId, that.userId)
                && Objects.equals(amount, that.amount)
                && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderNo, userId, amount, createTime);
    }

    @Override
    public String toString() {
        return "Order(id=" + id + ", orderNo=" + orderNo + ", userId=" + userId
                + ", amount=" + amount + ", createTime=" + createTime + ")";
    }
}
