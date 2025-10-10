<#-- 更新请求 DTO 模板 -->
package ${package.dtoPackage};

<#if entity.imports?has_content>
<#list entity.imports as import>
import ${import};
</#list>

</#if>
<#if config.useLombok>
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
</#if>
<#if config.generateValidation>
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
</#if>

<#if entity.description?has_content>
/**
 * ${entity.description}更新请求
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
<#if config.useLombok>
@Data
@NoArgsConstructor
@AllArgsConstructor
</#if>
public class ${entity.className} {
<#if !config.useLombok>

<#list entity.fields as field>
    <#if field.description?has_content>
    /**
     * ${field.description}
     */
    </#if>
    <#if config.generateValidation && !(field.nullable!true)>
    <#if field.fieldType == "String">
    @NotBlank(message = "${field.description!field.fieldName}不能为空")
    <#else>
    @NotNull(message = "${field.description!field.fieldName}不能为空")
    </#if>
    </#if>
    <#if config.generateValidation && field.fieldType == "String" && field.length??>
    @Size(max = ${field.length}, message = "${field.description!field.fieldName}长度不能超过${field.length}")
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
</#if>
}
