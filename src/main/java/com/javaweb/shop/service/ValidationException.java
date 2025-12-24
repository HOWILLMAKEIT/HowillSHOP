package com.javaweb.shop.service;

// 业务校验失败时抛出，直接提示给前端
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
