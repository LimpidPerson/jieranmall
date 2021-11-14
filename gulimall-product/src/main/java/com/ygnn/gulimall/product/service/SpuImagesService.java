package com.ygnn.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.product.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-10 22:21:37
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    void saveImages(Long id, List<String> images);

    PageUtils queryPage(Map<String, Object> params);
}

