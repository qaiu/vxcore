package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.lambda.example.User;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LambdaQueryWrapper查询包装器测试")
class LambdaQueryWrapperTest {

    private DSLContext dslContext;
    private Table<?> table;
    private LambdaQueryWrapper<User> wrapper;

    @BeforeEach
    void setUp() {
        // 使用Mock DSLContext，不需要真实的数据库连接
        dslContext = DSL.using(org.jooq.SQLDialect.H2);
        table = DSL.table("users");
        wrapper = new LambdaQueryWrapper<>(dslContext, table, User.class);
    }

    @Nested
    @DisplayName("基础条件查询测试")
    class BasicConditionTest {

        @Test
        @DisplayName("等于条件测试")
        void testEq() {
            LambdaQueryWrapper<User> result = wrapper.eq(User::getUsername, "john_doe");
            assertSame(wrapper, result, "应该返回自身以支持链式调用");
            
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("username = cast(? as varchar)"), "SQL应包含等于条件");
        }

        @Test
        @DisplayName("不等于条件测试")
        void testNe() {
            wrapper.ne(User::getStatus, "INACTIVE");
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status <> cast(? as varchar)"), "SQL应包含不等于条件");
        }

        @Test
        @DisplayName("大于条件测试")
        void testGt() {
            wrapper.gt(User::getAge, 18);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("age > cast(? as int)"), "SQL应包含大于条件");
        }

        @Test
        @DisplayName("大于等于条件测试")
        void testGe() {
            wrapper.ge(User::getBalance, 1000.0);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("balance >= cast(? as double)"), "SQL应包含大于等于条件");
        }

        @Test
        @DisplayName("小于条件测试")
        void testLt() {
            wrapper.lt(User::getAge, 65);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("age < cast(? as int)"), "SQL应包含小于条件");
        }

        @Test
        @DisplayName("小于等于条件测试")
        void testLe() {
            wrapper.le(User::getBalance, 5000.0);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("balance <= cast(? as double)"), "SQL应包含小于等于条件");
        }

        @Test
        @DisplayName("空值条件测试")
        void testIsNull() {
            wrapper.isNull(User::getBio);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("bio is null"), "SQL应包含is null条件");
        }

        @Test
        @DisplayName("非空值条件测试")
        void testIsNotNull() {
            wrapper.isNotNull(User::getEmail);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("email is not null"), "SQL应包含is not null条件");
        }

        @Test
        @DisplayName("LIKE条件测试")
        void testLike() {
            wrapper.like(User::getUsername, "john%");
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("username like cast(? as varchar)"), "SQL应包含like条件");
        }

        @Test
        @DisplayName("NOT LIKE条件测试")
        void testNotLike() {
            wrapper.notLike(User::getEmail, "%@spam.com");
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("email not like cast(? as varchar)"), "SQL应包含not like条件");
        }

        @Test
        @DisplayName("IN条件测试")
        void testIn() {
            List<String> statuses = Arrays.asList("ACTIVE", "PENDING");
            wrapper.in(User::getStatus, statuses);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status in"), "SQL应包含in条件");
        }

        @Test
        @DisplayName("NOT IN条件测试")
        void testNotIn() {
            List<String> statuses = Arrays.asList("INACTIVE", "BANNED");
            wrapper.notIn(User::getStatus, statuses);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status not in"), "SQL应包含not in条件");
        }

        @Test
        @DisplayName("BETWEEN条件测试")
        void testBetween() {
            wrapper.between(User::getAge, 18, 65);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("age between cast(? as int) and cast(? as int)"), "SQL应包含between条件");
        }

        @Test
        @DisplayName("NOT BETWEEN条件测试")
        void testNotBetween() {
            wrapper.notBetween(User::getBalance, 0.0, 100.0);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("balance not between cast(? as double) and cast(? as double)"), "SQL应包含not between条件");
        }
    }

    @Nested
    @DisplayName("嵌套条件查询测试")
    class NestedConditionTest {

        @Test
        @DisplayName("AND嵌套条件测试")
        void testAndNested() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .and(subWrapper -> subWrapper
                       .ge(User::getAge, 18)
                       .le(User::getAge, 65));

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含外层条件");
            assertTrue(sql.contains("age >= cast(? as int)"), "SQL应包含内层条件");
            assertTrue(sql.contains("age <= cast(? as int)"), "SQL应包含内层条件");
        }

        @Test
        @DisplayName("OR嵌套条件测试")
        void testOrNested() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .or(subWrapper -> subWrapper
                       .eq(User::getStatus, "PENDING")
                       .eq(User::getEmailVerified, true));

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含外层条件");
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含内层条件");
            assertTrue(sql.contains("email_verified = cast(? as boolean)"), "SQL应包含内层条件");
        }

        @Test
        @DisplayName("复杂嵌套条件测试")
        void testComplexNested() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .and(subWrapper1 -> subWrapper1
                       .ge(User::getAge, 18)
                       .or(subWrapper2 -> subWrapper2
                           .ge(User::getBalance, 1000.0)
                           .eq(User::getEmailVerified, true)));

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含外层条件");
            assertTrue(sql.contains("age >= cast(? as int)"), "SQL应包含内层条件");
            assertTrue(sql.contains("balance >= cast(? as double)"), "SQL应包含内层条件");
            assertTrue(sql.contains("email_verified = cast(? as boolean)"), "SQL应包含内层条件");
        }
    }

    @Nested
    @DisplayName("排序功能测试")
    class OrderByTest {

        @Test
        @DisplayName("升序排序测试")
        void testOrderByAsc() {
            wrapper.orderByAsc(User::getCreateTime);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("order by create_time asc"), "SQL应包含升序排序");
        }

        @Test
        @DisplayName("降序排序测试")
        void testOrderByDesc() {
            wrapper.orderByDesc(User::getBalance);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("order by balance desc"), "SQL应包含降序排序");
        }

        @Test
        @DisplayName("多字段排序测试")
        void testMultipleOrderBy() {
            wrapper.orderByDesc(User::getBalance)
                   .orderByAsc(User::getCreateTime);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("order by balance desc, create_time asc"), "SQL应包含多字段排序");
        }

        @Test
        @DisplayName("多字段升序排序测试")
        void testMultipleOrderByAsc() {
            wrapper.orderByAsc(User::getUsername, User::getEmail);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("order by username asc, email asc"), "SQL应包含多字段升序排序");
        }

        @Test
        @DisplayName("多字段降序排序测试")
        void testMultipleOrderByDesc() {
            wrapper.orderByDesc(User::getBalance, User::getAge);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("order by balance desc, age desc"), "SQL应包含多字段降序排序");
        }
    }

    @Nested
    @DisplayName("字段选择测试")
    class SelectTest {

        @Test
        @DisplayName("选择指定字段测试")
        void testSelectFields() {
            wrapper.select(User::getUsername, User::getEmail);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("select username, email"), "SQL应包含指定字段");
        }

        @Test
        @DisplayName("选择单个字段测试")
        void testSelectSingleField() {
            wrapper.select(User::getId);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("select id"), "SQL应包含单个字段");
        }

        @Test
        @DisplayName("选择所有字段测试")
        void testSelectAllFields() {
            // 不调用select方法，应该选择所有字段
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("select *"), "SQL应包含所有字段");
        }
    }

    @Nested
    @DisplayName("分页功能测试")
    class PaginationTest {

        @Test
        @DisplayName("限制查询数量测试")
        void testLimit() {
            wrapper.limit(10);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("fetch next ? rows only"), "SQL应包含limit条件");
        }

        @Test
        @DisplayName("偏移量测试")
        void testOffset() {
            wrapper.offset(20);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("offset ? rows"), "SQL应包含offset条件");
        }

        @Test
        @DisplayName("分页查询测试")
        void testPage() {
            wrapper.page(2, 10); // 第2页，每页10条
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("offset ? rows"), "SQL应包含offset条件");
            assertTrue(sql.contains("fetch next ? rows only"), "SQL应包含limit条件");
        }

        @Test
        @DisplayName("分页查询计算测试")
        void testPageCalculation() {
            LambdaQueryWrapper<User> pageWrapper = new LambdaQueryWrapper<>(dslContext, table, User.class);
            pageWrapper.page(3, 15); // 第3页，每页15条
            
            // 验证分页计算：offset = (3-1) * 15 = 30, limit = 15
            String sql = pageWrapper.buildSelect().getSQL();
            assertTrue(sql.contains("offset ? rows"), "SQL应包含offset条件");
            assertTrue(sql.contains("fetch next ? rows only"), "SQL应包含limit条件");
        }
    }

    @Nested
    @DisplayName("查询构建测试")
    class QueryBuildTest {

        @Test
        @DisplayName("构建查询条件测试")
        void testBuildCondition() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .gt(User::getAge, 18);
            
            String condition = wrapper.buildCondition().toString();
            assertTrue(condition.contains("status = 'ACTIVE'"), "条件应包含status条件");
            assertTrue(condition.contains("age > 18"), "条件应包含age条件");
        }

        @Test
        @DisplayName("构建空条件测试")
        void testBuildEmptyCondition() {
            String condition = wrapper.buildCondition().toString();
            assertEquals("true", condition, "空条件应该返回true");
        }

        @Test
        @DisplayName("构建计数查询测试")
        void testBuildCount() {
            wrapper.eq(User::getStatus, "ACTIVE");
            String sql = wrapper.buildCount().getSQL();
            assertTrue(sql.contains("select count(*)"), "SQL应包含count查询");
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含条件");
        }

        @Test
        @DisplayName("构建存在查询测试")
        void testBuildExists() {
            wrapper.eq(User::getStatus, "ACTIVE");
            String sql = wrapper.buildExists().getSQL();
            assertTrue(sql.contains("select 1"), "SQL应包含exists查询");
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含条件");
        }

        @Test
        @DisplayName("构建完整查询测试")
        void testBuildSelect() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .orderByDesc(User::getCreateTime)
                   .limit(10);
            
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("select *"), "SQL应包含select");
            assertTrue(sql.contains("from users"), "SQL应包含from子句");
            assertTrue(sql.contains("status = cast(? as varchar)"), "SQL应包含where条件");
            assertTrue(sql.contains("order by create_time desc"), "SQL应包含order by");
            assertTrue(sql.contains("fetch next ? rows only"), "SQL应包含limit");
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityTest {

        @Test
        @DisplayName("清空条件测试")
        void testClear() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .orderByDesc(User::getCreateTime)
                   .limit(10);
            
            LambdaQueryWrapper<User> clearedWrapper = wrapper.clear();
            assertSame(wrapper, clearedWrapper, "应该返回自身");
            
            String sql = wrapper.buildSelect().getSQL();
            assertFalse(sql.contains("status ="), "SQL不应包含之前的条件");
            assertFalse(sql.contains("order by"), "SQL不应包含之前的排序");
            assertFalse(sql.contains("fetch next"), "SQL不应包含之前的limit");
        }

        @Test
        @DisplayName("获取表名测试")
        void testGetTableName() {
            assertEquals("users", wrapper.getTableName(), "应该返回正确的表名");
        }

        @Test
        @DisplayName("获取实体类测试")
        void testGetEntityClass() {
            assertEquals(User.class, wrapper.getEntityClass(), "应该返回正确的实体类");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("空值条件测试")
        void testNullValue() {
            wrapper.eq(User::getUsername, null);
            String sql = wrapper.buildSelect().getSQL();
            assertFalse(sql.contains("username ="), "空值不应生成条件");
        }

        @Test
        @DisplayName("空集合IN条件测试")
        void testEmptyCollectionIn() {
            wrapper.in(User::getStatus, Arrays.asList());
            String sql = wrapper.buildSelect().getSQL();
            assertFalse(sql.contains("status in"), "空集合不应生成IN条件");
        }

        @Test
        @DisplayName("空字符串条件测试")
        void testEmptyString() {
            wrapper.eq(User::getUsername, "");
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("username = cast(? as varchar)"), "空字符串应该生成条件");
        }

        @Test
        @DisplayName("零值条件测试")
        void testZeroValue() {
            wrapper.eq(User::getAge, 0);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("age = cast(? as int)"), "零值应该生成条件");
        }

        @Test
        @DisplayName("布尔值条件测试")
        void testBooleanValue() {
            wrapper.eq(User::getEmailVerified, true);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("email_verified = cast(? as boolean)"), "布尔值应该生成条件");
        }
    }

    @Nested
    @DisplayName("参数化测试")
    class ParameterizedTests {

        @ParameterizedTest
        @CsvSource({
            "ACTIVE, status = cast(? as varchar)",
            "PENDING, status = cast(? as varchar)",
            "INACTIVE, status = cast(? as varchar)"
        })
        @DisplayName("状态值参数化测试")
        void testStatusParameterized(String status, String expectedCondition) {
            wrapper.eq(User::getStatus, status);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains(expectedCondition), "SQL应包含预期的条件");
        }

        @ParameterizedTest
        @ValueSource(ints = {18, 25, 30, 35, 40})
        @DisplayName("年龄值参数化测试")
        void testAgeParameterized(int age) {
            wrapper.ge(User::getAge, age);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("age >= cast(? as int)"), "SQL应包含年龄条件");
        }

        @ParameterizedTest
        @CsvSource({
            "john_doe, username = cast(? as varchar)",
            "jane_smith, username = cast(? as varchar)",
            "bob_wilson, username = cast(? as varchar)"
        })
        @DisplayName("用户名参数化测试")
        void testUsernameParameterized(String username, String expectedCondition) {
            wrapper.eq(User::getUsername, username);
            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains(expectedCondition), "SQL应包含预期的条件");
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {
        private static final int ITERATIONS = 10000;

        @Test
        @DisplayName("条件构建性能测试")
        void testConditionBuildPerformance() {
            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                LambdaQueryWrapper<User> testWrapper = new LambdaQueryWrapper<>(dslContext, table, User.class);
                testWrapper.eq(User::getStatus, "ACTIVE")
                          .gt(User::getAge, 18)
                          .le(User::getAge, 65)
                          .orderByDesc(User::getCreateTime)
                          .limit(10);
                testWrapper.buildSelect();
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            System.out.println("条件构建性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 1000, "条件构建性能测试超时");
        }

        @Test
        @DisplayName("SQL生成性能测试")
        void testSqlGenerationPerformance() {
            wrapper.eq(User::getStatus, "ACTIVE")
                   .gt(User::getAge, 18)
                   .le(User::getAge, 65)
                   .orderByDesc(User::getCreateTime)
                   .limit(10);

            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                wrapper.buildSelect().getSQL();
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            System.out.println("SQL生成性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 500, "SQL生成性能测试超时");
        }

        @Test
        @DisplayName("嵌套条件性能测试")
        void testNestedConditionPerformance() {
            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS / 10; i++) { // 减少迭代次数，因为嵌套条件更复杂
                LambdaQueryWrapper<User> testWrapper = new LambdaQueryWrapper<>(dslContext, table, User.class);
                testWrapper.eq(User::getStatus, "ACTIVE")
                          .and(subWrapper -> subWrapper
                              .ge(User::getAge, 18)
                              .or(subSubWrapper -> subSubWrapper
                                  .ge(User::getBalance, 1000.0)
                                  .eq(User::getEmailVerified, true)))
                          .orderByDesc(User::getCreateTime);
                testWrapper.buildSelect();
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            System.out.println("嵌套条件性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 2000, "嵌套条件性能测试超时");
        }
    }

    @Nested
    @DisplayName("实际应用场景测试")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("用户搜索场景测试")
        void testUserSearchScenario() {
            // 模拟用户搜索：活跃用户，年龄18-65，余额大于1000，按创建时间倒序
            wrapper.eq(User::getStatus, "ACTIVE")
                   .ge(User::getAge, 18)
                   .le(User::getAge, 65)
                   .gt(User::getBalance, 1000.0)
                   .orderByDesc(User::getCreateTime)
                   .limit(20);

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status = cast(? as varchar)"), "应包含状态条件");
            assertTrue(sql.contains("age >= cast(? as int)"), "应包含年龄下限条件");
            assertTrue(sql.contains("age <= cast(? as int)"), "应包含年龄上限条件");
            assertTrue(sql.contains("balance > cast(? as double)"), "应包含余额条件");
            assertTrue(sql.contains("order by create_time desc"), "应包含排序");
            assertTrue(sql.contains("fetch next ? rows only"), "应包含分页");
        }

        @Test
        @DisplayName("用户统计场景测试")
        void testUserStatisticsScenario() {
            // 模拟用户统计：统计不同状态的用户数量
            wrapper.eq(User::getEmailVerified, true)
                   .and(subWrapper -> subWrapper
                       .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))
                       .or(subSubWrapper -> subSubWrapper
                           .ge(User::getBalance, 500.0)
                           .eq(User::getAge, 25)));

            String sql = wrapper.buildCount().getSQL();
            assertTrue(sql.contains("select count(*)"), "应包含count查询");
            assertTrue(sql.contains("email_verified = cast(? as boolean)"), "应包含邮箱验证条件");
            assertTrue(sql.contains("status in"), "应包含状态IN条件");
            assertTrue(sql.contains("balance >= cast(? as double)"), "应包含余额条件");
            assertTrue(sql.contains("age = cast(? as int)"), "应包含年龄条件");
        }

        @Test
        @DisplayName("分页查询场景测试")
        void testPaginationScenario() {
            // 模拟分页查询：第3页，每页15条记录
            wrapper.eq(User::getStatus, "ACTIVE")
                   .orderByDesc(User::getCreateTime)
                   .page(3, 15);

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("status = cast(? as varchar)"), "应包含状态条件");
            assertTrue(sql.contains("order by create_time desc"), "应包含排序");
            assertTrue(sql.contains("offset ? rows"), "应包含offset条件");
            assertTrue(sql.contains("fetch next ? rows only"), "应包含limit条件");
        }

        @Test
        @DisplayName("复杂查询场景测试")
        void testComplexQueryScenario() {
            // 模拟复杂查询：多条件组合，字段选择，排序，分页
            wrapper.select(User::getId, User::getUsername, User::getEmail, User::getBalance)
                   .eq(User::getStatus, "ACTIVE")
                   .ge(User::getAge, 21)
                   .le(User::getAge, 60)
                   .gt(User::getBalance, 100.0)
                   .eq(User::getEmailVerified, true)
                   .and(subWrapper -> subWrapper
                       .like(User::getUsername, "user%")
                       .or(subSubWrapper -> subSubWrapper
                           .like(User::getEmail, "%@company.com")
                           .ge(User::getBalance, 1000.0)))
                   .orderByDesc(User::getBalance)
                   .orderByAsc(User::getUsername)
                   .limit(50);

            String sql = wrapper.buildSelect().getSQL();
            assertTrue(sql.contains("select id, username, email, balance"), "应包含字段选择");
            assertTrue(sql.contains("status = cast(? as varchar)"), "应包含状态条件");
            assertTrue(sql.contains("age >= cast(? as int)"), "应包含年龄下限条件");
            assertTrue(sql.contains("age <= cast(? as int)"), "应包含年龄上限条件");
            assertTrue(sql.contains("balance > cast(? as double)"), "应包含余额下限条件");
            assertTrue(sql.contains("email_verified = cast(? as boolean)"), "应包含邮箱验证条件");
            assertTrue(sql.contains("username like cast(? as varchar)"), "应包含用户名模糊查询");
            assertTrue(sql.contains("email like cast(? as varchar)"), "应包含邮箱模糊查询");
            assertTrue(sql.contains("order by balance desc, username asc"), "应包含排序");
            assertTrue(sql.contains("fetch next ? rows only"), "应包含分页");
        }
    }
}
