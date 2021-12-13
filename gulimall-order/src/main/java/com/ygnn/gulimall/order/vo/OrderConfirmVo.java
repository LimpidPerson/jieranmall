package com.ygnn.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要用的数据
 * @author FangKun
 */
@Data
public class OrderConfirmVo {

    /**
     * 收货地址，ums——member_receive_address表
     */
    private List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    private List<OrderItemVo> items;

    // 发票记录。。。

    /**
     * 优惠卷信息...
     */
    private Integer integration;

    /**
     * 防重令牌
     * 防止重复提交
     */
    private String orderToken;

    /**
     * 是否有库存
     */
    private Map<Long, Boolean> stocks;

    /**
     * 商品总件数
     */
    private Integer allCount;

    public Integer getAllCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }

    /**
     * 订单总额
     */
    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
