package com.ygnn.gulimall.member.exception;

/**
 * @author FangKun
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已存在");
    }
}
