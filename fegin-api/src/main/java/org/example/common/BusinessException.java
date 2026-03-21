package org.example.common;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private int code;
    
    /**
     * 构造方法
     */
    public BusinessException() {
        super();
    }
    
    /**
     * 构造方法
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    /**
     * 构造方法
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造方法
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
    
    /**
     * 构造方法
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * 获取错误码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置错误码
     */
    public void setCode(int code) {
        this.code = code;
    }
}