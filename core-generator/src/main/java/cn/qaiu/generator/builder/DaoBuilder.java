package cn.qaiu.generator.builder;

import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.FieldInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO 层构建器
 * 支持三种风格: Vert.x SQL, jOOQ, MP Lambda
 * 
 * @author QAIU
 */
public class DaoBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DaoBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public DaoBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建 DAO 信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return DAO 信息
     */
    public EntityInfo buildDao(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building DAO for table: {} with style: {}", 
                   tableInfo.getTableName(), featureConfig.getDaoStyle());
        
        EntityInfo daoInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "Dao";
        daoInfo.setClassName(className);
        daoInfo.setTableName(tableInfo.getTableName());
        daoInfo.setDescription(entityInfo.getDescription() + "数据访问对象");
        daoInfo.setPackageName(packageConfig.getDaoPackage());
        
        // 构建导入语句
        Set<String> imports = buildImports(entityInfo);
        daoInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("DAO built successfully: {}", className);
        return daoInfo;
    }
    
    /**
     * 构建导入语句
     */
    private Set<String> buildImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("java.util.List");
        imports.add("java.util.Optional");
        
        DaoStyle daoStyle = featureConfig.getDaoStyle();
        
        if (daoStyle.isVertxSql()) {
            // Vert.x SQL 风格导入
            imports.add("io.vertx.sqlclient.SqlClient");
            imports.add("io.vertx.sqlclient.Tuple");
            imports.add("io.vertx.sqlclient.Row");
            imports.add("io.vertx.sqlclient.RowSet");
            imports.add("io.vertx.sqlclient.SqlTemplate");
            imports.add("java.util.function.Function");
            
        } else if (daoStyle.isJooq()) {
            // jOOQ 风格导入
            imports.add("org.jooq.DSLContext");
            imports.add("org.jooq.Record");
            imports.add("org.jooq.Result");
            imports.add("org.jooq.SelectConditionStep");
            imports.add("org.jooq.SelectJoinStep");
            imports.add("org.jooq.impl.DSL");
            
            // 添加 jOOQ 生成的表类导入
            String tableClassName = entityInfo.getClassName().toUpperCase();
            imports.add("org.jooq.generated.tables." + tableClassName);
            imports.add("org.jooq.generated.tables.records." + tableClassName + "Record");
            
        } else if (daoStyle.isLambda()) {
            // MP Lambda 风格导入
            imports.add("cn.qaiu.db.dsl.core.LambdaDao");
            imports.add("cn.qaiu.db.dsl.core.LambdaQueryWrapper");
        }
        
        return imports;
    }
    
    /**
     * 获取 DAO 类名
     */
    public String getDaoClassName(String entityClassName) {
        return entityClassName + "Dao";
    }
    
    /**
     * 获取 DAO 包名
     */
    public String getDaoPackageName() {
        return packageConfig.getDaoPackage();
    }
    
    /**
     * 获取 DAO 完整类名
     */
    public String getDaoFullClassName(String entityClassName) {
        return packageConfig.getDaoPackage() + "." + getDaoClassName(entityClassName);
    }
    
    /**
     * 检查是否需要生成 DAO
     */
    public boolean shouldGenerateDao() {
        return featureConfig.isGenerateDao();
    }
    
    /**
     * 获取 DAO 风格
     */
    public DaoStyle getDaoStyle() {
        return featureConfig.getDaoStyle();
    }
}
