package cn.qaiu.generator.builder;

import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.FieldInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO 构建器
 * 生成请求/响应数据传输对象
 * 
 * @author QAIU
 */
public class DtoBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DtoBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public DtoBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建创建请求 DTO 信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return 创建请求 DTO 信息
     */
    public EntityInfo buildCreateRequestDto(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building CreateRequest DTO for table: {}", tableInfo.getTableName());
        
        EntityInfo dtoInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "CreateRequest";
        dtoInfo.setClassName(className);
        dtoInfo.setTableName(tableInfo.getTableName());
        dtoInfo.setDescription(entityInfo.getDescription() + "创建请求");
        dtoInfo.setPackageName(packageConfig.getDtoPackage());
        
        // 构建字段信息（排除主键和自动生成字段）
        List<FieldInfo> fields = buildCreateRequestFields(entityInfo.getFields());
        dtoInfo.setFields(fields);
        
        // 构建导入语句
        Set<String> imports = buildDtoImports(fields);
        dtoInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("CreateRequest DTO built successfully: {}", className);
        return dtoInfo;
    }
    
    /**
     * 构建更新请求 DTO 信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return 更新请求 DTO 信息
     */
    public EntityInfo buildUpdateRequestDto(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building UpdateRequest DTO for table: {}", tableInfo.getTableName());
        
        EntityInfo dtoInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "UpdateRequest";
        dtoInfo.setClassName(className);
        dtoInfo.setTableName(tableInfo.getTableName());
        dtoInfo.setDescription(entityInfo.getDescription() + "更新请求");
        dtoInfo.setPackageName(packageConfig.getDtoPackage());
        
        // 构建字段信息（包含主键）
        List<FieldInfo> fields = buildUpdateRequestFields(entityInfo.getFields());
        dtoInfo.setFields(fields);
        
        // 构建导入语句
        Set<String> imports = buildDtoImports(fields);
        dtoInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("UpdateRequest DTO built successfully: {}", className);
        return dtoInfo;
    }
    
    /**
     * 构建响应 DTO 信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return 响应 DTO 信息
     */
    public EntityInfo buildResponseDto(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building Response DTO for table: {}", tableInfo.getTableName());
        
        EntityInfo dtoInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "Response";
        dtoInfo.setClassName(className);
        dtoInfo.setTableName(tableInfo.getTableName());
        dtoInfo.setDescription(entityInfo.getDescription() + "响应");
        dtoInfo.setPackageName(packageConfig.getDtoPackage());
        
        // 构建字段信息（包含所有字段）
        List<FieldInfo> fields = buildResponseFields(entityInfo.getFields());
        dtoInfo.setFields(fields);
        
        // 构建导入语句
        Set<String> imports = buildDtoImports(fields);
        dtoInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("Response DTO built successfully: {}", className);
        return dtoInfo;
    }
    
    /**
     * 构建 DTO 转换器信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return DTO 转换器信息
     */
    public EntityInfo buildDtoConverter(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building DTO Converter for table: {}", tableInfo.getTableName());
        
        EntityInfo converterInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "DtoConverter";
        converterInfo.setClassName(className);
        converterInfo.setTableName(tableInfo.getTableName());
        converterInfo.setDescription(entityInfo.getDescription() + "DTO转换器");
        converterInfo.setPackageName(packageConfig.getDtoPackage());
        
        // 设置字段信息（转换器需要访问实体字段）
        converterInfo.setFields(entityInfo.getFields());
        
        // 构建导入语句
        Set<String> imports = buildConverterImports(entityInfo);
        converterInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("DTO Converter built successfully: {}", className);
        return converterInfo;
    }
    
    /**
     * 构建创建请求字段（排除主键和自动生成字段）
     */
    private List<FieldInfo> buildCreateRequestFields(List<FieldInfo> entityFields) {
        List<FieldInfo> fields = new ArrayList<>();
        
        for (FieldInfo field : entityFields) {
            // 跳过主键字段
            if (field.isPrimaryKey()) {
                continue;
            }
            
            // 跳过自动生成字段（如创建时间、更新时间等）
            if (isAutoGeneratedField(field.getFieldName())) {
                continue;
            }
            
            fields.add(field);
        }
        
        return fields;
    }
    
    /**
     * 构建更新请求字段（包含主键）
     */
    private List<FieldInfo> buildUpdateRequestFields(List<FieldInfo> entityFields) {
        List<FieldInfo> fields = new ArrayList<>();
        
        for (FieldInfo field : entityFields) {
            // 跳过自动生成字段
            if (isAutoGeneratedField(field.getFieldName())) {
                continue;
            }
            
            fields.add(field);
        }
        
        return fields;
    }
    
    /**
     * 构建响应字段（包含所有字段）
     */
    private List<FieldInfo> buildResponseFields(List<FieldInfo> entityFields) {
        return new ArrayList<>(entityFields);
    }
    
    /**
     * 检查是否为自动生成字段
     */
    private boolean isAutoGeneratedField(String fieldName) {
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("create_time") ||
               lowerFieldName.contains("update_time") ||
               lowerFieldName.contains("created_at") ||
               lowerFieldName.contains("updated_at");
    }
    
    /**
     * 构建 DTO 导入语句
     */
    private Set<String> buildDtoImports(List<FieldInfo> fields) {
        Set<String> imports = new HashSet<>();
        
        // 基础导入
        imports.add("java.time.LocalDateTime");
        imports.add("java.time.LocalDate");
        imports.add("java.time.LocalTime");
        imports.add("java.math.BigDecimal");
        imports.add("java.util.Objects");
        
        // 根据字段类型添加导入
        for (FieldInfo field : fields) {
            String fieldType = field.getFieldType();
            
            if ("BigDecimal".equals(fieldType)) {
                imports.add("java.math.BigDecimal");
            } else if ("LocalDateTime".equals(fieldType)) {
                imports.add("java.time.LocalDateTime");
            } else if ("LocalDate".equals(fieldType)) {
                imports.add("java.time.LocalDate");
            } else if ("LocalTime".equals(fieldType)) {
                imports.add("java.time.LocalTime");
            }
        }
        
        // 如果启用参数校验，添加校验注解导入
        if (featureConfig.isGenerateValidation()) {
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
            imports.add("jakarta.validation.constraints.Size");
            imports.add("jakarta.validation.constraints.Email");
            imports.add("jakarta.validation.constraints.Pattern");
        }
        
        // 如果启用 Lombok，添加 Lombok 注解导入
        if (featureConfig.isUseLombok()) {
            imports.add("lombok.Data");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
        }
        
        return imports;
    }
    
    /**
     * 构建转换器导入语句
     */
    private Set<String> buildConverterImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // DTO 类导入
        String createRequestClassName = entityInfo.getClassName() + "CreateRequest";
        String updateRequestClassName = entityInfo.getClassName() + "UpdateRequest";
        String responseClassName = entityInfo.getClassName() + "Response";
        
        imports.add(packageConfig.getDtoPackage() + "." + createRequestClassName);
        imports.add(packageConfig.getDtoPackage() + "." + updateRequestClassName);
        imports.add(packageConfig.getDtoPackage() + "." + responseClassName);
        
        // 基础导入
        imports.add("java.util.List");
        imports.add("java.util.stream.Collectors");
        
        return imports;
    }
    
    /**
     * 检查是否需要生成 DTO
     */
    public boolean shouldGenerateDto() {
        return featureConfig.isGenerateDto();
    }
    
    /**
     * 获取 DTO 包名
     */
    public String getDtoPackageName() {
        return packageConfig.getDtoPackage();
    }
}
