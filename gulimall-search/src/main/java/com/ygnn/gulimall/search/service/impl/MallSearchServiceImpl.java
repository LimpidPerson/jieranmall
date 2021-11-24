package com.ygnn.gulimall.search.service.impl;

import com.ygnn.gulimall.search.constant.EsConstant;
import com.ygnn.gulimall.search.service.MallSearchService;
import com.ygnn.gulimall.search.vo.SearchParam;
import com.ygnn.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author FangKun
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 去es进行检索
     * @param param 检索的所有参数
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {
        //1、动态构建出查询需要的DSL语句
        SearchResult searchResult = null;

        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2、执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //3、分析响应数据封装成我们需要的格式
            searchResult = buildSearchResutle(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResutle(SearchResponse response) {

        return null;
    }

    /**
     * 准备检索请求
     * 模糊匹配、过滤(按照属性、分类、品牌、价格区间、库存)、排序、分页、高亮、聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        // 构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 模糊匹配、过滤(按照属性、分类、品牌、价格区间、库存)
         */
        //1、构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1.1、must - 模糊品牌
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //1.2.1、bool - filter - 按照三级分类ID查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //1.2.2、bool - filter - 按照品牌ID查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //1.2.3、bool - filter - 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()){
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                // 检索属性的ID
                String attrId = s[0];
                // 检索属性的值
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个都必须单独生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //1.2.4、bool - filter - 按照库存是否有进行查询
        boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));

        //1.2.5、bool - filter - 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                // 区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getKeyword().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        // 封装所有条件
        sourceBuilder.query(boolQuery);

        /**
         * 排序、分页、高亮
         */
        //2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            // sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }

        //2.2、分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        //2.3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'");
            builder.postTags("</b>");
            sourceBuilder.highlighter();
        }

        /**
         * 聚合分析
         */

        System.out.println("构建的DSL" + sourceBuilder);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
