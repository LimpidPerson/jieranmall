package com.ygnn.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.gulimall.member.dao.MemberDao;
import com.ygnn.gulimall.member.dao.MemberLevelDao;
import com.ygnn.gulimall.member.entity.MemberEntity;
import com.ygnn.gulimall.member.entity.MemberLevelEntity;
import com.ygnn.gulimall.member.exception.PhoneExistException;
import com.ygnn.gulimall.member.exception.UsernameExistException;
import com.ygnn.gulimall.member.service.MemberService;
import com.ygnn.gulimall.member.vo.MemberLoginVo;
import com.ygnn.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity member = new MemberEntity();

        // 设置默认等级
        member.setLevelId(memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1)).getId());

        // 验证唯一性并抛出异常
        checkUsernameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        // 设置
        member.setUsername(vo.getUserName());
        member.setMobile(vo.getPhone());

        // 密码要进行加密存储 MD5盐值加密
        member.setPassword(new BCryptPasswordEncoder().encode(vo.getPassword()));

        // 其他默认信息
        member.setIntegration(0);
        member.setGrowth(0);
        member.setCreateTime(new Date());

        baseMapper.insert(member);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        if (baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{
        if (baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username)) > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", vo.getLoginAcct()).or().eq("mobile", vo.getLoginAcct()));
        if (entity == null) {
            // 登录失败
            return null;
        } else {
            // 密码匹配
            if (new BCryptPasswordEncoder().matches(vo.getPassword(), entity.getPassword())){
                return entity;
            } else {
                return null;
            }
        }
    }

}