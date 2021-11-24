package com.ygnn.gulimall.search.service;

import com.ygnn.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author FangKun
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
