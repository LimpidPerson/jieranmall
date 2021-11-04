package com.ygnn.gulimall.product.dao;

import com.ygnn.gulimall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-03 21:34:54
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
