package cn.qaiu.vx.core.codegen;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代码生成器CLI工具
 * 提供命令行接口来生成代码
 * 
 * @author QAIU
 */
public class CodeGenCli {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenCli.class);
    
    private final CodeGenerator codeGenerator;
    
    public CodeGenCli(Vertx vertx) {
        this.codeGenerator = new CodeGenerator(vertx, new GeneratorConfig());
    }
    
    /**
     * 生成实体类
     * 
     * @param tableName 表名
     * @param className 类名
     * @param packageName 包名
     * @param outputPath 输出路径
     * @return 生成结果
     */
    public CodeGenResult generateEntity(String tableName, String className, String packageName, String outputPath) {
        try {
            // 参数验证
            if (tableName == null || tableName.trim().isEmpty()) {
                return CodeGenResult.failure("表名不能为空");
            }
            if (className == null || className.trim().isEmpty()) {
                return CodeGenResult.failure("类名不能为空");
            }
            if (packageName == null || packageName.trim().isEmpty()) {
                return CodeGenResult.failure("包名不能为空");
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                return CodeGenResult.failure("输出路径不能为空");
            }
            
            // 创建实体信息
            EntityInfo entityInfo = createEntityInfo(tableName, className, packageName);
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo(packageName);
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(codeGenerator.getGeneratorConfig());
            
            // 生成代码
            String result = codeGenerator.generateToString("entity.ftl", context).result();
            
            // 写入文件
            String fullPath = outputPath + "/" + packageInfo.getEntityPackagePath() + "/" + className + ".java";
            java.nio.file.Path filePath = java.nio.file.Paths.get(fullPath);
            java.nio.file.Files.createDirectories(filePath.getParent());
            java.nio.file.Files.write(filePath, result.getBytes());
            
            return CodeGenResult.success("Entity generated successfully: " + fullPath);
            
        } catch (Exception e) {
            LOGGER.error("生成实体类失败", e);
            return CodeGenResult.failure("Failed to generate entity: " + e.getMessage());
        }
    }
    
    /**
     * 生成DAO类
     * 
     * @param tableName 表名
     * @param className 类名
     * @param packageName 包名
     * @param outputPath 输出路径
     * @return 生成结果
     */
    public CodeGenResult generateDao(String tableName, String className, String packageName, String outputPath) {
        try {
            // 参数验证
            if (tableName == null || tableName.trim().isEmpty()) {
                return CodeGenResult.failure("表名不能为空");
            }
            if (className == null || className.trim().isEmpty()) {
                return CodeGenResult.failure("类名不能为空");
            }
            if (packageName == null || packageName.trim().isEmpty()) {
                return CodeGenResult.failure("包名不能为空");
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                return CodeGenResult.failure("输出路径不能为空");
            }
            
            // 创建实体信息
            EntityInfo entityInfo = createEntityInfo(tableName, className, packageName);
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo(packageName);
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(codeGenerator.getGeneratorConfig());
            
            // 生成代码
            String result = codeGenerator.generateToString("dao.ftl", context).result();
            
            // 写入文件
            String fullPath = outputPath + "/" + packageInfo.getDaoPackagePath() + "/" + className + "Dao.java";
            java.nio.file.Path filePath = java.nio.file.Paths.get(fullPath);
            java.nio.file.Files.createDirectories(filePath.getParent());
            java.nio.file.Files.write(filePath, result.getBytes());
            
            return CodeGenResult.success("DAO generated successfully: " + fullPath);
            
        } catch (Exception e) {
            LOGGER.error("生成DAO类失败", e);
            return CodeGenResult.failure("Failed to generate DAO: " + e.getMessage());
        }
    }
    
    /**
     * 生成控制器类
     * 
     * @param tableName 表名
     * @param className 类名
     * @param packageName 包名
     * @param outputPath 输出路径
     * @return 生成结果
     */
    public CodeGenResult generateController(String tableName, String className, String packageName, String outputPath) {
        try {
            // 参数验证
            if (tableName == null || tableName.trim().isEmpty()) {
                return CodeGenResult.failure("表名不能为空");
            }
            if (className == null || className.trim().isEmpty()) {
                return CodeGenResult.failure("类名不能为空");
            }
            if (packageName == null || packageName.trim().isEmpty()) {
                return CodeGenResult.failure("包名不能为空");
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                return CodeGenResult.failure("输出路径不能为空");
            }
            
            // 创建实体信息
            EntityInfo entityInfo = createEntityInfo(tableName, className, packageName);
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo(packageName);
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(codeGenerator.getGeneratorConfig());
            
            // 生成代码
            String result = codeGenerator.generateToString("controller.ftl", context).result();
            
            // 写入文件
            String fullPath = outputPath + "/" + packageInfo.getControllerPackagePath() + "/" + className + "Controller.java";
            java.nio.file.Path filePath = java.nio.file.Paths.get(fullPath);
            java.nio.file.Files.createDirectories(filePath.getParent());
            java.nio.file.Files.write(filePath, result.getBytes());
            
            return CodeGenResult.success("Controller generated successfully: " + fullPath);
            
        } catch (Exception e) {
            LOGGER.error("生成控制器类失败", e);
            return CodeGenResult.failure("Failed to generate Controller: " + e.getMessage());
        }
    }
    
    /**
     * 批量生成所有代码
     * 
     * @param tableName 表名
     * @param className 类名
     * @param packageName 包名
     * @param outputPath 输出路径
     * @return 生成结果
     */
    public CodeGenResult generateAll(String tableName, String className, String packageName, String outputPath) {
        try {
            // 参数验证
            if (tableName == null || tableName.trim().isEmpty()) {
                return CodeGenResult.failure("表名不能为空");
            }
            if (className == null || className.trim().isEmpty()) {
                return CodeGenResult.failure("类名不能为空");
            }
            if (packageName == null || packageName.trim().isEmpty()) {
                return CodeGenResult.failure("包名不能为空");
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                return CodeGenResult.failure("输出路径不能为空");
            }
            
            CodeGenResult entityResult = generateEntity(tableName, className, packageName, outputPath);
            if (!entityResult.isSuccess()) {
                return entityResult;
            }
            
            CodeGenResult daoResult = generateDao(tableName, className, packageName, outputPath);
            if (!daoResult.isSuccess()) {
                return daoResult;
            }
            
            CodeGenResult controllerResult = generateController(tableName, className, packageName, outputPath);
            if (!controllerResult.isSuccess()) {
                return controllerResult;
            }
            
            return CodeGenResult.success("All code generated successfully");
            
        } catch (Exception e) {
            LOGGER.error("批量生成代码失败", e);
            return CodeGenResult.failure("Failed to generate all code: " + e.getMessage());
        }
    }
    
    /**
     * 创建实体信息
     */
    private EntityInfo createEntityInfo(String tableName, String className, String packageName) {
        EntityInfo entityInfo = new EntityInfo(className, tableName);
        entityInfo.setPackageName(packageName + ".entity");
        entityInfo.setDescription("Generated entity for table " + tableName);
        
        // 添加默认字段
        FieldInfo idField = new FieldInfo("id", "Long");
        idField.setPrimaryKey(true);
        idField.setDescription("主键ID");
        entityInfo.addField(idField);
        
        // 添加创建时间字段
        FieldInfo createTimeField = new FieldInfo("createTime", "LocalDateTime");
        createTimeField.setDescription("创建时间");
        entityInfo.addField(createTimeField);
        
        // 添加更新时间字段
        FieldInfo updateTimeField = new FieldInfo("updateTime", "LocalDateTime");
        updateTimeField.setDescription("更新时间");
        entityInfo.addField(updateTimeField);
        
        // 添加必要的导入
        entityInfo.addImport("java.time.LocalDateTime");
        
        return entityInfo;
    }
    
    /**
     * 代码生成结果
     */
    public static class CodeGenResult {
        private final boolean success;
        private final String message;
        
        private CodeGenResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static CodeGenResult success(String message) {
            return new CodeGenResult(true, message);
        }
        
        public static CodeGenResult failure(String message) {
            return new CodeGenResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "CodeGenResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
