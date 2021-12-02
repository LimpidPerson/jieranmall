package com.ygnn.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.utils.HttpUtils;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.gulimall.member.dao.MemberDao;
import com.ygnn.gulimall.member.dao.MemberLevelDao;
import com.ygnn.gulimall.member.entity.MemberEntity;
import com.ygnn.gulimall.member.entity.MemberLevelEntity;
import com.ygnn.gulimall.member.exception.PhoneExistException;
import com.ygnn.gulimall.member.exception.UsernameExistException;
import com.ygnn.gulimall.member.service.MemberService;
import com.ygnn.gulimall.member.vo.SocialUser;
import com.ygnn.gulimall.member.vo.MemberLoginVo;
import com.ygnn.gulimall.member.vo.MemberRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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
        member.setNickname(vo.getUserName());

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

    @Override
    public MemberEntity giteeLogin(SocialUser socialUser) {
        // 登录和注册合并逻辑
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("gitee_uid", socialUser.getId()));
        //1、判断当前社交用户是否已经登录系统
        if (entity != null) {
            // 这个用户已经注册了
            MemberEntity update = new MemberEntity();
            update.setGiteeUid(entity.getGiteeUid());
            update.setGiteeAccessToken(entity.getGiteeAccessToken());
            baseMapper.updateById(update);

            entity.setGiteeAccessToken(socialUser.getAccessToken());
            return entity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个
            MemberEntity register = new MemberEntity();
            try {
                //3、查询当前社交用户的社交账户信息(昵称、性别等)
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccessToken());
                query.put("uid", socialUser.getId().toString());
                HttpResponse response = HttpUtils.doGet("https://gitee.com","/api/v5/user", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 查询成功
                    JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity()));
                    // 获取gitee用户信息
                    register.setNickname(json.getString("name"));
                    //TODO 还可以继续获取用户的其他基本信息
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            // 设置默认等级
            register.setLevelId(memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1)).getId());
            // 设置创建时间
            register.setCreateTime(new Date());
            register.setGiteeUid(socialUser.getId());
            register.setGiteeAccessToken(socialUser.getAccessToken());
            baseMapper.insert(register);
            return  register;
        }
    }

    @Override
    public MemberEntity weiboLogin(SocialUser socialUser) {
        // 登录和注册合并逻辑
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("weibo_uid", socialUser.getUid()));
        //1、判断当前社交用户是否已经登录系统
        if (entity != null) {
            // 这个用户已经注册了
            MemberEntity update = new MemberEntity();
            update.setWeiboUid(entity.getWeiboUid());
            update.setWeiboAccessToken(entity.getWeiboAccessToken());
            baseMapper.updateById(update);

            entity.setWeiboAccessToken(socialUser.getAccessToken());
            return entity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个
            MemberEntity register = new MemberEntity();
            try {
                //3、查询当前社交用户的社交账户信息(昵称、性别等)
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccessToken());
                query.put("uid", socialUser.getUid().toString());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com","/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 查询成功
                    JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity()));
                    // 获取微博用户信息
                    register.setNickname(json.getString("name"));
                    //TODO 还可以继续获取用户的其他基本信息
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            // 设置默认等级
            register.setLevelId(memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1)).getId());
            // 设置创建时间
            register.setCreateTime(new Date());
            register.setWeiboUid(socialUser.getUid());
            register.setWeiboAccessToken(socialUser.getAccessToken());
            baseMapper.insert(register);
            return  register;
        }
    }

}