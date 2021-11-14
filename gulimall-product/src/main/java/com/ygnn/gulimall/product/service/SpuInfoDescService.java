package com.ygnn.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-03 21:34:55
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);
}

