package cn.qaiu.generator.config;

import cn.qaiu.generator.model.DaoStyle;

/**
 * 功能配置
 * 
 * @author QAIU
 */
public class FeatureConfig {
    
    private boolean lombokEnabled = false;
    private boolean jpaEnabled = false;
    private boolean validationEnabled = true;
    private boolean swaggerEnabled = false;
    private boolean mybatisPlusEnabled = false;
    private boolean jooqEnabled = false;
    private boolean vertxSqlEnabled = false;
    
    // 生成控制标志
    private boolean generateEntity = true;
    private boolean generateDao = true;
    private boolean generateService = true;
    private boolean generateController = true;
    private boolean generateDto = true;
    private boolean generateComments = true;
    
    // DAO 风格
    private DaoStyle daoStyle = DaoStyle.LAMBDA;
    
    public FeatureConfig() {
    }
    
    public boolean isLombokEnabled() {
        return lombokEnabled;
    }
    
    public FeatureConfig setLombokEnabled(boolean lombokEnabled) {
        this.lombokEnabled = lombokEnabled;
        return this;
    }
    
    public boolean isJpaEnabled() {
        return jpaEnabled;
    }
    
    public FeatureConfig setJpaEnabled(boolean jpaEnabled) {
        this.jpaEnabled = jpaEnabled;
        return this;
    }
    
    public boolean isValidationEnabled() {
        return validationEnabled;
    }
    
    public FeatureConfig setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
        return this;
    }
    
    public boolean isSwaggerEnabled() {
        return swaggerEnabled;
    }
    
    public FeatureConfig setSwaggerEnabled(boolean swaggerEnabled) {
        this.swaggerEnabled = swaggerEnabled;
        return this;
    }
    
    public boolean isMybatisPlusEnabled() {
        return mybatisPlusEnabled;
    }
    
    public FeatureConfig setMybatisPlusEnabled(boolean mybatisPlusEnabled) {
        this.mybatisPlusEnabled = mybatisPlusEnabled;
        return this;
    }
    
    public boolean isJooqEnabled() {
        return jooqEnabled;
    }
    
    public FeatureConfig setJooqEnabled(boolean jooqEnabled) {
        this.jooqEnabled = jooqEnabled;
        return this;
    }
    
    public boolean isVertxSqlEnabled() {
        return vertxSqlEnabled;
    }
    
    public FeatureConfig setVertxSqlEnabled(boolean vertxSqlEnabled) {
        this.vertxSqlEnabled = vertxSqlEnabled;
        return this;
    }
    
    // 生成控制方法
    public boolean isGenerateEntity() {
        return generateEntity;
    }
    
    public FeatureConfig setGenerateEntity(boolean generateEntity) {
        this.generateEntity = generateEntity;
        return this;
    }
    
    public boolean isGenerateDao() {
        return generateDao;
    }
    
    public FeatureConfig setGenerateDao(boolean generateDao) {
        this.generateDao = generateDao;
        return this;
    }
    
    public boolean isGenerateService() {
        return generateService;
    }
    
    public FeatureConfig setGenerateService(boolean generateService) {
        this.generateService = generateService;
        return this;
    }
    
    public boolean isGenerateController() {
        return generateController;
    }
    
    public FeatureConfig setGenerateController(boolean generateController) {
        this.generateController = generateController;
        return this;
    }
    
    public boolean isGenerateDto() {
        return generateDto;
    }
    
    public FeatureConfig setGenerateDto(boolean generateDto) {
        this.generateDto = generateDto;
        return this;
    }
    
    public boolean isGenerateComments() {
        return generateComments;
    }
    
    public FeatureConfig setGenerateComments(boolean generateComments) {
        this.generateComments = generateComments;
        return this;
    }
    
    public boolean isGenerateValidation() {
        return validationEnabled;
    }
    
    public FeatureConfig setGenerateValidation(boolean generateValidation) {
        this.validationEnabled = generateValidation;
        return this;
    }
    
    public DaoStyle getDaoStyle() {
        return daoStyle;
    }
    
    public FeatureConfig setDaoStyle(DaoStyle daoStyle) {
        this.daoStyle = daoStyle;
        return this;
    }
    
    // 别名方法
    public boolean isUseLombok() {
        return lombokEnabled;
    }
    
    public FeatureConfig setUseLombok(boolean useLombok) {
        this.lombokEnabled = useLombok;
        return this;
    }
    
    public boolean isUseJpaAnnotations() {
        return jpaEnabled;
    }
    
    public FeatureConfig setUseJpaAnnotations(boolean useJpaAnnotations) {
        this.jpaEnabled = useJpaAnnotations;
        return this;
    }
    
    public boolean isUseVertxAnnotations() {
        return vertxSqlEnabled;
    }
    
    public FeatureConfig setUseVertxAnnotations(boolean useVertxAnnotations) {
        this.vertxSqlEnabled = useVertxAnnotations;
        return this;
    }
}