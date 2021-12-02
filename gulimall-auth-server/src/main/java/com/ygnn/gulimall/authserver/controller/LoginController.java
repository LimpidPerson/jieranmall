package com.ygnn.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.ygnn.common.constant.AuthServerConstant;
import com.ygnn.common.exception.BizCodeEnume;
import com.ygnn.common.utils.R;
import com.ygnn.common.vo.MemberResoVo;
import com.ygnn.gulimall.authserver.feign.MemberFeignService;
import com.ygnn.gulimall.authserver.feign.ThirdPartFeignService;
import com.ygnn.gulimall.authserver.vo.UserLoginVo;
import com.ygnn.gulimall.authserver.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author FangKun
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            // 没登录
            return "login";
        } else {
            return  "redirect:http://gulimall.com";
        }
    }

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
        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring + "_" + System.currentTimeMillis();
        // Redis缓存验证码，防止同一个phone在60秒内再次发送验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, substring);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理。将数据放在session中。
     * 只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO 1、分布式下的session问题
     * RedirectAttributes redirectAttributes: 模拟重定向携带数据
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        // 校验出错
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 真正注册,调用远程服务进行注册
        //1、校验验证码
        String code =vo.getCode();

        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isEmpty(s)){
            if (code.equals("1234")){
                // 删除验证码，令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getCode());
                // 验证码通过  真正注册,调用远程服务进行注册
                R r = memberFeignService.Register(vo);
                if (r.getCode() == 0) {
                    // 成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    // 注册成功回到首页,回到登录页
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                // 校验出错，转发到注册页
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        // 调用远程服务登录接口
        R login = memberFeignService.Login(vo);
        if (login.getCode() == 0) {
            // 成功放到session中
            session.setAttribute(AuthServerConstant.LOGIN_USER, login.getData("data", new TypeReference<MemberResoVo>(){}));
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
