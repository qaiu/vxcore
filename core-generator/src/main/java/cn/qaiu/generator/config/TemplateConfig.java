package cn.qaiu.generator.config;

/**
 * 模板配置
 * 
 * @author QAIU
 */
public class TemplateConfig {
    
    private String templatePath;
    private String entityTemplate;
    private String daoTemplate;
    private String serviceTemplate;
    private String controllerTemplate;
    private String dtoTemplate;
    
    public TemplateConfig() {
        this.templatePath = "templates";
        this.entityTemplate = "entity.ftl";
        this.daoTemplate = "dao.ftl";
        this.serviceTemplate = "service.ftl";
        this.controllerTemplate = "controller.ftl";
        this.dtoTemplate = "dto.ftl";
    }
    
    public String getTemplatePath() {
        return templatePath;
    }
    
    public TemplateConfig setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }
    
    public String getEntityTemplate() {
        return entityTemplate;
    }
    
    public TemplateConfig setEntityTemplate(String entityTemplate) {
        this.entityTemplate = entityTemplate;
        return this;
    }
    
    public String getDaoTemplate() {
        return daoTemplate;
    }
    
    public TemplateConfig setDaoTemplate(String daoTemplate) {
        this.daoTemplate = daoTemplate;
        return this;
    }
    
    public String getServiceTemplate() {
        return serviceTemplate;
    }
    
    public TemplateConfig setServiceTemplate(String serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
        return this;
    }
    
    public String getControllerTemplate() {
        return controllerTemplate;
    }
    
    public TemplateConfig setControllerTemplate(String controllerTemplate) {
        this.controllerTemplate = controllerTemplate;
        return this;
    }
    
    public String getDtoTemplate() {
        return dtoTemplate;
    }
    
    public TemplateConfig setDtoTemplate(String dtoTemplate) {
        this.dtoTemplate = dtoTemplate;
        return this;
    }
}