package com.ygnn.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ygnn.common.to.es.SkuEsModel;
import com.ygnn.gulimall.search.config.GulimallElasticSearchConfig;
import com.ygnn.gulimall.search.constant.EsConstant;
import com.ygnn.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author FangKun
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 保存到es
        //1、给es中建立索引

        //2、给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        //1、构造保存请求
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            request.source(jsonString, XContentType.JSON);
            //2、保存数据
            bulkRequest.add(request);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //TODO 如果批量处理错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId() + item.getFailureMessage()).collect(Collectors.toList());
        log.info("商品上架完成: {},返回数据: {}", collect, bulk.toString());
        return b;
    }
}
