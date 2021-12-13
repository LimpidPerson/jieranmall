package com.ygnn.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.exception.NoStockException;
import com.ygnn.common.to.SkuHahStockVo;
import com.ygnn.common.to.mq.OrderTo;
import com.ygnn.common.to.mq.StockDetailTo;
import com.ygnn.common.to.mq.StockLockedTo;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.common.utils.R;
import com.ygnn.gulimall.ware.dao.WareSkuDao;
import com.ygnn.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.ygnn.gulimall.ware.entity.WareOrderTaskEntity;
import com.ygnn.gulimall.ware.entity.WareSkuEntity;
import com.ygnn.gulimall.ware.feign.OrderFeignService;
import com.ygnn.gulimall.ware.feign.ProductFeignService;
import com.ygnn.gulimall.ware.service.WareOrderTaskDetailService;
import com.ygnn.gulimall.ware.service.WareOrderTaskService;
import com.ygnn.gulimall.ware.service.WareSkuService;
import com.ygnn.gulimall.ware.vo.OrderItemVo;
import com.ygnn.gulimall.ware.vo.OrderVo;
import com.ygnn.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 库存解锁
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unLockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        // 修改为已解锁
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录就新增
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //TODO: 远程调用sku的名字,如果失败，整个事务无需回滚
            //TODO: 还可以用什么方法让异常出现以后不回滚? 高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHahStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHahStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHahStockVo vo = new SkuHahStockVo();
            // 查询当前sku的总库存量
            // SELECT SUM(stock - stock_locked) FROM wms_ware_sku WHERE sku_id = ?
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单的详情用来溯源
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1-1、按照下单的收货地址，找到一个就近仓库然后锁定该库存
        //1-2、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        //2、锁定库存
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            //1、
            for (Long wareId : wareIds) {
                // 成功就返回1，否则就是0
                System.out.println(skuId + "|||" + wareId + "||||" + stock.getNum());
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    //TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", stock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    lockedTo.setDetailId(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    // 当前仓库锁定失败，重试下一个仓库
                }
            }
            if (skuStocked == false) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }

        //3、肯定全部都是锁定成功才能走到这
        return true;
    }

    /**
     * 1、库存自动解锁
     * 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。
     * 2、订单失败
     * 锁库存失败
     *
     * 只要解锁库存的消息失败一定要告诉服务解锁失败
     * @param to
     */
    @Override
    public void unLockStock(StockLockedTo to) {
        Long id = to.getId();
        StockDetailTo detail = to.getDetailId();
        Long detailId = detail.getId();
        // 解锁流程
        //1、查询数据库关于这个订单的锁定库存信息
        // 有：表示库存锁定成功了
        //      解锁：订单情况
        //          1、没有这个订单，必须解锁
        //          2、有这个订单，不是解锁库存
        //              订单状态：已取消：解锁库存
        //                      没取消：不能解锁
        // 没有：表示库存锁定失败库存回滚了，这种情况无需解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            // 根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                // 订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    // 订单不存在，订单已经被取消，才能解锁库存
                    if (byId.getLockStatus() == 1) {
                        // 当前库存工作单详情的状态为1表示已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                } else {
                    // 消息拒绝以后重新放到队列里面，让别人继续消费解锁
                    throw new RuntimeException("远程服务失败");
                }
            }
        } else {
            // 无需解锁
        }
    }

    /**
     * 防止因为订单服务卡顿导致订单状态消息一直改不了，库存消息优先到期时查询订单状态为新建状态就上面都不做就走了。
     * 导致卡顿的订单就会永远不能解锁库存
     * @param order
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo order) {
        // 查一下最新的库存状态，防止重复解锁库存
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(order.getOrderSn());
        // 按照工作单找到所有还没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", task.getId()).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {

        private Long skuId;

        private Integer num;

        private List<Long> wareId;
    }

}