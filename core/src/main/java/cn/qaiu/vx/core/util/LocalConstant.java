package cn.qaiu.vx.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * vertx 上下文外的本地容器 为不在vertx线程的方法传递数据
 * <br>Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class LocalConstant {
    private static final Map<String, Object> LOCAL_CONST = new HashMap<>();

    /**
     * 存储键值对到本地常量映射
     * 
     * @param k 键
     * @param v 值
     * @return 本地常量映射
     */
    public static Map<String, Object> put(String k, Object v) {
        if (LOCAL_CONST.containsKey(k)) return LOCAL_CONST;
        LOCAL_CONST.put(k, v);
        return LOCAL_CONST;
    }

    /**
     * 获取本地常量值
     * 
     * @param k 键
     * @return 对应的值
     */
    public static Object get(String k) {
        return LOCAL_CONST.get(k);
    }

    /**
     * 获取本地常量值并转换为指定类型
     * 
     * @param k 键
     * @param <T> 目标类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getWithCast(String k) {
        return (T) LOCAL_CONST.get(k);
    }

    /**
     * 检查本地常量是否包含指定键
     * 
     * @param k 键
     * @return 是否包含
     */
    public static boolean containsKey(String k) {
        return LOCAL_CONST.containsKey(k);
    }

    /**
     * 获取本地常量中的映射对象
     * 
     * @param k 键
     * @return 映射对象
     */
    public static Map<?, ?> getMap(String k) {
        return (Map<?, ?>) LOCAL_CONST.get(k);
    }

    /**
     * 获取本地常量中的字符串值
     * 
     * @param k 键
     * @return 字符串值
     */
    public static String getString(String k) {
        return LOCAL_CONST.get(k).toString();
    }


}
