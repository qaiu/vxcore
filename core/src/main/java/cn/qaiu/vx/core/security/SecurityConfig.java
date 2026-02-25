package cn.qaiu.vx.core.security;

import io.vertx.core.json.JsonObject;

/**
 * 安全认证配置 类型安全的配置类
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SecurityConfig {

  /** 默认构造函数 */
  public SecurityConfig() {}

  /**
   * 从JsonObject构建配置
   *
   * @param json 配置JSON
   */
  public SecurityConfig(JsonObject json) {
    if (json != null) {
      // JWT配置
      this.jwtEnabled = json.getBoolean("jwt-enable", json.getBoolean("jwtEnabled", false));
      this.jwtSecret = json.getString("jwt-secret", json.getString("jwtSecret"));
      this.jwtPublicKeyPath = json.getString("jwt-public-key", json.getString("jwtPublicKeyPath"));
      this.jwtPrivateKeyPath =
          json.getString("jwt-private-key", json.getString("jwtPrivateKeyPath"));
      this.jwtAlgorithm = json.getString("jwt-algorithm", json.getString("jwtAlgorithm", "RS256"));
      this.jwtExpireSeconds =
          json.getInteger("jwt-expire-seconds", json.getInteger("jwtExpireSeconds", 3600));
      this.refreshTokenExpireSeconds =
          json.getInteger(
              "refresh-token-expire-seconds",
              json.getInteger("refreshTokenExpireSeconds", 86400 * 7));
      this.jwtIssuer = json.getString("jwt-issuer", json.getString("jwtIssuer", "vxcore"));

      // 路径配置
      if (json.containsKey("jwt-auth-reg")) {
        this.authPaths =
            json.getJsonArray("jwt-auth-reg").stream().map(Object::toString).toArray(String[]::new);
      } else if (json.containsKey("authPaths")) {
        this.authPaths =
            json.getJsonArray("authPaths").stream().map(Object::toString).toArray(String[]::new);
      }
      if (json.containsKey("jwt-ignores-reg")) {
        this.ignorePaths =
            json.getJsonArray("jwt-ignores-reg").stream()
                .map(Object::toString)
                .toArray(String[]::new);
      } else if (json.containsKey("ignorePaths")) {
        this.ignorePaths =
            json.getJsonArray("ignorePaths").stream().map(Object::toString).toArray(String[]::new);
      }

      // Token配置
      this.tokenHeader =
          json.getString("token-header", json.getString("tokenHeader", "Authorization"));
      this.tokenPrefix = json.getString("token-prefix", json.getString("tokenPrefix", "Bearer "));

      // 防盗链配置
      this.refererEnabled = json.getBoolean("ref-enable", json.getBoolean("refererEnabled", false));
    }
  }

  /** 是否启用JWT认证 */
  private boolean jwtEnabled = false;

  /** JWT密钥（对称加密HS256使用） */
  private String jwtSecret;

  /** JWT公钥路径（非对称加密RS256使用） */
  private String jwtPublicKeyPath;

  /** JWT私钥路径（非对称加密RS256使用） */
  private String jwtPrivateKeyPath;

  /** JWT算法（HS256, RS256等） */
  private String jwtAlgorithm = "RS256";

  /** JWT过期时间（秒） */
  private int jwtExpireSeconds = 3600;

  /** 刷新Token过期时间（秒） */
  private int refreshTokenExpireSeconds = 86400 * 7;

  /** JWT签发者 */
  private String jwtIssuer = "vxcore";

  /** 需要认证的路径正则列表 */
  private String[] authPaths = {};

  /** 忽略认证的路径正则列表 */
  private String[] ignorePaths = {};

  /** Token请求头名称 */
  private String tokenHeader = "Authorization";

  /** Token前缀 */
  private String tokenPrefix = "Bearer ";

  /** 是否启用防盗链验证 */
  private boolean refererEnabled = false;

  /** 允许的域名列表 */
  private String[] allowedDomains = {};

  // Getters and Setters

  public boolean isJwtEnabled() {
    return jwtEnabled;
  }

  public void setJwtEnabled(boolean jwtEnabled) {
    this.jwtEnabled = jwtEnabled;
  }

  public String getJwtSecret() {
    return jwtSecret;
  }

  public void setJwtSecret(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public String getJwtPublicKeyPath() {
    return jwtPublicKeyPath;
  }

  public void setJwtPublicKeyPath(String jwtPublicKeyPath) {
    this.jwtPublicKeyPath = jwtPublicKeyPath;
  }

  public String getJwtPrivateKeyPath() {
    return jwtPrivateKeyPath;
  }

  public void setJwtPrivateKeyPath(String jwtPrivateKeyPath) {
    this.jwtPrivateKeyPath = jwtPrivateKeyPath;
  }

  public String getJwtAlgorithm() {
    return jwtAlgorithm;
  }

  public void setJwtAlgorithm(String jwtAlgorithm) {
    this.jwtAlgorithm = jwtAlgorithm;
  }

  public int getJwtExpireSeconds() {
    return jwtExpireSeconds;
  }

  public void setJwtExpireSeconds(int jwtExpireSeconds) {
    this.jwtExpireSeconds = jwtExpireSeconds;
  }

  public int getRefreshTokenExpireSeconds() {
    return refreshTokenExpireSeconds;
  }

  public void setRefreshTokenExpireSeconds(int refreshTokenExpireSeconds) {
    this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
  }

  public String getJwtIssuer() {
    return jwtIssuer;
  }

  public void setJwtIssuer(String jwtIssuer) {
    this.jwtIssuer = jwtIssuer;
  }

  public String[] getAuthPaths() {
    return authPaths != null ? authPaths.clone() : new String[0];
  }

  public void setAuthPaths(String[] authPaths) {
    this.authPaths = authPaths != null ? authPaths.clone() : null;
  }

  public String[] getIgnorePaths() {
    return ignorePaths != null ? ignorePaths.clone() : new String[0];
  }

  public void setIgnorePaths(String[] ignorePaths) {
    this.ignorePaths = ignorePaths != null ? ignorePaths.clone() : null;
  }

  public String getTokenHeader() {
    return tokenHeader;
  }

  public void setTokenHeader(String tokenHeader) {
    this.tokenHeader = tokenHeader;
  }

  public String getTokenPrefix() {
    return tokenPrefix;
  }

  public void setTokenPrefix(String tokenPrefix) {
    this.tokenPrefix = tokenPrefix;
  }

  public boolean isRefererEnabled() {
    return refererEnabled;
  }

  public void setRefererEnabled(boolean refererEnabled) {
    this.refererEnabled = refererEnabled;
  }

  public String[] getAllowedDomains() {
    return allowedDomains != null ? allowedDomains.clone() : new String[0];
  }

  public void setAllowedDomains(String[] allowedDomains) {
    this.allowedDomains = allowedDomains != null ? allowedDomains.clone() : null;
  }

  /**
   * 从JsonObject构建配置
   *
   * @param json 配置JSON
   * @return SecurityConfig实例
   */
  public static SecurityConfig fromJson(JsonObject json) {
    if (json == null) {
      return new SecurityConfig();
    }

    SecurityConfig config = new SecurityConfig();

    // JWT配置
    config.setJwtEnabled(json.getBoolean("jwt-enable", json.getBoolean("jwtEnabled", false)));
    config.setJwtSecret(json.getString("jwt-secret", json.getString("jwtSecret")));
    config.setJwtPublicKeyPath(
        json.getString("jwt-public-key", json.getString("jwtPublicKeyPath")));
    config.setJwtPrivateKeyPath(
        json.getString("jwt-private-key", json.getString("jwtPrivateKeyPath")));
    config.setJwtAlgorithm(
        json.getString("jwt-algorithm", json.getString("jwtAlgorithm", "RS256")));
    config.setJwtExpireSeconds(
        json.getInteger("jwt-expire-seconds", json.getInteger("jwtExpireSeconds", 3600)));
    config.setRefreshTokenExpireSeconds(
        json.getInteger(
            "refresh-token-expire-seconds",
            json.getInteger("refreshTokenExpireSeconds", 86400 * 7)));
    config.setJwtIssuer(json.getString("jwt-issuer", json.getString("jwtIssuer", "vxcore")));

    // 路径配置
    if (json.containsKey("jwt-auth-reg")) {
      config.setAuthPaths(
          json.getJsonArray("jwt-auth-reg").stream().map(Object::toString).toArray(String[]::new));
    }
    if (json.containsKey("jwt-ignores-reg")) {
      config.setIgnorePaths(
          json.getJsonArray("jwt-ignores-reg").stream()
              .map(Object::toString)
              .toArray(String[]::new));
    }

    // Token配置
    config.setTokenHeader(
        json.getString("token-header", json.getString("tokenHeader", "Authorization")));
    config.setTokenPrefix(json.getString("token-prefix", json.getString("tokenPrefix", "Bearer ")));

    // 防盗链配置
    config.setRefererEnabled(
        json.getBoolean("ref-enable", json.getBoolean("refererEnabled", false)));

    return config;
  }

  /**
   * 转换为JsonObject
   *
   * @return JsonObject
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("jwtEnabled", jwtEnabled);
    json.put("jwtAlgorithm", jwtAlgorithm);
    json.put("jwtExpireSeconds", jwtExpireSeconds);
    json.put("refreshTokenExpireSeconds", refreshTokenExpireSeconds);
    json.put("jwtIssuer", jwtIssuer);
    json.put("tokenHeader", tokenHeader);
    json.put("tokenPrefix", tokenPrefix);
    json.put("refererEnabled", refererEnabled);

    if (jwtSecret != null) json.put("jwtSecret", jwtSecret);
    if (jwtPublicKeyPath != null) json.put("jwtPublicKeyPath", jwtPublicKeyPath);
    if (jwtPrivateKeyPath != null) json.put("jwtPrivateKeyPath", jwtPrivateKeyPath);

    return json;
  }
}
