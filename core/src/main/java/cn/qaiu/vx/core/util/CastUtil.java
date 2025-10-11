package cn.qaiu.vx.core.util;

/**
 * 类型转换工具类
 * 提供安全的泛型转换方法，旨在消除泛型转换时的异常
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface CastUtil {

    /**
     * 泛型转换
     * 将对象安全地转换为指定类型
     * 
     * @param object 要转换的对象
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    static <T> T cast(Object object) {
        return (T) object;
    }
}
