package com.ygnn.gulimall.ware.controller;

import com.ygnn.common.exception.BizCodeEnume;
import com.ygnn.common.to.SkuHahStockVo;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.R;
import com.ygnn.gulimall.ware.entity.WareSkuEntity;
import com.ygnn.gulimall.ware.service.WareSkuService;
import com.ygnn.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:50:42
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 锁定库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try {
            Boolean stock = wareSkuService.orderLockStock(vo);
            System.out.println("stock = " + stock);
            return R.ok();
        } catch (Exception e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 查询sku是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds){
        List<SkuHahStockVo> vos = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
