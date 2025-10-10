package cn.qaiu.vx.core.config;

import cn.qaiu.vx.core.annotaions.config.ConfigurationProperties;
import cn.qaiu.vx.core.annotaions.config.ConfigurationProperty;
import cn.qaiu.vx.core.util.ReflectionUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 配置元数据生成器
 * 扫描配置类并生成元数据信息，用于IDE自动提示
 * 
 * @author QAIU
 */
public class ConfigurationMetadataGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataGenerator.class);
    
    /**
     * 配置元数据
     */
    public static class PropertyMetadata {
        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
        private ConfigurationProperty.PropertyType type;
        private String[] allowedValues;
        private double minValue;
        private double maxValue;
        private String group;
        
        // Getters and setters
        public String getUsername() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public ConfigurationProperty.PropertyType getType() { return type; }
        public void setType(ConfigurationProperty.PropertyType type) { this.type = type; }
        
        public String[] getAllowedValues() { return allowedValues; }
        public void setAllowedValues(String[] allowedValues) { this.allowedValues = allowedValues; }
        
        public double getMinValue() { return minValue; }
        public void setMinValue(double minValue) { this.minValue = minValue; }
        
        public double getMaxValue() { return maxValue; }
        public void setMaxValue(double maxValue) { this.maxValue = maxValue; }
        
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
    }
    
    /**
     * 配置类元数据
     */
    public static class ConfigurationClassMetadata {
        private String className;
        private String prefix;
        private String description;
        private List<PropertyMetadata> properties;
        
        public ConfigurationClassMetadata() {
            this.properties = new ArrayList<>();
        }
        
        // Getters and setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<PropertyMetadata> getProperties() { return properties; }
        public void setProperties(List<PropertyMetadata> properties) { this.properties = properties; }
    }
    
    /**
     * 生成所有配置类的元数据
     * 
     * @return 配置元数据列表
     */
    public static List<ConfigurationClassMetadata> generateAllMetadata() {
        List<ConfigurationClassMetadata> metadataList = new ArrayList<>();
        Reflections reflections = ReflectionUtil.getReflections();
        
        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(ConfigurationProperties.class);
        for (Class<?> configClass : configClasses) {
            try {
                ConfigurationClassMetadata metadata = generateMetadata(configClass);
                metadataList.add(metadata);
                LOGGER.debug("Generated metadata for configuration class: {}", configClass.getSimpleName());
            } catch (Exception e) {
                LOGGER.error("Failed to generate metadata for configuration class: {}", configClass.getSimpleName(), e);
            }
        }
        
        return metadataList;
    }
    
    /**
     * 生成单个配置类的元数据
     * 
     * @param configClass 配置类
     * @return 配置元数据
     */
    public static ConfigurationClassMetadata generateMetadata(Class<?> configClass) {
        ConfigurationClassMetadata metadata = new ConfigurationClassMetadata();
        metadata.setClassName(configClass.getSimpleName());
        
        ConfigurationProperties classAnnotation = configClass.getAnnotation(ConfigurationProperties.class);
        if (classAnnotation != null) {
            metadata.setPrefix(classAnnotation.prefix());
            metadata.setDescription(classAnnotation.description());
        }
        
        // 扫描字段
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            PropertyMetadata propertyMetadata = extractFieldMetadata(field);
            if (propertyMetadata != null) {
                metadata.getProperties().add(propertyMetadata);
            }
        }
        
        // 扫描方法
        Method[] methods = configClass.getMethods();
        for (Method method : methods) {
            PropertyMetadata propertyMetadata = extractMethodMetadata(method);
            if (propertyMetadata != null) {
                metadata.getProperties().add(propertyMetadata);
            }
        }
        
        return metadata;
    }
    
    /**
     * 提取字段元数据
     */
    private static PropertyMetadata extractFieldMetadata(Field field) {
        ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
        if (annotation == null) {
            return null;
        }
        
        PropertyMetadata metadata = new PropertyMetadata();
        
        // 获取属性名
        String propertyName = annotation.value();
        if (propertyName.isEmpty()) {
            propertyName = field.getUsername();
        }
        metadata.setName(propertyName);
        
        // 设置其他属性
        metadata.setDescription(annotation.description());
        metadata.setDefaultValue(annotation.defaultValue());
        metadata.setRequired(annotation.required());
        metadata.setType(annotation.type());
        metadata.setAllowedValues(annotation.allowedValues());
        metadata.setMinValue(annotation.minValue());
        metadata.setMaxValue(annotation.maxValue());
        metadata.setGroup(annotation.group());
        
        return metadata;
    }
    
    /**
     * 提取方法元数据
     */
    private static PropertyMetadata extractMethodMetadata(Method method) {
        ConfigurationProperty annotation = method.getAnnotation(ConfigurationProperty.class);
        if (annotation == null) {
            return null;
        }
        
        PropertyMetadata metadata = new PropertyMetadata();
        
        // 获取属性名
        String propertyName = annotation.value();
        if (propertyName.isEmpty()) {
            // 从方法名推断属性名
            String methodName = method.getUsername();
            if (methodName.startsWith("get") || methodName.startsWith("set")) {
                propertyName = methodName.substring(3);
                propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
            } else {
                propertyName = methodName;
            }
        }
        metadata.setName(propertyName);
        
        // 设置其他属性
        metadata.setDescription(annotation.description());
        metadata.setDefaultValue(annotation.defaultValue());
        metadata.setRequired(annotation.required());
        metadata.setType(annotation.type());
        metadata.setAllowedValues(annotation.allowedValues());
        metadata.setMinValue(annotation.minValue());
        metadata.setMaxValue(annotation.maxValue());
        metadata.setGroup(annotation.group());
        
        return metadata;
    }
    
    /**
     * 生成JSON Schema格式的元数据
     * 
     * @return JSON Schema
     */
    public static JsonObject generateJsonSchema() {
        List<ConfigurationClassMetadata> metadataList = generateAllMetadata();
        JsonObject schema = new JsonObject();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("type", "object");
        schema.put("title", "VX Core Configuration Schema");
        schema.put("description", "Configuration schema for VX Core framework");
        
        JsonObject properties = new JsonObject();
        JsonObject definitions = new JsonObject();
        
        for (ConfigurationClassMetadata classMetadata : metadataList) {
            JsonObject classSchema = new JsonObject();
            classSchema.put("type", "object");
            classSchema.put("description", classMetadata.getDescription());
            
            JsonObject classProperties = new JsonObject();
            for (PropertyMetadata property : classMetadata.getProperties()) {
                JsonObject propertySchema = new JsonObject();
                propertySchema.put("description", property.getDescription());
                propertySchema.put("type", mapPropertyTypeToJsonType(property.getType()));
                
                if (!property.getDefaultValue().isEmpty()) {
                    propertySchema.put("default", parseDefaultValue(property.getDefaultValue(), property.getType()));
                }
                
                if (property.isRequired()) {
                    propertySchema.put("required", true);
                }
                
                if (property.getAllowedValues().length > 0) {
                    propertySchema.put("enum", Arrays.asList(property.getAllowedValues()));
                }
                
                if (property.getType() == ConfigurationProperty.PropertyType.INTEGER || 
                    property.getType() == ConfigurationProperty.PropertyType.LONG ||
                    property.getType() == ConfigurationProperty.PropertyType.DOUBLE) {
                    if (property.getMinValue() != Double.MIN_VALUE) {
                        propertySchema.put("minimum", property.getMinValue());
                    }
                    if (property.getMaxValue() != Double.MAX_VALUE) {
                        propertySchema.put("maximum", property.getMaxValue());
                    }
                }
                
                classProperties.put(property.getUsername(), propertySchema);
            }
            
            classSchema.put("properties", classProperties);
            definitions.put(classMetadata.getClassName(), classSchema);
            
            if (!classMetadata.getPrefix().isEmpty()) {
                properties.put(classMetadata.getPrefix(), new JsonObject().put("$ref", "#/definitions/" + classMetadata.getClassName()));
            }
        }
        
        schema.put("properties", properties);
        schema.put("definitions", definitions);
        
        return schema;
    }
    
    /**
     * 映射属性类型到JSON Schema类型
     */
    private static String mapPropertyTypeToJsonType(ConfigurationProperty.PropertyType type) {
        switch (type) {
            case STRING:
            case ENUM:
                return "string";
            case INTEGER:
            case LONG:
                return "integer";
            case DOUBLE:
                return "number";
            case BOOLEAN:
                return "boolean";
            case ARRAY:
                return "array";
            case OBJECT:
                return "object";
            default:
                return "string";
        }
    }
    
    /**
     * 解析默认值
     */
    private static Object parseDefaultValue(String defaultValue, ConfigurationProperty.PropertyType type) {
        if (defaultValue.isEmpty()) {
            return null;
        }
        
        try {
            switch (type) {
                case INTEGER:
                    return Integer.parseInt(defaultValue);
                case LONG:
                    return Long.parseLong(defaultValue);
                case DOUBLE:
                    return Double.parseDouble(defaultValue);
                case BOOLEAN:
                    return Boolean.parseBoolean(defaultValue);
                default:
                    return defaultValue;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
