package com.ygnn.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.to.mq.SeckillOrderTo;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.order.entity.OrderEntity;
import com.ygnn.gulimall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:45:41
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 下单
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭订单
     * @param entity
     */
    void closeOrder(OrderEntity entity);

    /**
     * 获取当前订单的支付消息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTo orderTo);

}

