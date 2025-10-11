package cn.qaiu.vx.core.enums;

/**
 * MIME类型枚举
 * 定义HTTP请求和响应头中使用的MIME类型
 * <br>Create date 2021/8/30 4:35
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public enum MIMEType {

    /** 空类型 */
    NULL(""),
    /** 所有类型 */
    ALL("*/*"),
    /** HTML文本 */
    TEXT_HTML("text/html"),
    /** PostScript应用 */
    APPLICATION_POSTSCRIPT("application/postscript"),
    /** 纯文本 */
    TEXT_PLAIN("text/plain"),
    /** URL编码表单 */
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    /** 二进制流 */
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    /** 多部分表单数据 */
    MULTIPART_FORM_DATA("multipart/form-data"),
    /** Java代理 */
    APPLICATION_X_JAVA_AGENT("application/x-java-agent"),
    /** HTTP消息 */
    MESSAGE_HTTP("message/http"),
    /** CSS样式 */
    TEXT_CSS("text/css"),
    /** XML文本 */
    TEXT_XML("text/xml"),
    /** 文本类型 */
    TEXT("text/*"),
    /** RDF XML */
    APPLICATION_RDF_XML("application/rdf+xml"),
    /** XHTML XML */
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    /** XML应用 */
    APPLICATION_XML("application/xml"),
    /** JSON应用 */
    APPLICATION_JSON("application/json");

    /**
     * 获取MIME类型值
     * 
     * @return MIME类型字符串
     */
    public String getValue() {
        return type;
    }

    /** MIME类型值 */
    private final String type;

    /**
     * 构造函数
     * 
     * @param type MIME类型字符串
     */
    MIMEType(String type) {
        this.type = type;
    }

}
