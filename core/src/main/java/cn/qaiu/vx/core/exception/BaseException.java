package cn.qaiu.vx.core.exception;

/**
 * 基础异常类
 * 所有业务异常的父类
 * 
 * @author qaiu
 */
public class BaseException extends RuntimeException {
    
    private final int code;
    private final String message;
    
    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
