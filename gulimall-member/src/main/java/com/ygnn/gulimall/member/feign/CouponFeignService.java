package com.ygnn.gulimall.member.feign;

import com.ygnn.common.utils.R;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
@Component
public interface CouponFeignService {

    @LoadBalanced
    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupons();

}
