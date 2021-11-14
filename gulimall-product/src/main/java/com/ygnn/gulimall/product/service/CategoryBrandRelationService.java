package com.ygnn.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.product.entity.BrandEntity;
import com.ygnn.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-03 21:34:55
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    void updateCategory(Long catId, String name);

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

