package com.ygnn.gulimall.authserver.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FangKun
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocialUser {

    private Long id;

    private Long uid;

    private String accessToken;

    private String tokenType;

    private Integer expiresIn;

    private String refreshToken;

    private String scope;

    private Integer createdAt;

}
