package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.HandleSortFilter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CommonUtil
 * <br>Create date 2021/5/8 14:52
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class CommonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 匹配正则list
     *
     * @param regList 正则list
     * @param destStr 要匹配的目标字符
     * @return 匹配成功返回true 否则返回false
     */
    public static boolean matchRegList(List<?> regList, String destStr) {
        // 判断是否忽略
        for (Object ignores : regList) {
            if (destStr.matches(ignores.toString())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 测试本机端口是否被使用
     *
     * @param port port
     * @return boolean
     */
    public static boolean isPortUsing(int port) {
        //如果该端口还在使用则返回true,否则返回false,127.0.0.1代表本机
        return isPortUsing("127.0.0.1", port);
    }

    /**
     * 测试主机Host的port端口是否被使用
     *
     * @param host host
     * @param port port
     * @return 是否被使用
     */
    public static boolean isPortUsing(String host, int port) {
        boolean flag = false;
        InetAddress Address;
        try {
            Address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }
        try (Socket ignored = new Socket(Address, port)) {
            //建立一个Socket连接
            flag = true;
        } catch (IOException ignoredException) {
        }
        return flag;
    }

    /**
     * 获取实体类匹配的json字段组成的json
     *
     * @param jsonObject 要解析的json
     * @param clazz      对应的实体类
     * @return 子json
     */
    public static JsonObject getSubJsonForEntity(JsonObject jsonObject, Class<?> clazz) {
        JsonObject data = new JsonObject();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (jsonObject.containsKey(field.getName())) {
                data.put(field.getName(), jsonObject.getValue(field.getName()));
            }
        }
        return data;
    }

    /**
     * 处理其他配置
     *
     * @param configName configName
     */
    public static void initConfig(String configName, Class<?> tClass) {
        URL resource = tClass.getResource("/conf/" + configName);
        if (resource == null) throw new RuntimeException("路径不存在");
        Buffer buffer = VertxHolder.getVertxInstance().fileSystem().readFileBlocking(resource.getPath());
        JsonObject entries = new JsonObject(buffer);
        Map<String, Object> map = entries.getMap();
        LocalConstant.put(configName, map);
        LOGGER.info("读取配置{}成功", configName);
    }

    /**
     * 对类集合进行排序
     * 根据 @HandleSortFilter 注解的 value 值进行排序
     * 
     * @param set 要排序的类集合
     * @param <T> 类型参数
     * @return 排序后的实例集合
     */
    public static <T> Set<T> sortClassSet(Set<Class<? extends T>> set) {
        return set.stream().filter(c1 -> {
            HandleSortFilter s1 = c1.getAnnotation(HandleSortFilter.class);
            if (s1 != null) {
                return s1.value() >= 0;
            } else {
                return true;
            }
        }).sorted((c1, c2) -> {
            HandleSortFilter s1 = c1.getAnnotation(HandleSortFilter.class);
            HandleSortFilter s2 = c2.getAnnotation(HandleSortFilter.class);
            int n1 = 0, n2 = 0;
            if (s1 != null) {
                n1 = s1.value();
            }
            if (s2 != null) {
                n2 = s2.value();
            }
            return n1 - n2;
        }).map(c -> {
            try {
                return ReflectionUtil.newWithNoParam(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
    }

    private static String appVersion;

    /**
     * 获取应用版本号
     * 从 app.properties 文件中读取版本信息
     * 
     * @return 应用版本号字符串
     */
    public static String getAppVersion() {
        if (null == appVersion) {
            Properties properties = new Properties();
            try {
                properties.load(CommonUtil.class.getClassLoader().getResourceAsStream("app.properties"));
                if (!properties.isEmpty()) {
                    appVersion = properties.getProperty("app.version") + "build" + properties.getProperty("build");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appVersion;
    }
}
