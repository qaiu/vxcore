package cn.qaiu.vx.core.security;

import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT认证提供者 封装Vert.x JWT Auth，提供Token生成和验证功能
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class JwtAuthProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthProvider.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final Vertx vertx;
  private final SecurityConfig config;
  private JWTAuth jwtAuth;
  private boolean initialized = false;

  public JwtAuthProvider(Vertx vertx, SecurityConfig config) {
    this.vertx = vertx;
    this.config = config;
  }

  /**
   * 使用VertxHolder获取Vertx实例的构造函数
   *
   * @param config 安全配置
   */
  public JwtAuthProvider(SecurityConfig config) {
    this.vertx = VertxHolder.getVertxInstance();
    this.config = config;
  }

  /**
   * 初始化JWT认证提供者
   *
   * @return 初始化结果Future
   */
  public Future<Void> initialize() {
    if (initialized) {
      return Future.succeededFuture();
    }

    return Future.future(
        promise -> {
          try {
            JWTAuthOptions options = createJwtAuthOptions();
            this.jwtAuth = JWTAuth.create(vertx, options);
            this.initialized = true;
            LOGGER.info(
                "JWT Auth Provider initialized with algorithm: {}", config.getJwtAlgorithm());
            promise.complete();
          } catch (Exception e) {
            LOGGER.error("Failed to initialize JWT Auth Provider", e);
            promise.fail(e);
          }
        });
  }

  /** 创建JWT认证选项 */
  private JWTAuthOptions createJwtAuthOptions() throws Exception {
    JWTAuthOptions options = new JWTAuthOptions();
    String algorithm = config.getJwtAlgorithm();

    if (algorithm.startsWith("HS")) {
      // 对称加密（HMAC）
      String secret = config.getJwtSecret();
      if (secret == null || secret.isEmpty()) {
        // 生成随机密钥
        secret = generateRandomSecret();
        LOGGER.warn(
            "JWT secret not configured, using auto-generated secret (not recommended for production)");
      }

      options.addPubSecKey(new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(secret));

    } else if (algorithm.startsWith("RS") || algorithm.startsWith("ES")) {
      // 非对称加密（RSA/ECDSA）
      String publicKeyPath = config.getJwtPublicKeyPath();
      String privateKeyPath = config.getJwtPrivateKeyPath();

      if (publicKeyPath != null && privateKeyPath != null) {
        // 从文件加载密钥
        String publicKey = loadKeyFromFile(publicKeyPath);
        String privateKey = loadKeyFromFile(privateKeyPath);

        options.addPubSecKey(new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(publicKey));

        options.addPubSecKey(new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(privateKey));

      } else {
        // 生成临时密钥对
        LOGGER.warn(
            "JWT keys not configured, generating temporary key pair (not recommended for production)");
        KeyPair keyPair = generateRSAKeyPair();

        String publicKey =
            "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";
        String privateKey =
            "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";

        options.addPubSecKey(new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(publicKey));

        options.addPubSecKey(new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(privateKey));
      }
    }

    return options;
  }

  /** 从文件加载密钥 */
  private String loadKeyFromFile(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  /** 生成随机密钥 */
  private String generateRandomSecret() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getEncoder().encodeToString(bytes);
  }

  /** 生成RSA密钥对 */
  private KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }

  /**
   * 生成访问Token
   *
   * @param claims Token载荷
   * @return Token字符串
   */
  public String generateToken(JsonObject claims) {
    checkInitialized();

    JWTOptions options =
        new JWTOptions()
            .setExpiresInSeconds(config.getJwtExpireSeconds())
            .setIssuer(config.getJwtIssuer())
            .setAlgorithm(config.getJwtAlgorithm());

    return jwtAuth.generateToken(claims, options);
  }

  /**
   * 生成刷新Token
   *
   * @param claims Token载荷
   * @return Token字符串
   */
  public String generateRefreshToken(JsonObject claims) {
    checkInitialized();

    JWTOptions options =
        new JWTOptions()
            .setExpiresInSeconds(config.getRefreshTokenExpireSeconds())
            .setIssuer(config.getJwtIssuer())
            .setAlgorithm(config.getJwtAlgorithm());

    // 添加刷新Token标识
    JsonObject refreshClaims = claims.copy().put("type", "refresh");
    return jwtAuth.generateToken(refreshClaims, options);
  }

  /**
   * 验证Token
   *
   * @param token Token字符串
   * @return 验证结果Future
   */
  public Future<User> authenticate(String token) {
    checkInitialized();

    // 首先检查Token是否在黑名单中
    String tokenIdentifier = TokenBlacklist.generateTokenIdentifier(token);
    if (TokenBlacklist.getInstance().isBlacklisted(tokenIdentifier)) {
      LOGGER.debug("Token is blacklisted: {}", tokenIdentifier);
      return Future.failedFuture("Token has been revoked");
    }

    // 使用新的 TokenCredentials API 替代废弃的 JsonObject 认证方式
    Credentials credentials = new TokenCredentials(token);
    return jwtAuth.authenticate(credentials);
  }

  /**
   * 撤销Token（加入黑名单）
   *
   * @param token Token字符串
   * @param user 用户对象（用于获取过期时间）
   */
  public void revokeToken(String token, User user) {
    String tokenIdentifier = TokenBlacklist.generateTokenIdentifier(token);

    // 尝试从claims中获取过期时间
    JsonObject principal = user.principal();
    Long exp = principal.getLong("exp");

    if (exp != null) {
      // exp是Unix时间戳（秒），转换为毫秒
      TokenBlacklist.getInstance().addToBlacklist(tokenIdentifier, exp * 1000);
    } else {
      // 使用默认过期时间
      TokenBlacklist.getInstance().addToBlacklist(tokenIdentifier);
    }

    LOGGER.info("Token revoked for user: {}", principal.getString("sub"));
  }

  /**
   * 撤销Token（仅使用token字符串）
   *
   * @param token Token字符串
   */
  public void revokeToken(String token) {
    String tokenIdentifier = TokenBlacklist.generateTokenIdentifier(token);
    TokenBlacklist.getInstance().addToBlacklist(tokenIdentifier);
    LOGGER.info("Token revoked: {}", tokenIdentifier);
  }

  /**
   * 验证并获取安全上下文
   *
   * @param token Token字符串
   * @return SecurityContext Future
   */
  public Future<SecurityContext> authenticateAndGetContext(String token) {
    return authenticate(token).map(SecurityContext::new);
  }

  /**
   * 检查Token是否为刷新Token
   *
   * @param user 用户对象
   * @return 是否为刷新Token
   */
  public boolean isRefreshToken(User user) {
    return "refresh".equals(user.principal().getString("type"));
  }

  /**
   * 获取底层JWTAuth实例
   *
   * @return JWTAuth实例
   */
  public JWTAuth getJwtAuth() {
    checkInitialized();
    return jwtAuth;
  }

  /**
   * 获取配置
   *
   * @return SecurityConfig
   */
  public SecurityConfig getConfig() {
    return config;
  }

  /** 检查是否已初始化 */
  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("JwtAuthProvider not initialized. Call initialize() first.");
    }
  }
}
