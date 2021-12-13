package com.ygnn.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.common.utils.R;
import com.ygnn.gulimall.ware.dao.WareInfoDao;
import com.ygnn.gulimall.ware.entity.WareInfoEntity;
import com.ygnn.gulimall.ware.feign.MemberFeignService;
import com.ygnn.gulimall.ware.service.WareInfoService;
import com.ygnn.gulimall.ware.vo.FareVo;
import com.ygnn.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if (data != null) {
            //TODO 可以对接第三方计算运费结果
            String phone = data.getPhone();
            BigDecimal bigDecimal = new BigDecimal(phone.substring(phone.length() - 1, phone.length()));
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}