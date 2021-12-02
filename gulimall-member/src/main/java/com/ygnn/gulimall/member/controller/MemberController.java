package com.ygnn.gulimall.member.controller;

import com.ygnn.common.exception.BizCodeEnume;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.R;
import com.ygnn.gulimall.member.entity.MemberEntity;
import com.ygnn.gulimall.member.exception.PhoneExistException;
import com.ygnn.gulimall.member.exception.UsernameExistException;
import com.ygnn.gulimall.member.feign.CouponFeignService;
import com.ygnn.gulimall.member.service.MemberService;
import com.ygnn.gulimall.member.vo.MemberLoginVo;
import com.ygnn.gulimall.member.vo.MemberRegisterVo;
import com.ygnn.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:35:51
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/gitee/login")
    public R oauthGiteeLogin(@RequestBody SocialUser socialUser){
        MemberEntity entity = memberService.giteeLogin(socialUser);
        if (entity != null) {
            //TODO 1、登录成功处理
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/weibo/login")
    public R oauthWeiboLogin(@RequestBody SocialUser socialUser){
        MemberEntity entity = memberService.weiboLogin(socialUser);
        if (entity != null) {
            //TODO 1、登录成功处理
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity member = new MemberEntity();
        member.setNickname("奇衡三");
        R r = couponFeignService.memberCoupons();
        return R.ok().put("member", member).put("coupons", r.get("coupons"));
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity login = memberService.login(vo);
        if (login != null) {
            //TODO 1、登录成功处理
            return R.ok().setData(login);
        } else {
            return R.error(BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo){
        try {
            memberService.register(vo);
        } catch (PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (Exception e){
            return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), e.getMessage());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
