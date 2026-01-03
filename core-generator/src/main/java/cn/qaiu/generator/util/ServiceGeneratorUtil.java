package cn.qaiu.generator.util;

import cn.qaiu.generator.builder.ServiceInterfaceBuilder;
import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.OutputConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.generator.model.GeneratorContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service接口生成工具
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceGeneratorUtil {

  private final ServiceInterfaceBuilder serviceBuilder;

  public ServiceGeneratorUtil() {
    this.serviceBuilder = new ServiceInterfaceBuilder();
  }

  /** 为指定实体生成Service接口 */
  public void generateServiceForEntity(String entityName, String packageName, String outputDir) {
    try {
      PackageConfig packageConfig = new PackageConfig(packageName);
      OutputConfig outputConfig = new OutputConfig(outputDir);
      FeatureConfig featureConfig = new FeatureConfig().setDaoStyle(DaoStyle.JOOQ);

      GeneratorContext context =
          GeneratorContext.builder()
              .packageConfig(packageConfig)
              .outputConfig(outputConfig)
              .featureConfig(featureConfig)
              .build();

      serviceBuilder.generateServiceInterface(context, entityName);

      System.out.println("✅ 成功生成 " + entityName + "Service 接口");

    } catch (Exception e) {
      System.err.println("❌ 生成 " + entityName + "Service 接口失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /** 批量生成Service接口 */
  public void generateServicesForEntities(
      String packageName, String outputDir, String... entityNames) {
    System.out.println("🚀 开始批量生成Service接口...");

    for (String entityName : entityNames) {
      generateServiceForEntity(entityName, packageName, outputDir);
    }

    System.out.println("🎉 批量生成完成！");
  }

  /** 从实体类文件自动扫描并生成Service接口 */
  public void generateServicesFromEntityDirectory(
      String entityDir, String packageName, String outputDir) {
    try {
      Path entityPath = Paths.get(entityDir);
      if (!Files.exists(entityPath)) {
        System.err.println("❌ 实体目录不存在: " + entityDir);
        return;
      }

      List<String> entityNames = new ArrayList<>();

      // 扫描所有Java文件
      Files.walk(entityPath)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(
              path -> {
                String fileName = path.getFileName().toString();
                String entityName = fileName.substring(0, fileName.lastIndexOf('.'));
                entityNames.add(entityName);
              });

      if (entityNames.isEmpty()) {
        System.out.println("⚠️ 未找到实体类文件");
        return;
      }

      System.out.println("📋 发现实体类: " + String.join(", ", entityNames));
      generateServicesForEntities(packageName, outputDir, entityNames.toArray(new String[0]));

    } catch (IOException e) {
      System.err.println("❌ 扫描实体目录失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /** 主方法 - 可以直接运行 */
  public static void main(String[] args) {
    ServiceGeneratorUtil generator = new ServiceGeneratorUtil();

    if (args.length >= 3) {
      String entityDir = args[0];
      String packageName = args[1];
      String outputDir = args[2];

      generator.generateServicesFromEntityDirectory(entityDir, packageName, outputDir);
    } else {
      // 默认示例
      System.out.println("🔧 Service接口生成工具");
      System.out.println("用法: java ServiceGeneratorUtil <entityDir> <packageName> <outputDir>");
      System.out.println();
      System.out.println("示例:");
      System.out.println(
          "  java ServiceGeneratorUtil src/main/java/cn/qaiu/example/entity cn.qaiu.example .");
      System.out.println();

      // 为示例项目生成Service接口
      generator.generateServicesForEntities(
          "cn.qaiu.example", ".", "User", "Product", "Order", "OrderDetail");
    }
  }
}
