package cn.qaiu.generator.cli;

import cn.qaiu.generator.config.*;
import cn.qaiu.generator.core.CodeGeneratorFacade;
import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.generator.model.GeneratorContext;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 代码生成器命令行工具
 * 
 * @author QAIU
 */
@Command(name = "vxcore-generator", 
         description = "VXCore 代码生成器 - 基于数据库表结构生成三层架构代码",
         mixinStandardHelpOptions = true,
         version = "1.0.0")
public class GeneratorCli implements Callable<Integer> {
    
    // 数据库配置
    @Option(names = {"--db-url"}, description = "数据库连接URL")
    private String dbUrl;
    
    @Option(names = {"--db-user"}, description = "数据库用户名")
    private String dbUser;
    
    @Option(names = {"--db-password"}, description = "数据库密码")
    private String dbPassword;
    
    @Option(names = {"--db-schema"}, description = "数据库模式名")
    private String dbSchema;
    
    // 表配置
    @Parameters(description = "要生成的表名列表")
    private List<String> tables = new ArrayList<>();
    
    // 包配置
    @Option(names = {"--package"}, description = "基础包名")
    private String basePackage = "com.example";
    
    // 输出配置
    @Option(names = {"--output"}, description = "输出目录")
    private String outputPath = "./src/main/java";
    
    // DAO 风格
    @Option(names = {"--dao-style"}, description = "DAO风格: vertx, jooq, lambda")
    private String daoStyle = "lambda";
    
    // 配置文件
    @Option(names = {"--config"}, description = "配置文件路径")
    private String configPath;
    
    // 功能开关
    @Option(names = {"--no-entity"}, description = "不生成实体类")
    private boolean noEntity = false;
    
    @Option(names = {"--no-dao"}, description = "不生成DAO")
    private boolean noDao = false;
    
    @Option(names = {"--no-service"}, description = "不生成Service")
    private boolean noService = false;
    
    @Option(names = {"--no-controller"}, description = "不生成Controller")
    private boolean noController = false;
    
    @Option(names = {"--no-dto"}, description = "不生成DTO")
    private boolean noDto = false;
    
    // 注解支持
    @Option(names = {"--lombok"}, description = "使用Lombok注解")
    private boolean useLombok = false;
    
    @Option(names = {"--jpa"}, description = "使用JPA注解")
    private boolean useJpa = false;
    
    @Option(names = {"--vertx"}, description = "使用Vert.x注解")
    private boolean useVertx = false;
    
    // 其他选项
    @Option(names = {"--overwrite"}, description = "覆盖已存在的文件")
    private boolean overwrite = false;
    
    @Option(names = {"--no-comments"}, description = "不生成注释")
    private boolean noComments = false;
    
    @Option(names = {"--no-validation"}, description = "不生成校验注解")
    private boolean noValidation = false;
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new GeneratorCli()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() throws Exception {
        try {
            System.out.println("VXCore 代码生成器启动...");
            
            // 验证参数
            if (!validateArguments()) {
                return 1;
            }
            
            // 创建配置
            GeneratorContext context = createContext();
            
            // 初始化 Vertx 实例到 VertxHolder
            Vertx vertx = Vertx.vertx();
            VertxHolder.init(vertx);
            
            // 创建生成器
            CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
            
            // 生成代码
            System.out.println("开始生成代码...");
            List<String> generatedFiles = generator.generateAll(tables).toCompletionStage().toCompletableFuture().get();
            
            System.out.println("代码生成完成！");
            System.out.println("生成的文件数量: " + generatedFiles.size());
            System.out.println("输出目录: " + outputPath);
            
            // 关闭资源
            generator.close().toCompletionStage().toCompletableFuture().get();
            vertx.close().toCompletionStage().toCompletableFuture().get();
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("代码生成失败: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
    
    /**
     * 验证命令行参数
     */
    private boolean validateArguments() {
        // 检查数据库配置或配置文件
        if (configPath == null) {
            if (dbUrl == null || dbUser == null) {
                System.err.println("错误: 必须指定数据库连接信息或配置文件");
                System.err.println("使用 --help 查看帮助信息");
                return false;
            }
        } else {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                System.err.println("错误: 配置文件不存在: " + configPath);
                return false;
            }
        }
        
        // 检查表名列表
        if (tables.isEmpty()) {
            System.err.println("错误: 必须指定要生成的表名");
            System.err.println("使用 --help 查看帮助信息");
            return false;
        }
        
        // 检查输出目录
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("错误: 无法创建输出目录: " + outputPath);
                return false;
            }
        }
        
        // 检查 DAO 风格
        try {
            DaoStyle.fromCode(daoStyle);
        } catch (IllegalArgumentException e) {
            System.err.println("错误: 不支持的 DAO 风格: " + daoStyle);
            System.err.println("支持的风格: vertx, jooq, lambda");
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建生成上下文
     */
    private GeneratorContext createContext() {
        // 数据库配置
        DatabaseConfig databaseConfig = null;
        if (configPath == null) {
            databaseConfig = new DatabaseConfig(dbUrl, dbUser, dbPassword);
            databaseConfig.setSchema(dbSchema);
        }
        
        // 包配置
        PackageConfig packageConfig = new PackageConfig(basePackage);
        
        // 模板配置
        TemplateConfig templateConfig = new TemplateConfig();
        
        // 输出配置
        OutputConfig outputConfig = new OutputConfig(outputPath);
        outputConfig.setOverwriteExisting(overwrite);
        
        // 功能配置
        FeatureConfig featureConfig = new FeatureConfig();
        featureConfig.setGenerateEntity(!noEntity);
        featureConfig.setGenerateDao(!noDao);
        featureConfig.setGenerateService(!noService);
        featureConfig.setGenerateController(!noController);
        featureConfig.setGenerateDto(!noDto);
        featureConfig.setGenerateComments(!noComments);
        featureConfig.setGenerateValidation(!noValidation);
        featureConfig.setDaoStyle(DaoStyle.fromCode(daoStyle));
        featureConfig.setUseLombok(useLombok);
        featureConfig.setUseJpaAnnotations(useJpa);
        featureConfig.setUseVertxAnnotations(useVertx);
        
        // 创建上下文
        GeneratorContext context = GeneratorContext.builder()
                .databaseConfig(databaseConfig)
                .packageConfig(packageConfig)
                .templateConfig(templateConfig)
                .outputConfig(outputConfig)
                .featureConfig(featureConfig)
                .build();
        
        // 设置自定义属性
        if (configPath != null) {
            context.setCustomProperty("configPath", configPath);
        }
        
        return context;
    }
}
