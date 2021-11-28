package com.ygnn.gulimall.authserver.controller;

import com.ygnn.common.constant.AuthServerConstant;
import com.ygnn.common.exception.BizCodeEnume;
import com.ygnn.common.utils.R;
import com.ygnn.gulimall.authserver.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author FangKun
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //TODO 1、接口防刷

        String redisCode =  stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            if (System.currentTimeMillis() - Long.parseLong(redisCode.split("_")[1]) < 60 * 1000) {
                // 60秒内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //2、验证码的再次校验 Redis
        String code = UUID.randomUUID().toString().substring(0 ,5) + "_" + System.currentTimeMillis();
        // Redis缓存验证码，防止同一个phone在60秒内再次发送验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

}
