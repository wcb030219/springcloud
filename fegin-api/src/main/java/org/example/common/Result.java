package org.example.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装类
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 失败
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败
     */
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
    
    /**
     * 失败
     */
    public static <T> Result<T> fail() {
        return fail(500, "操作失败");
    }
}