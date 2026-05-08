package org.hongxi.whatsmars.shardingsphere.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.IdUtil;
import org.hongxi.whatsmars.shardingsphere.dao.OrderMapper;
import org.hongxi.whatsmars.shardingsphere.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {
    
    /**
     * 创建订单
     * 注意：ID 由 ShardingSphere 雪花算法自动生成，无需手动设置
     */
    public Order createOrder(Long userId, BigDecimal amount) {
        Order order = new Order();
        order.setOrderNo(IdUtil.fastSimpleUUID());  // 生成订单号
        order.setUserId(userId);
        order.setAmount(amount);
        order.setCreateTime(LocalDateTime.now());
        
        // 保存时 ShardingSphere 会根据 user_id 自动路由到对应库表
        this.save(order);
        return order;
    }
    
    /**
     * 根据用户ID查询订单（分片键查询，性能最优）
     */
    public Order getOrderByUser(Long userId) {
        return lambdaQuery()
                .eq(Order::getUserId, userId)
                .one();
    }
}