package cn.qaiu.vx.core.exception;

/**
 * 系统异常
 * 用于系统级错误
 * 
 * @author qaiu
 */
public class SystemException extends BaseException {
    
    public SystemException(String message) {
        super(500, message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(500, message, cause);
    }
}
