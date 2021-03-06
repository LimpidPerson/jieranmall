package com.ygnn.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.constant.WareConstant;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.gulimall.ware.dao.PurchaseDao;
import com.ygnn.gulimall.ware.entity.PurchaseDetailEntity;
import com.ygnn.gulimall.ware.entity.PurchaseEntity;
import com.ygnn.gulimall.ware.service.PurchaseDetailService;
import com.ygnn.gulimall.ware.service.PurchaseService;
import com.ygnn.gulimall.ware.service.WareSkuService;
import com.ygnn.gulimall.ware.vo.MergeVo;
import com.ygnn.gulimall.ware.vo.PurchaseDoneVo;
import com.ygnn.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            //1???????????????
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // TODO ???????????????????????? 0 ??? 1 ???????????????

        List<Long> items = mergeVo.getItems();
        Long finalPruchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPruchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param ids: ?????????ID
     */
    @Override
    public void received(List<Long> ids) {
        //1??????????????????????????????????????????????????????
        List<PurchaseEntity> collect = ids.stream().map(item -> this.getById(item)).filter(item -> item.getStatus() == WareConstant.PurchaseEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode()).map(item -> {
            item.setStatus(WareConstant.PurchaseEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2???????????????????????????
        this.updateBatchById(collect);

        //3???????????????????????????
        collect.forEach( item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> entityList = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(entityList);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();
        //1????????????????????????
        Boolean flag = true;

        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for(PurchaseItemDoneVo item : items){
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3?????????????????????????????????
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());

                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            updates.add(purchaseDetailEntity);
        }

        purchaseDetailService.updateBatchById(updates);

        //2????????????????????????
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseEnum.FINISH.getCode() : WareConstant.PurchaseEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}