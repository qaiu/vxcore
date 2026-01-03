package cn.qaiu.vx.core.util;

import org.apache.commons.lang3.StringUtils;


/**
 * 驼峰式下划线命名的字符串相互转换
 *
 * <br>Create date 2021/6/2 0:41
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class StringCase {

    /**
     * 将驼峰式命名的字符串转换为下划线方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。<br>
     * 例如：
     *
     * <pre>
     * HelloWorld=》hello_world
     * Hello_World=》hello_world
     * HelloWorld_test=》hello_world_test
     * </pre>
     *
     * @param str 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串
     */
    public static String toUnderlineCase(String str) {
        if (StringUtils.isEmpty(str)) return str;
        
        // 如果字符串已经包含下划线，先按驼峰规则处理，然后合并下划线
        if (str.contains("_")) {
            // 先按驼峰规则分割
            StringBuilder sb = new StringBuilder();
            for (String s : StringUtils.splitByCharacterTypeCamelCase(str)) {
                if (!s.startsWith("_")) {
                    sb.append(s.toLowerCase()).append("_");
                } else {
                    sb.append(s);
                }
            }
            String result = sb.substring(0, sb.length() - 1);
            // 合并连续的下划线为单个下划线
            return result.replaceAll("_+", "_");
        }
        
        // 标准驼峰转下划线逻辑
        StringBuilder sb = new StringBuilder();
        for (String s : StringUtils.splitByCharacterTypeCamelCase(str)) {
            if (!s.startsWith("_")) {
                sb.append(s.toLowerCase()).append("_");
            } else {
                sb.append(s);
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 将驼峰式命名的字符串转换为下划线方式（大写）。如果转换前的驼峰式命名的字符串为空，则返回空字符串。<br>
     * 例如：HelloWorld=》HELLO_WORLD
     *
     * @param str 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串(大写)
     */
    public static String toUnderlineUpperCase(String str) {
        String result = toUnderlineCase(str);
        return result != null ? result.toUpperCase() : null;
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式
     *
     * @param str 转换前的下划线方式命名的字符串
     * @param isBigCamel 是否为大驼峰式（首字母大写）
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(String str, boolean isBigCamel) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        String[] split = StringUtils.split(str, '_');
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            char[] chars = s.toCharArray();
            if ((i == 0 && isBigCamel) || (i > 0 && chars[0] >= 'a')) {
                chars[0] -= ('a' - 'A');
            }
            sb.append(new String(chars));
        }
        return sb.toString();
    }


    /**
     * 将下划线方式命名的字符串转换为大驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。<br>
     * 例如：hello_world=》HelloWorld
     *
     * @param str 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toBigCamelCase(String str) {
        return toCamelCase(str, true);
    }


    /**
     * 将下划线方式命名的字符串转换为小驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。<br>
     * 例如：hello_world=》helloWorld
     *
     * @param str 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toLittleCamelCase(String str) {
        return toCamelCase(str, false);
    }

}
