package com.ygnn.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-10 22:21:37
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

