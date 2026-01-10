package cn.qaiu.vx.core.security;

import cn.qaiu.vx.core.lifecycle.LifecycleComponent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 安全组件
 * 框架生命周期组件，负责安全模块的初始化
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SecurityComponent implements LifecycleComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityComponent.class);

    private SecurityConfig securityConfig;
    private JwtAuthProvider jwtAuthProvider;
    private SecurityInterceptor securityInterceptor;
    private boolean enabled = false;

    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {

        return Future.future(promise -> {
            try {
                LOGGER.info("Initializing Security component...");

                // 解析安全配置
                JsonObject authConfig = config.getJsonObject("server-auth");
                if (authConfig == null) {
                    authConfig = config.getJsonObject("security");
                }
                
                this.securityConfig = SecurityConfig.fromJson(authConfig);

                // 检查是否启用JWT认证
                if (!securityConfig.isJwtEnabled()) {
                    LOGGER.info("JWT authentication is disabled");
                    promise.complete();
                    return;
                }

                this.enabled = true;

                // 创建JWT认证提供者
                this.jwtAuthProvider = new JwtAuthProvider(vertx, securityConfig);

                // 初始化JWT提供者
                jwtAuthProvider.initialize()
                        .onSuccess(v -> {
                            // 创建安全拦截器
                            this.securityInterceptor = new SecurityInterceptor(jwtAuthProvider, securityConfig);
                            LOGGER.info("Security component initialized successfully");
                            promise.complete();
                        })
                        .onFailure(err -> {
                            LOGGER.error("Failed to initialize JWT Auth Provider", err);
                            promise.fail(err);
                        });

            } catch (Exception e) {
                LOGGER.error("Failed to initialize Security component", e);
                promise.fail(e);
            }
        });
    }

    @Override
    public Future<Void> start() {
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> stop() {
        return Future.succeededFuture();
    }

    @Override
    public String getName() {
        return "SecurityComponent";
    }

    @Override
    public int getPriority() {
        return 15; // 在ConfigurationComponent之后，DataSourceComponent之前
    }

    /**
     * 获取安全配置
     *
     * @return SecurityConfig
     */
    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    /**
     * 获取JWT认证提供者
     *
     * @return JwtAuthProvider
     */
    public JwtAuthProvider getJwtAuthProvider() {
        return jwtAuthProvider;
    }

    /**
     * 获取安全拦截器
     *
     * @return SecurityInterceptor
     */
    public SecurityInterceptor getSecurityInterceptor() {
        return securityInterceptor;
    }

    /**
     * 检查是否已启用
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
}
