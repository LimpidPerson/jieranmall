package com.ygnn.gulimall.member.dao;

import com.ygnn.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Limpid
 * @email liufangkun1008@163.com
 * @date 2021-11-04 08:35:51
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
