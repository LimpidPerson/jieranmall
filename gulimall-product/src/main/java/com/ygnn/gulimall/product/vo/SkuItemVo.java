package com.ygnn.gulimall.product.vo;

import com.ygnn.gulimall.product.entity.SkuImagesEntity;
import com.ygnn.gulimall.product.entity.SkuInfoEntity;
import com.ygnn.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author FangKun
 */
@Data
public class SkuItemVo {

    /**
     * 1、sku基本信息获取 pms_sku_info
     */
    private SkuInfoEntity info;

    /**
     * 是否有库存,默认有
     */
    private Boolean hasStock = true;

    /**
     * 2、sku的图片信息 pms_sku_images
     */
    private List<SkuImagesEntity> images;

    /**
     * 3、获取spu的销售属性组合
     */
    private List<SkuItemSaleAttrVo> saleAttr;

    /**
     * 4、获取spu的介绍 pms_spu_info_desc
     */
    private SpuInfoDescEntity desc;

    /**
     * 5、获取spu的规格参数信息
     */
    private List<SpuItemAttrGroupVo> groupAttrs;

    /**
     * 当前商品的秒杀优惠信息
     */
    private SeckillInfoVo seckillInfo;


    @Data
    public static class SkuItemSaleAttrVo {

        private Long attrId;

        private String attrName;

        private List<AttrValueWithSkuIdVo> attrValues;

    }

    @Data
    public static class SpuItemAttrGroupVo{

        private String groupName;

        private List<SpuBaseAttrVo> attrs;

    }

    @Data
    public static class SpuBaseAttrVo{

        private String attrName;

        private String attrValue;

    }

    @Data
    public static class AttrValueWithSkuIdVo{

        private String attrValue;

        private String skuIds;

    }

}
