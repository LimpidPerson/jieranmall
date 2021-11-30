package com.ygnn.gulimall.authserver.feign;

import com.ygnn.common.utils.R;
import com.ygnn.gulimall.authserver.vo.UserLoginVo;
import com.ygnn.gulimall.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author FangKun
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R Register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R Login(@RequestBody UserLoginVo vo);

}
