package com.ygnn.gulimall.member.exception;

/**
 * @author FangKun
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("用户名已存在");
    }
}
