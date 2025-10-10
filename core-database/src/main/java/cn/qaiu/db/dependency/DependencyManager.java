package cn.qaiu.db.dependency;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * 数据库依赖管理器
 * 管理可选数据库驱动的按需引入
 * 
 * @author QAIU
 */
public class DependencyManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyManager.class);
    
    private static final String DEPENDENCIES_CONFIG = "database-dependencies.json";
    
    private static final Map<String, DatabaseDependency> dependencies = new HashMap<>();
    
    static {
        loadDependencies();
    }
    
    /**
     * 加载数据库依赖配置
     */
    private static void loadDependencies() {
        try (InputStream is = DependencyManager.class.getClassLoader()
                .getResourceAsStream(DEPENDENCIES_CONFIG)) {
            if (is == null) {
                LOGGER.warn("Database dependencies configuration not found: {}", DEPENDENCIES_CONFIG);
                return;
            }
            
            JsonObject config = new JsonObject(new String(is.readAllBytes()));
            JsonObject dbDeps = config.getJsonObject("databaseDependencies");
            
            for (String dbType : dbDeps.fieldNames()) {
                JsonObject depConfig = dbDeps.getJsonObject(dbType);
                DatabaseDependency dependency = new DatabaseDependency();
                dependency.setName(dbType);
                dependency.setDescription(depConfig.getString("description"));
                dependency.setRequired(depConfig.getBoolean("required", false));
                
                List<MavenDependency> mavenDeps = new ArrayList<>();
                for (Object depObj : depConfig.getJsonArray("dependencies")) {
                    JsonObject dep = (JsonObject) depObj;
                    MavenDependency mavenDep = new MavenDependency();
                    mavenDep.setGroupId(dep.getString("groupId"));
                    mavenDep.setArtifactId(dep.getString("artifactId"));
                    mavenDep.setVersion(dep.getString("version"));
                    mavenDeps.add(mavenDep);
                }
                dependency.setDependencies(mavenDeps);
                
                dependencies.put(dbType, dependency);
            }
            
            LOGGER.info("Loaded {} database dependencies", dependencies.size());
            
        } catch (Exception e) {
            LOGGER.error("Failed to load database dependencies configuration", e);
        }
    }
    
    /**
     * 获取数据库依赖信息
     */
    public static DatabaseDependency getDependency(String databaseType) {
        return dependencies.get(databaseType);
    }
    
    /**
     * 获取所有支持的数据库类型
     */
    public static Set<String> getSupportedDatabases() {
        return dependencies.keySet();
    }
    
    /**
     * 检查数据库类型是否支持
     */
    public static boolean isSupported(String databaseType) {
        return dependencies.containsKey(databaseType);
    }
    
    /**
     * 检查数据库类型是否支持
     */
    public static boolean isSupported(JDBCType jdbcType) {
        return isSupported(jdbcType.name().toLowerCase());
    }
    
    /**
     * 获取数据库依赖的Maven坐标
     */
    public static List<MavenDependency> getMavenDependencies(String databaseType) {
        DatabaseDependency dep = dependencies.get(databaseType);
        return dep != null ? dep.getDependencies() : Collections.emptyList();
    }
    
    /**
     * 生成Maven依赖XML
     */
    public static String generateMavenDependencyXml(String databaseType) {
        List<MavenDependency> deps = getMavenDependencies(databaseType);
        if (deps.isEmpty()) {
            return "<!-- No dependencies found for " + databaseType + " -->";
        }
        
        StringBuilder xml = new StringBuilder();
        xml.append("<!-- ").append(databaseType.toUpperCase()).append(" Database Dependencies -->\n");
        
        for (MavenDependency dep : deps) {
            xml.append("<dependency>\n");
            xml.append("    <groupId>").append(dep.getGroupId()).append("</groupId>\n");
            xml.append("    <artifactId>").append(dep.getArtifactId()).append("</artifactId>\n");
            if (dep.getVersion() != null) {
                xml.append("    <version>").append(dep.getVersion()).append("</version>\n");
            }
            xml.append("</dependency>\n");
        }
        
        return xml.toString();
    }
    
    /**
     * 检查类是否可用（用于运行时检查依赖）
     */
    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 检查数据库驱动是否可用
     */
    public static boolean isDatabaseDriverAvailable(JDBCType jdbcType) {
        switch (jdbcType) {
            case MySQL:
                return isClassAvailable("com.mysql.cj.jdbc.Driver");
            case PostgreSQL:
                return isClassAvailable("org.postgresql.Driver");
            case H2DB:
                return isClassAvailable("org.h2.Driver");
            default:
                return false;
        }
    }
    
    /**
     * 数据库依赖信息
     */
    public static class DatabaseDependency {
        private String name;
        private String description;
        private boolean required;
        private List<MavenDependency> dependencies;
        
        // Getters and Setters
        public String getUsername() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public List<MavenDependency> getDependencies() { return dependencies; }
        public void setDependencies(List<MavenDependency> dependencies) { this.dependencies = dependencies; }
    }
    
    /**
     * Maven依赖信息
     */
    public static class MavenDependency {
        private String groupId;
        private String artifactId;
        private String version;
        
        // Getters and Setters
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        
        public String getArtifactId() { return artifactId; }
        public void setArtifactId(String artifactId) { this.artifactId = artifactId; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}
