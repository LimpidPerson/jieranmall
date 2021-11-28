package com.ygnn.gulimall.product.web;

import com.ygnn.gulimall.product.service.SkuInfoService;
import com.ygnn.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author FangKun
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){

        System.out.println(skuId);
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item", vo);

        return "item";
    }

}
