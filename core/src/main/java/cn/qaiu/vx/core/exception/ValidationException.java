package cn.qaiu.vx.core.exception;

/**
 * 验证异常
 * 用于参数验证错误
 * 
 * @author qaiu
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String message) {
        super(422, message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(422, message, cause);
    }
}
