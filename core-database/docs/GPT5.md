明白 ✅
你要的是一个 **通用的提示词（prompt）**，能让任意代码生成 AI agent 理解需求，然后帮你产出：

* jOOQ 只做 **SQL 构建**
* Vert.x **异步执行 SQL**
* 自动 DSL / DAO 模板（可 CRUD，可继承扩展自定义 SQL）
* 结果映射为 **实体类**（配合 Vert.x CodeGen / `JsonObject` / `vertx-jdbc-template` 风格）
* 返回值类型必须是 `Future<Entity>` 或 `Future<List<Entity>>`

而不是指向某个具体 AI 工具的名字。

---

## 通用 AI code agent 提示词

下面是整理好的提示词（中文），你可以直接用它来驱动任何 AI 代码生成工具，生成符合你要求的框架代码：

---

### 🚀 提示词（Prompt）

我需要你帮我生成一套 Java/Vert.x + jOOQ 的数据库访问框架代码，具体要求如下：

1. **SQL 构建**

   * 只使用 jOOQ 的 DSL 来生成 SQL（`Query.getSQL(ParamType.INDEXED)` + `Query.getBindValues()`）。
   * **不要**使用 jOOQ 自带的执行方法。

2. **SQL 执行**

   * 执行交给 Vert.x 的 `PgPool` 或 `Pool`（最新版本，不要使用过时的 `JDBCPool`）。
   * 使用 `preparedQuery(sql).execute(Tuple.from(params))` 异步执行。
   * 返回值类型必须是 `io.vertx.core.Future`。

3. **自动 DSL 部分**

   * 框架需要根据实体类（或 jOOQ codegen 生成的表对象）自动生成对应的 `Query` 类。
   * 每个实体类对应的 DAO 对象要拥有 **增删改查 (CRUD)** 能力。
   * 用户可以继承这些 DAO 类，编写自定义 SQL 查询（依然基于 jOOQ）。

4. **返回值类型**

   * 查询结果不能直接返回 `RowSet<Row>`，需要映射成实体类。
   * 提供 `Future<Entity>` 和 `Future<List<Entity>>` 两种常见形式。
   * 映射实体类时，要结合 Vert.x CodeGen 的风格：

     * 实体类带 `@DataObject` 注解
     * 提供 `JsonObject` 构造函数和 `toJson()` 方法
     * 可以使用 `row.toJson()` 或 `JsonObject` 转换后 `mapTo(Entity.class)`

5. **设计要求**

   * 提供一个通用的 `JooqVertxExecutor`（负责把 jOOQ Query 转成 SQL + 参数，并用 Vert.x Pool 执行）。
   * 提供一个 `EntityMapper<T>` 接口（定义 `T from(Row row)` / `JsonObject toJson(T)`）。
   * 提供一个 `BaseDao<T>` 抽象类（封装 `insert/update/delete/findById/findAll` 等方法，返回 `Future<T>` 或 `Future<List<T>>`）。
   * 基于示例实体（如 `User`）生成对应的 `UserDao`，并展示如何继承扩展写自定义查询。

6. **示例代码**

   * 给出一个 `User` 实体类（`id: Long, name: String, email: String`），符合 Vert.x CodeGen 风格。
   * 给出 `UserDao` 示例，展示 `findById(long id)` 和 `findAll()` 的实现。
   * 展示如何用 jOOQ 构建 SQL，然后交给框架执行，并返回 `Future<List<User>>`。

7. **API 风格**

   * 必须使用 **Vert.x 最新版本（4.5+）** 的 API，基于 JDK 17。
   * 不要使用过时的 `Future` API。
   * 保证代码是非阻塞式的，完全符合 Vert.x 的风格。

---

### ✅ 期望输出

* `JooqVertxExecutor.java`
* `EntityMapper<T>.java`
* `BaseDao<T>.java`
* `User.java`（带 @DataObject 注解 + JsonObject 构造函数 + toJson）
* `UserDao.java`（接口 + 实现）
* `ExampleVerticle.java` 或 `Main`（展示如何执行 `findById`、`findAll`）

---

### 📝 额外说明

* 请在代码里写上必要的注释，解释关键步骤（如 jOOQ 如何转 SQL，Vert.x 如何异步执行，实体映射是如何完成的）。
* 保证生成的代码可以直接 copy 到 Vert.x + jOOQ 项目中运行，或仅需少量改造即可。

---

这样写，AI 就会按你的 3 个需求点一次性生成出一整套 **Vert.x + jOOQ + 实体映射 DAO 模板代码**。

---

要不要我帮你直接用这个提示词生成一整套 **完整的 Java 代码骨架**（包含 Executor、BaseDao、UserDao、User 实体、示例调用）？
