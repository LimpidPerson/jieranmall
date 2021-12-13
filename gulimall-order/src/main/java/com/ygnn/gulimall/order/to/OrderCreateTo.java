package com.ygnn.gulimall.order.to;

import com.ygnn.gulimall.order.entity.OrderEntity;
import com.ygnn.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 订单项
     */
    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的的应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;

}
