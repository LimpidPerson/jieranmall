package com.ygnn.gulimall.seckill.interceptor;

import com.ygnn.common.constant.AuthServerConstant;
import com.ygnn.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author FangKun
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // order/order/status/*
        // 匹配并放行这个路径
        boolean match = new AntPathMatcher().match("/kill", request.getRequestURI());
        if (match) {
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null) {
                loginUser.set(attribute);
                return true;
            } else {
                // 没登录就去登录
                request.getSession().setAttribute("msg", "请先进行登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        } else {
            return true;
        }
    }
}
