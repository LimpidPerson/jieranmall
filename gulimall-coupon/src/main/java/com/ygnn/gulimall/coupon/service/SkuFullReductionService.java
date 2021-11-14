package com.ygnn.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.to.SkuReductionTo;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:24:26
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

