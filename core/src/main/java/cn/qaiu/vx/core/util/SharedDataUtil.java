package cn.qaiu.vx.core.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

/**
 * vertx 共享数据
 * <br>Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SharedDataUtil {

    private static SharedData sharedData;

    /**
     * 获取共享数据对象
     * 
     * @return Vert.x共享数据对象
     */
    public static SharedData shareData() {
        if (sharedData == null) {
            try {
                sharedData = VertxHolder.getVertxInstance().sharedData();
            } catch (Exception e) {
                // 在测试环境中可能没有初始化Vertx，返回null
                return null;
            }
        }
        return sharedData;
    }

    /**
     * 获取本地映射
     * 
     * @param key 映射键
     * @return 本地映射对象
     */
    public static LocalMap<String, Object> getLocalMap(String key) {
        SharedData data = shareData();
        if (data == null) {
            return null;
        }
        return data.getLocalMap(key);
    }

    /**
     * 获取带类型转换的本地映射
     * 
     * @param key 映射键
     * @param <T> 值类型
     * @return 本地映射对象
     */
    public static <T> LocalMap<String, T> getLocalMapWithCast(String key) {
        SharedData data = shareData();
        if (data == null) {
            return null;
        }
        return data.getLocalMap(key);
    }

    /**
     * 获取JSON配置
     * 
     * @param key 配置键
     * @return JSON配置对象
     */
    public static JsonObject getJsonConfig(String key) {
        LocalMap<String, Object> localMap = getLocalMap("local");
        if (localMap == null) {
            return null;
        }
        return (JsonObject) localMap.get(key);
    }

    /**
     * 获取自定义配置
     * 
     * @return 自定义配置JSON对象
     */
    public static JsonObject getCustomConfig() {
        return getJsonConfig("customConfig");
    }

    /**
     * 获取自定义配置中的字符串值
     * 
     * @param key 配置键
     * @return 字符串值
     */
    public static String getStringForCustomConfig(String key) {
        return getJsonConfig("customConfig").getString(key);
    }

    /**
     * 获取自定义配置中的JSON数组
     * 
     * @param key 配置键
     * @return JSON数组
     */
    public static JsonArray getJsonArrayForCustomConfig(String key) {
        return getJsonConfig("customConfig").getJsonArray(key);
    }

    /**
     * 获取自定义配置中的值
     * 
     * @param key 配置键
     * @param <T> 值类型
     * @return 配置值
     */
    public static <T> T getValueForCustomConfig(String key) {
        return CastUtil.cast(getJsonConfig("customConfig").getValue(key));
    }

    /**
     * 获取服务器配置中的JSON对象
     * 
     * @param key 配置键
     * @return JSON对象
     */
    public static JsonObject getJsonObjectForServerConfig(String key) {
        return getJsonConfig("server").getJsonObject(key);
    }

    /**
     * 获取服务器配置中的JSON数组
     * 
     * @param key 配置键
     * @return JSON数组
     */
    public static JsonArray getJsonArrayForServerConfig(String key) {
        return getJsonConfig("server").getJsonArray(key);
    }

    /**
     * 获取服务器配置中的字符串值
     * 
     * @param key 配置键
     * @return 字符串值
     */
    public static String getJsonStringForServerConfig(String key) {
        return getJsonConfig("server").getString(key);
    }

    /**
     * 获取服务器配置中的值
     * 
     * @param key 配置键
     * @param <T> 值类型
     * @return 配置值
     */
    public static <T> T getValueForServerConfig(String key) {
        return CastUtil.cast(getJsonConfig("server").getValue(key));
    }

}
