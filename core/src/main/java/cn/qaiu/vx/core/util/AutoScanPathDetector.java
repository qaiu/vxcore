package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * 自动扫描路径检测器
 * 根据启动类的位置自动配置扫描路径
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AutoScanPathDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScanPathDetector.class);

    /**
     * 根据启动类自动检测扫描路径
     *
     * @param mainClass 启动类
     * @return 扫描路径集合
     */
    public static Set<String> detectScanPaths(Class<?> mainClass) {
        Set<String> scanPaths = new HashSet<>();
        
        if (mainClass == null) {
            LOGGER.warn("Main class is null, using default scan path: cn.qaiu");
            scanPaths.add("cn.qaiu");
            return scanPaths;
        }

        String mainClassName = mainClass.getName();
        LOGGER.info("Detecting scan paths for main class: {}", mainClassName);

        // 优先检查@App注解
        if (mainClass.isAnnotationPresent(App.class)) {
            App appAnnotation = mainClass.getAnnotation(App.class);
            String baseScanPackage = appAnnotation.baseScanPackage();
            
            if (baseScanPackage != null && !baseScanPackage.trim().isEmpty()) {
                LOGGER.info("Using @App annotation baseScanPackage: {}", baseScanPackage);
                Set<String> appScanPaths = parseScanPackages(baseScanPackage);
                LOGGER.info("App-annotated scan paths: {}", appScanPaths);
                return appScanPaths;
            } else {
                LOGGER.info("@App annotation found but baseScanPackage is empty, falling back to auto-detection");
            }
        }

        // 获取启动类的包名
        String packageName = getPackageName(mainClassName);
        
        if (packageName == null || packageName.isEmpty()) {
            LOGGER.warn("Cannot determine package name from main class: {}, using default", mainClassName);
            scanPaths.add("cn.qaiu");
            return scanPaths;
        }

        // 生成扫描路径
        generateScanPaths(packageName, scanPaths);
        
        LOGGER.info("Auto-detected scan paths: {}", scanPaths);
        return scanPaths;
    }

    /**
     * 根据启动类自动检测扫描路径（通过堆栈跟踪）
     *
     * @return 扫描路径集合
     */
    public static Set<String> detectScanPathsFromStackTrace() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            // 查找main方法
            for (StackTraceElement element : stackTrace) {
                if ("main".equals(element.getMethodName())) {
                    String className = element.getClassName();
                    Class<?> mainClass = Class.forName(className);
                    return detectScanPaths(mainClass);
                }
            }
            
            LOGGER.warn("Cannot find main method in stack trace, using default scan path");
            Set<String> defaultPaths = new HashSet<>();
            defaultPaths.add("cn.qaiu");
            return defaultPaths;
            
        } catch (Exception e) {
            LOGGER.error("Failed to detect scan paths from stack trace", e);
            Set<String> defaultPaths = new HashSet<>();
            defaultPaths.add("cn.qaiu");
            return defaultPaths;
        }
    }

    /**
     * 获取类名的包名部分
     *
     * @param className 完整类名
     * @return 包名
     */
    private static String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return className.substring(0, lastDotIndex);
        }
        return null;
    }

    /**
     * 生成扫描路径
     *
     * @param packageName 包名
     * @param scanPaths 扫描路径集合
     */
    private static void generateScanPaths(String packageName, Set<String> scanPaths) {
        // 添加当前包
        scanPaths.add(packageName);
        
        // 添加父包（如果存在）
        String[] packageParts = packageName.split("\\.");
        if (packageParts.length > 1) {
            // 添加直接父包
            String parentPackage = String.join(".", Arrays.copyOf(packageParts, packageParts.length - 1));
            scanPaths.add(parentPackage);
            
            // 如果是example包，也添加根包
            if (packageParts.length >= 2 && "example".equals(packageParts[packageParts.length - 1])) {
                String rootPackage = String.join(".", Arrays.copyOf(packageParts, packageParts.length - 1));
                scanPaths.add(rootPackage);
            }
        }
        
        // 特殊处理：如果是cn.qaiu.example，也添加cn.qaiu
        if (packageName.startsWith("cn.qaiu.") && !packageName.equals("cn.qaiu")) {
            scanPaths.add("cn.qaiu");
        }
    }

    /**
     * 格式化扫描路径为配置字符串
     *
     * @param scanPaths 扫描路径集合
     * @return 配置字符串
     */
    public static String formatScanPaths(Set<String> scanPaths) {
        if (scanPaths == null || scanPaths.isEmpty()) {
            return "cn.qaiu";
        }
        
        return String.join(",", scanPaths);
    }

    /**
     * 验证扫描路径是否有效
     *
     * @param scanPath 扫描路径
     * @return 是否有效
     */
    public static boolean isValidScanPath(String scanPath) {
        if (scanPath == null || scanPath.trim().isEmpty()) {
            return false;
        }
        
        // 检查包名格式
        return scanPath.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }

    /**
     * 解析扫描包配置字符串
     *
     * @param baseScanPackage 扫描包配置字符串，支持逗号分隔
     * @return 扫描路径集合
     */
    private static Set<String> parseScanPackages(String baseScanPackage) {
        Set<String> scanPaths = new HashSet<>();
        
        if (baseScanPackage == null || baseScanPackage.trim().isEmpty()) {
            return scanPaths;
        }
        
        // 按逗号分割并去除空白
        String[] packages = baseScanPackage.split(",");
        for (String pkg : packages) {
            String trimmedPkg = pkg.trim();
            if (!trimmedPkg.isEmpty() && isValidScanPath(trimmedPkg)) {
                scanPaths.add(trimmedPkg);
            } else if (!trimmedPkg.isEmpty()) {
                LOGGER.warn("Invalid scan package: {}, skipping", trimmedPkg);
            }
        }
        
        return scanPaths;
    }

    /**
     * 获取推荐的扫描路径（用于日志输出）
     *
     * @param mainClass 启动类
     * @return 推荐信息
     */
    public static String getRecommendation(Class<?> mainClass) {
        Set<String> scanPaths = detectScanPaths(mainClass);
        StringBuilder recommendation = new StringBuilder();
        
        if (mainClass.isAnnotationPresent(App.class)) {
            App appAnnotation = mainClass.getAnnotation(App.class);
            String baseScanPackage = appAnnotation.baseScanPackage();
            if (baseScanPackage != null && !baseScanPackage.trim().isEmpty()) {
                recommendation.append("App-annotated scan paths for ").append(mainClass.getName()).append(":\n");
            } else {
                recommendation.append("Auto-detected scan paths for ").append(mainClass.getName()).append(":\n");
            }
        } else {
            recommendation.append("Auto-detected scan paths for ").append(mainClass.getName()).append(":\n");
        }
        
        for (String path : scanPaths) {
            recommendation.append("  - ").append(path).append("\n");
        }
        
        recommendation.append("You can set 'baseLocations' in your config file to override this behavior.");
        
        return recommendation.toString();
    }
}
