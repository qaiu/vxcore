package cn.qaiu.vx.core.exception;

/**
 * 业务异常
 * 用于业务逻辑错误
 * 
 * @author qaiu
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(400, message);
    }
    
    public BusinessException(int code, String message) {
        super(code, message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(400, message, cause);
    }
    
    public BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
