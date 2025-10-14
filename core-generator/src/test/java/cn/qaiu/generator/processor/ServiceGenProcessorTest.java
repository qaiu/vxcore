package cn.qaiu.generator.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.DiagnosticCollector;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * 服务生成处理器测试类
 * 
 * 功能：
 * 1. 测试重构后的注解处理器
 * 2. 验证泛型参数解析
 * 3. 验证服务接口生成
 * 4. 验证服务实现类生成
 * 5. 验证 ProxyGen 注解处理
 * 
 * @author vxcore
 * @version 1.0
 */
public class ServiceGenProcessorTest {

    private static JavaCompiler compiler;
    private static File tempDir;
    private static File generatedDir;

    @BeforeAll
    public static void setup() {
        compiler = ToolProvider.getSystemJavaCompiler();
        tempDir = new File("target/processor-test");
        generatedDir = new File(tempDir, "generated");
        tempDir.mkdirs();
        generatedDir.mkdirs();
    }

    @AfterAll
    public static void teardown() {
        if (tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }

    @Test
    public void testProcessorGeneratesServiceClasses() throws IOException {
        // 测试实体源码
        String testEntitySource =
            "package cn.qaiu.generator.processor;\n" +
            "\n" +
            "import cn.qaiu.vx.core.annotations.GenerateServiceGen;\n" +
            "import io.vertx.core.json.JsonObject;\n" +
            "\n" +
            "@GenerateServiceGen(idType = Long.class, generateProxy = true, basePackage = \"cn.qaiu.generator.processor\")\n" +
            "public class TestEntity implements GenericInterface<User, Long> {\n" +
            "    private Long id;\n" +
            "    private String name;\n" +
            "    private String status;\n" +
            "    private String email;\n" +
            "    private User user;\n" +
            "    private Long timestamp;\n" +
            "\n" +
            "    public TestEntity() {}\n" +
            "\n" +
            "    public TestEntity(Long id, String name, String status, String email) {\n" +
            "        this.id = id;\n" +
            "        this.name = name;\n" +
            "        this.status = status;\n" +
            "        this.email = email;\n" +
            "        this.timestamp = System.currentTimeMillis();\n" +
            "    }\n" +
            "\n" +
            "    public Long getId() { return id; }\n" +
            "    public void setId(Long id) { this.id = id; }\n" +
            "    public String getName() { return name; }\n" +
            "    public void setName(String name) { this.name = name; }\n" +
            "    public String getStatus() { return status; }\n" +
            "    public void setStatus(String status) { this.status = status; }\n" +
            "    public String getEmail() { return email; }\n" +
            "    public void setEmail(String email) { this.email = email; }\n" +
            "    public User getUser() { return user; }\n" +
            "    public void setUser(User user) { this.user = user; }\n" +
            "    public Long getTimestamp() { return timestamp; }\n" +
            "    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }\n" +
            "\n" +
            "    public JsonObject toJson() {\n" +
            "        JsonObject json = new JsonObject();\n" +
            "        if (id != null) json.put(\"id\", id);\n" +
            "        if (name != null) json.put(\"name\", name);\n" +
            "        if (status != null) json.put(\"status\", status);\n" +
            "        if (email != null) json.put(\"email\", email);\n" +
            "        if (user != null) json.put(\"user\", user.toJson());\n" +
            "        if (timestamp != null) json.put(\"timestamp\", timestamp);\n" +
            "        return json;\n" +
            "    }\n" +
            "\n" +
            "    public static TestEntity fromJson(JsonObject json) {\n" +
            "        TestEntity entity = new TestEntity();\n" +
            "        if (json.containsKey(\"id\")) entity.setId(json.getLong(\"id\"));\n" +
            "        if (json.containsKey(\"name\")) entity.setName(json.getString(\"name\"));\n" +
            "        if (json.containsKey(\"status\")) entity.setStatus(json.getString(\"status\"));\n" +
            "        if (json.containsKey(\"email\")) entity.setEmail(json.getString(\"email\"));\n" +
            "        if (json.containsKey(\"timestamp\")) entity.setTimestamp(json.getLong(\"timestamp\"));\n" +
            "        return entity;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String toString() {\n" +
            "        return \"TestEntity{\" +\n" +
            "                \"id=\" + id +\n" +
            "                \", name='\" + name + '\\'' +\n" +
            "                \", status='\" + status + '\\'' +\n" +
            "                \", email='\" + email + '\\'' +\n" +
            "                \", timestamp=\" + timestamp +\n" +
            "                '}';\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "interface GenericInterface<T, U> {\n" +
            "    T getFirst();\n" +
            "    U getSecond();\n" +
            "    void setFirst(T first);\n" +
            "    void setSecond(U second);\n" +
            "}\n" +
            "\n" +
            "class User {\n" +
            "    private Long id;\n" +
            "    private String username;\n" +
            "    private String email;\n" +
            "\n" +
            "    public User() {}\n" +
            "\n" +
            "    public User(Long id, String username, String email) {\n" +
            "        this.id = id;\n" +
            "        this.username = username;\n" +
            "        this.email = email;\n" +
            "    }\n" +
            "\n" +
            "    public Long getId() { return id; }\n" +
            "    public void setId(Long id) { this.id = id; }\n" +
            "    public String getUsername() { return username; }\n" +
            "    public void setUsername(String username) { this.username = username; }\n" +
            "    public String getEmail() { return email; }\n" +
            "    public void setEmail(String email) { this.email = email; }\n" +
            "\n" +
            "    public JsonObject toJson() {\n" +
            "        JsonObject json = new JsonObject();\n" +
            "        if (id != null) json.put(\"id\", id);\n" +
            "        if (username != null) json.put(\"username\", username);\n" +
            "        if (email != null) json.put(\"email\", email);\n" +
            "        return json;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String toString() {\n" +
            "        return \"User{\" +\n" +
            "                \"id=\" + id +\n" +
            "                \", username='\" + username + '\\'' +\n" +
            "                \", email='\" + email + '\\'' +\n" +
            "                '}';\n" +
            "    }\n" +
            "}\n";

        JavaFileObject testEntityFile = new TestJavaFileObject("cn.qaiu.generator.processor.TestEntity", testEntitySource);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> options = new ArrayList<>();
        options.add("-processorpath");
        // 指向编译后的 core 模块，其中包含 CustomServiceGenProcessor
        options.add(System.getProperty("user.dir") + "/../core/target/classes");
        options.add("-d");
        options.add(generatedDir.getAbsolutePath());
        options.add("-s");
        options.add(generatedDir.getAbsolutePath());
        options.add("-verbose");
        options.add("-Xlint:processing");

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(testEntityFile);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        // 显式设置注解处理器
        task.setProcessors(Arrays.asList(new cn.qaiu.vx.core.processor.CustomServiceGenProcessor()));

        Boolean success = task.call();

        // 打印诊断信息
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.out.println(diagnostic);
        }

        assertTrue(success, "编译应该成功");

        // 验证生成的文件
        File generatedService = new File(generatedDir, "cn/qaiu/generator/processor/TestEntityService.java");
        File generatedServiceImpl = new File(generatedDir, "cn/qaiu/generator/processor/TestEntityServiceGen.java");

        assertTrue(generatedService.exists(), "生成的 TestEntityService.java 应该存在");
        assertTrue(generatedServiceImpl.exists(), "生成的 TestEntityServiceGen.java 应该存在");

        // 验证内容质量
        String serviceContent = Files.readString(generatedService.toPath());
        assertTrue(serviceContent.contains("@Generated"), "生成的服务文件应该有 @Generated 注解");
        assertFalse(serviceContent.contains("&lt;"), "生成的服务文件不应该包含 HTML 实体");
        assertFalse(serviceContent.contains("&gt;"), "生成的服务文件不应该包含 HTML 实体");
        assertTrue(serviceContent.contains("Future<"), "生成的服务文件应该包含正确的泛型语法");
        assertTrue(serviceContent.contains("@ProxyGen"), "生成的服务文件应该包含 @ProxyGen 注解");
        assertTrue(serviceContent.contains("@VertxGen"), "生成的服务文件应该包含 @VertxGen 注解");

        String implContent = Files.readString(generatedServiceImpl.toPath());
        assertTrue(implContent.contains("@Generated"), "生成的实现文件应该有 @Generated 注解");
        assertFalse(implContent.contains("&lt;"), "生成的实现文件不应该包含 HTML 实体");
        assertFalse(implContent.contains("&gt;"), "生成的实现文件不应该包含 HTML 实体");
        assertTrue(implContent.contains("Future<"), "生成的实现文件应该包含正确的泛型语法");

        System.out.println("✓ 所有生成的文件都有正确的语法和注解");
    }

    @Test
    public void testGenericTypeAnalysis() throws IOException {
        // 测试泛型类型分析
        String testEntitySource =
            "package cn.qaiu.generator.processor;\n" +
            "\n" +
            "import cn.qaiu.vx.core.annotations.GenerateServiceGen;\n" +
            "import io.vertx.core.json.JsonObject;\n" +
            "\n" +
            "@GenerateServiceGen(idType = Long.class, generateProxy = true)\n" +
            "public class GenericTestEntity implements GenericInterface<User, Long> {\n" +
            "    private Long id;\n" +
            "    private String name;\n" +
            "\n" +
            "    public Long getId() { return id; }\n" +
            "    public void setId(Long id) { this.id = id; }\n" +
            "    public String getName() { return name; }\n" +
            "    public void setName(String name) { this.name = name; }\n" +
            "}\n" +
            "\n" +
            "interface GenericInterface<T, U> {\n" +
            "    T getFirst();\n" +
            "    U getSecond();\n" +
            "}\n" +
            "\n" +
            "class User {\n" +
            "    private Long id;\n" +
            "    private String username;\n" +
            "\n" +
            "    public Long getId() { return id; }\n" +
            "    public void setId(Long id) { this.id = id; }\n" +
            "    public String getUsername() { return username; }\n" +
            "    public void setUsername(String username) { this.username = username; }\n" +
            "}\n";

        JavaFileObject testEntityFile = new TestJavaFileObject("cn.qaiu.generator.processor.GenericTestEntity", testEntitySource);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> options = new ArrayList<>();
        options.add("-processorpath");
        options.add(System.getProperty("user.dir") + "/../core/target/classes");
        options.add("-d");
        options.add(generatedDir.getAbsolutePath());
        options.add("-s");
        options.add(generatedDir.getAbsolutePath());

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(testEntityFile);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        task.setProcessors(Arrays.asList(new cn.qaiu.vx.core.processor.CustomServiceGenProcessor()));

        Boolean success = task.call();

        assertTrue(success, "泛型测试编译应该成功");

        // 验证生成的文件
        File generatedService = new File(generatedDir, "cn/qaiu/generator/processor/GenericTestEntityService.java");
        File generatedServiceImpl = new File(generatedDir, "cn/qaiu/generator/processor/GenericTestEntityServiceGen.java");

        assertTrue(generatedService.exists(), "生成的 GenericTestEntityService.java 应该存在");
        assertTrue(generatedServiceImpl.exists(), "生成的 GenericTestEntityServiceGen.java 应该存在");

        // 验证泛型参数
        String serviceContent = Files.readString(generatedService.toPath());
        assertTrue(serviceContent.contains("<T0, T1>"), "生成的服务文件应该包含泛型参数");
        assertTrue(serviceContent.contains("findByUser"), "生成的服务文件应该包含基于泛型类型的查询方法");
        assertTrue(serviceContent.contains("findByLong"), "生成的服务文件应该包含基于泛型类型的查询方法");

        System.out.println("✓ 泛型类型分析测试通过");
    }

    @Test
    public void testProxyGenIntegration() throws IOException {
        // 测试 ProxyGen 集成
        String testEntitySource =
            "package cn.qaiu.generator.processor;\n" +
            "\n" +
            "import cn.qaiu.vx.core.annotations.GenerateServiceGen;\n" +
            "import io.vertx.core.json.JsonObject;\n" +
            "\n" +
            "@GenerateServiceGen(idType = Long.class, generateProxy = true)\n" +
            "public class ProxyTestEntity {\n" +
            "    private Long id;\n" +
            "    private String name;\n" +
            "\n" +
            "    public Long getId() { return id; }\n" +
            "    public void setId(Long id) { this.id = id; }\n" +
            "    public String getName() { return name; }\n" +
            "    public void setName(String name) { this.name = name; }\n" +
            "}\n";

        JavaFileObject testEntityFile = new TestJavaFileObject("cn.qaiu.generator.processor.ProxyTestEntity", testEntitySource);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> options = new ArrayList<>();
        options.add("-processorpath");
        options.add(System.getProperty("user.dir") + "/../core/target/classes");
        options.add("-d");
        options.add(generatedDir.getAbsolutePath());
        options.add("-s");
        options.add(generatedDir.getAbsolutePath());

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(testEntityFile);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        task.setProcessors(Arrays.asList(new cn.qaiu.vx.core.processor.CustomServiceGenProcessor()));

        Boolean success = task.call();

        assertTrue(success, "ProxyGen 集成测试编译应该成功");

        // 验证生成的文件
        File generatedService = new File(generatedDir, "cn/qaiu/generator/processor/ProxyTestEntityService.java");
        File generatedServiceImpl = new File(generatedDir, "cn/qaiu/generator/processor/ProxyTestEntityServiceGen.java");

        assertTrue(generatedService.exists(), "生成的 ProxyTestEntityService.java 应该存在");
        assertTrue(generatedServiceImpl.exists(), "生成的 ProxyTestEntityServiceGen.java 应该存在");

        // 验证 ProxyGen 注解
        String serviceContent = Files.readString(generatedService.toPath());
        assertTrue(serviceContent.contains("@ProxyGen"), "生成的服务文件应该包含 @ProxyGen 注解");
        assertTrue(serviceContent.contains("@VertxGen"), "生成的服务文件应该包含 @VertxGen 注解");
        assertTrue(serviceContent.contains("@Fluent"), "生成的服务文件应该包含 @Fluent 注解");

        System.out.println("✓ ProxyGen 集成测试通过");
    }

    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return dir.delete();
    }

    private static class TestJavaFileObject extends SimpleJavaFileObject {
        private final String content;

        protected TestJavaFileObject(String className, String content) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}