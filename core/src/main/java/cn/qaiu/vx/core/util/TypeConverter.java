package cn.qaiu.vx.core.util;

import java.lang.reflect.Type;

/**
 * 类型转换器接口
 * 用于自定义参数类型转换
 * 
 * @param <T> 目标类型
 * @author QAIU
 */
public interface TypeConverter<T> {
    
    /**
     * 将字符串转换为目标类型
     * 
     * @param value 字符串值
     * @return 转换后的对象
     * @throws IllegalArgumentException 转换失败时抛出
     */
    T convert(String value) throws IllegalArgumentException;
    
    /**
     * 获取支持的目标类型
     * 
     * @return 目标类型
     */
    Class<T> getTargetType();
}