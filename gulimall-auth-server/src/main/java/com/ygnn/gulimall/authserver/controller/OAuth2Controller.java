package com.ygnn.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ygnn.common.constant.AuthServerConstant;
import com.ygnn.common.utils.HttpUtils;
import com.ygnn.common.utils.R;
import com.ygnn.common.vo.MemberRespVo;
import com.ygnn.gulimall.authserver.feign.MemberFeignService;
import com.ygnn.gulimall.authserver.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 * @author FangKun
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        SocialUser socialUser;
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "6394549fd7908bbc4afb1c0fdfa8244301b89f7edf5026598e323126e22d9daa");
        map.put("client_secret", "05c98c3cb3a2946ff0c36fd897193d1a1a73dbfc6a20fd6df219e0c041613486");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee");
        map.put("code", code);

        //1、根据code换取accessToken
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", header, query, map);

        //2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            socialUser = JSON.parseObject(EntityUtils.toString(response.getEntity()), SocialUser.class);
            // 获取gitee账户ID
            query.put("access_token", socialUser.getAccessToken());
            HttpResponse res = HttpUtils.doGet("https://gitee.com","/api/v5/user","get", header, query);
            if (res.getStatusLine().getStatusCode() == 200) {
                // 把获取到的账户ID设置进去
                socialUser.setId(Long.parseLong(JSON.parseObject(EntityUtils.toString(res.getEntity())).getString("id")));
            }
            R r = memberFeignService.oauthGiteeLogin(socialUser);
            if (r.getCode() == 0) {
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功,用户信息: {}", data);
                //3、登录成功就跳回首页
                //第一次使用session,命令浏览器保存卡号,JsessionId的cookie
                //子域之间，发卡的时候(指定域名为父域名),即使是子域发卡,父域也可使用
                //TODO 1、默认发的令牌作用域是当前域(解决子域session共享问题)
                //TODO 2、使用JSON的序列化方式来序列化对象数据到Redis中
                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                return "redirect:http://gulimall.com";
            } else {
                // 登录失败
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            // 请求失败
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/oauth2.0/weibo")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        SocialUser socialUser;
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "3874709481");
        map.put("client_secret", "d5db8b42e6921ce7216fbb795aa3defe");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo");
        map.put("code", code);

        //1、根据code换取accessToken
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);

        //2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            socialUser = JSON.parseObject(EntityUtils.toString(response.getEntity()), SocialUser.class);
            R r = memberFeignService.oauthWeiboLogin(socialUser);
            if (r.getCode() == 0) {
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功,用户信息: {}", data);
                //3、登录成功就跳回首页
                //第一次使用session,命令浏览器保存卡号,JsessionId的cookie
                //子域之间，发卡的时候(指定域名为父域名),即使是子域发卡,父域也可使用
                //TODO 1、默认发的令牌作用域是当前域(解决子域session共享问题)
                //TODO 2、使用JSON的序列化方式来序列化对象数据到Redis中
                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                return "redirect:http://gulimall.com";
            } else {
                // 登录失败
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            // 请求失败
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
