package com.ygnn.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 * @author FangKun
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     */
    private String sort;

    /**
     * 显示是否有货
     */
    private Integer hasStock;

    /**
     * 价格区间查询
     */
    private String skuPrice;

    /**
     *
     * 按照品牌进行查询，可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行查询，可以多选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

}
