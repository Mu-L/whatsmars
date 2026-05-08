package org.hongxi.whatsmars.shardingsphere.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hongxi.whatsmars.shardingsphere.entity.Order;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 继承 BaseMapper 即可获得基础 CRUD 方法，无需额外编写 SQL
}