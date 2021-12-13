package com.ygnn.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.ware.entity.WareInfoEntity;
import com.ygnn.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:50:42
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收货地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

