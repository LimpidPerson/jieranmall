package com.ygnn.gulimall.product.dao;

import com.ygnn.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-03 21:34:55
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
