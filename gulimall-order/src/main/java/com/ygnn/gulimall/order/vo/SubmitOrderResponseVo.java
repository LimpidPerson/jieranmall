package com.ygnn.gulimall.order.vo;

import com.ygnn.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    /**
     * 订单信息
     */
    private OrderEntity orderEntity;

    /**
     * 0表示成功，其他表示各种错误信息
     */
    private Integer code;

}
