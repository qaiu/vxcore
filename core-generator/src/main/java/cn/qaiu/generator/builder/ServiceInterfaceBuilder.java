package cn.qaiu.generator.builder;

import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.generator.model.GeneratorContext;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Service接口生成器
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceInterfaceBuilder {

  private final Configuration freemarkerConfig;

  public ServiceInterfaceBuilder() {
    this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
    this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
  }

  /** 生成Service接口 */
  public void generateServiceInterface(GeneratorContext context, String entityName)
      throws IOException, TemplateException {
    String serviceName = entityName + "Service";
    String packageName = context.getPackageConfig().getServicePackage();

    // 准备模板数据
    Map<String, Object> data = new HashMap<>();
    data.put("packageName", packageName);
    data.put("serviceName", serviceName);
    data.put("entityName", entityName);
    data.put("entityPackage", context.getPackageConfig().getEntityPackage());
    data.put(
        "daoStyle",
        context.getFeatureConfig() != null
            ? context.getFeatureConfig().getDaoStyle()
            : DaoStyle.JOOQ);

    // 生成Service接口
    String outputDir = context.getOutputConfig().getOutputPath();
    generateFromTemplate(
        "service-interface.ftl",
        data,
        getOutputPath(outputDir, packageName, serviceName + ".java"));
  }

  /** 从模板生成文件 */
  private void generateFromTemplate(String templateName, Map<String, Object> data, Path outputPath)
      throws IOException, TemplateException {

    Template template = freemarkerConfig.getTemplate(templateName);

    // 确保输出目录存在
    Files.createDirectories(outputPath.getParent());

    try (Writer writer = new FileWriter(outputPath.toFile())) {
      template.process(data, writer);
    }
  }

  /** 获取输出路径 */
  private Path getOutputPath(String outputDir, String packageName, String fileName) {
    String packagePath = packageName.replace('.', '/');
    return Paths.get(outputDir, "src/main/java", packagePath, fileName);
  }
}
