package com.ygnn.gulimall.search.controller;

import com.ygnn.gulimall.search.service.MallSearchService;
import com.ygnn.gulimall.search.vo.SearchParam;
import com.ygnn.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author FangKun
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 模糊匹配、过滤(按照属性、分类、品牌、价格区间、库存)、排序、分页、高亮、聚合分析
     * @param param
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        //原生查询参数
        param.set_queryString(request.getQueryString());
        //1、根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        System.out.println(" --> " + result);
        return "list";
    }

}
