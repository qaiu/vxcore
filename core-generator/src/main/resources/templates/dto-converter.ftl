<#-- DTO 转换器模板 -->
package ${package.dtoPackage};

import ${package.entityPackage}.${entity.className};
import java.util.List;
import java.util.stream.Collectors;

<#if entity.description?has_content>
/**
 * ${entity.description}DTO转换器
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}DtoConverter {

    /**
     * 实体转响应DTO
     * 
     * @param entity 实体对象
     * @return 响应DTO
     */
    public static ${entity.className}Response toResponse(${entity.className} entity) {
        if (entity == null) {
            return null;
        }
        
        ${entity.className}Response response = new ${entity.className}Response();
        <#list entity.fields as field>
        response.set${field.fieldName?cap_first}(entity.get${field.fieldName?cap_first}());
        </#list>
        
        return response;
    }

    /**
     * 实体列表转响应DTO列表
     * 
     * @param entities 实体列表
     * @return 响应DTO列表
     */
    public static List<${entity.className}Response> toResponseList(List<${entity.className}> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(${entity.className}DtoConverter::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建请求DTO转实体
     * 
     * @param createRequest 创建请求DTO
     * @return 实体对象
     */
    public static ${entity.className} toEntity(${entity.className}CreateRequest createRequest) {
        if (createRequest == null) {
            return null;
        }
        
        ${entity.className} entity = new ${entity.className}();
        <#list entity.fields as field>
        <#if field.fieldName != "id">
        entity.set${field.fieldName?cap_first}(createRequest.get${field.fieldName?cap_first}());
        </#if>
        </#list>
        
        return entity;
    }

    /**
     * 更新请求DTO转实体
     * 
     * @param updateRequest 更新请求DTO
     * @return 实体对象
     */
    public static ${entity.className} toEntity(${entity.className}UpdateRequest updateRequest) {
        if (updateRequest == null) {
            return null;
        }
        
        ${entity.className} entity = new ${entity.className}();
        <#list entity.fields as field>
        entity.set${field.fieldName?cap_first}(updateRequest.get${field.fieldName?cap_first}());
        </#list>
        
        return entity;
    }

    /**
     * 更新实体（从更新请求DTO）
     * 
     * @param entity 现有实体
     * @param updateRequest 更新请求DTO
     * @return 更新后的实体
     */
    public static ${entity.className} updateEntity(${entity.className} entity, ${entity.className}UpdateRequest updateRequest) {
        if (entity == null || updateRequest == null) {
            return entity;
        }
        
        <#list entity.fields as field>
        <#if field.fieldName != "id">
        if (updateRequest.get${field.fieldName?cap_first}() != null) {
            entity.set${field.fieldName?cap_first}(updateRequest.get${field.fieldName?cap_first}());
        }
        </#if>
        </#list>
        
        return entity;
    }
}
