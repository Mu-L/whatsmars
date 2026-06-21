package org.hongxi.whatsmars.shardingsphere.controller;

import org.hongxi.whatsmars.shardingsphere.entity.Order;
import org.hongxi.whatsmars.shardingsphere.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/order")
public class OrderController {
    
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping("/add")
    public Order addOrder(@RequestParam Long userId,
                          @RequestParam BigDecimal amount) {
        return orderService.createOrder(userId, amount);
    }
    
    @GetMapping("/query")
    public Order queryOrder(@RequestParam Long userId) {
        return orderService.getOrderByUser(userId);
    }
}
