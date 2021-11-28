package com.ygnn.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.product.entity.SkuInfoEntity;
import com.ygnn.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-03 21:34:54
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    SkuItemVo item(Long skuId);
}

