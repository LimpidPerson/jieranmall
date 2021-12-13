package com.ygnn.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写他的get方法，保证每次获取属性都会进行计算
 * @author FangKun
 */
@Data
public class Cart {

    /**
     * 商品项
     */
    private List<CartItem> items;

    /**
     * 商品总数量
     */
    private Integer countNum;

    /**
     * 同品类型商品数量
     */
    private Integer countType;

    /**
     * 购物车商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 优惠价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        if (items != null && items.size() > 0){
            return items.size();
        }
        return 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        //1、计算购物车总价
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }

        //2、减去优惠总价再返回
        return amount.subtract(getReduce());
    }
}
