package com.ygnn.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.gulimall.member.entity.MemberEntity;
import com.ygnn.gulimall.member.exception.PhoneExistException;
import com.ygnn.gulimall.member.exception.UsernameExistException;
import com.ygnn.gulimall.member.vo.MemberLoginVo;
import com.ygnn.gulimall.member.vo.MemberRegisterVo;
import com.ygnn.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:35:51
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity giteeLogin(SocialUser socialUser);

    MemberEntity weiboLogin(SocialUser socialUser);
}

