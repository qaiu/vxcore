<#-- 实体类模板 -->
package ${entity.packageName};

<#if entity.imports?has_content>
<#list entity.imports as import>
import ${import};
</#list>

</#if>
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.Objects;

<#if entity.description?has_content>
/**
 * ${entity.description}
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className} {
<#if entity.fields?has_content>

<#list entity.fields as field>
    <#if field.description?has_content>
    /**
     * ${field.description}
     */
    </#if>
    private ${field.fieldType} ${field.fieldName};
</#list>

<#if config.generateConstructors>
    /**
     * 默认构造函数
     */
    public ${entity.className}() {
    }

    <#if entity.fields?has_content>
    /**
     * 全参构造函数
     */
    public ${entity.className}(<#list entity.fields as field>${field.fieldType} ${field.fieldName}<#if field_has_next>, </#if></#list>) {
    <#list entity.fields as field>
        this.${field.fieldName} = ${field.fieldName};
    </#list>
    }
    </#if>
</#if>

<#if config.generateGetters>
<#list entity.fields as field>
    /**
     * 获取${field.description!field.fieldName}
     * 
     * @return ${field.description!field.fieldName}
     */
    public ${field.fieldType} ${field.getterName}() {
        return ${field.fieldName};
    }
</#list>
</#if>

<#if config.generateSetters>
<#list entity.fields as field>
    /**
     * 设置${field.description!field.fieldName}
     * 
     * @param ${field.fieldName} ${field.description!field.fieldName}
     */
    public void ${field.setterName}(${field.fieldType} ${field.fieldName}) {
        this.${field.fieldName} = ${field.fieldName};
    }
</#list>
</#if>

<#if config.generateEquals>
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ${entity.className} that = (${entity.className}) obj;
        return Objects.equals(<#list entity.fields as field>${field.fieldName}<#if field_has_next>, </#if></#list>, that.<#list entity.fields as field>${field.fieldName}<#if field_has_next>, </#if></#list>);
    }
</#if>

<#if config.generateHashCode>
    @Override
    public int hashCode() {
        return Objects.hash(<#list entity.fields as field>${field.fieldName}<#if field_has_next>, </#if></#list>);
    }
</#if>

<#if config.generateToString>
    @Override
    public String toString() {
        return "${entity.className}{" +
        <#list entity.fields as field>
                "${field.fieldName}=" + ${field.fieldName}<#if field_has_next> + ", " +</#if>
        </#list>
                '}';
    }
</#if>
}
</#if>
