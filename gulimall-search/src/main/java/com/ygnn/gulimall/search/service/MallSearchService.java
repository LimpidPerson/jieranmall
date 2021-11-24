package com.ygnn.gulimall.search.service;

import com.ygnn.gulimall.search.vo.SearchParam;
import com.ygnn.gulimall.search.vo.SearchResult;

/**
 * @author FangKun
 */
public interface MallSearchService {

    /**
     * 检索
     * @param param 检索的所有参数
     * @return 返回的检索结果
     */
    SearchResult search(SearchParam param);

}
