package com.ygnn.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(DigestUtils.md5Hex("mltyyds"));
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println(new BCryptPasswordEncoder().matches("1234", "$2a$10$jMftdxiyvk9GFS2XlgAemOhSWukFga.wOA1.UiA.o/1MAMjiviUKq"));
    }

}
