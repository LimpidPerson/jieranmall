package com.ygnn.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类vo
 * @author FangKun
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {

    /**
     * 1级父分类ID
     */
    private String catalog1Id;

    /**
     * 三级子分类
     */
    private List<Catalog3VO> catalog3List;

    private String id;

    private String name;

    /**
     * 三级分类vo
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3VO {

        private String catelog2Id;

        private String id;

        private String name;
    }
}
