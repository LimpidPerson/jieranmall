package com.ygnn.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.ygnn.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void searchDate() throws IOException {
        //1、创建检索请求
        SearchRequest request = new SearchRequest();
        //指定索引
        request.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1、构造检索条件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        System.out.println(sourceBuilder);

        request.source(sourceBuilder);

        //2、执行检索
        SearchResponse search = client.search(request, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //3、分析结果
        System.out.println(search.toString());

    }

    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        User user = new User();
        user.setUsername("hanhan");
        user.setAge(16);
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        // 真正执行保存操作
        IndexResponse index = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User{
        private String username;
        private String gender;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
