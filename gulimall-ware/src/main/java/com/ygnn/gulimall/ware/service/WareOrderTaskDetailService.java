package com.ygnn.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:50:42
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

