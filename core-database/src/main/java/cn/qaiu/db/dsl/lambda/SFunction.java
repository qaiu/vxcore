package cn.qaiu.db.dsl.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 序列化的函数接口
 * 用于Lambda表达式的序列化，支持从Lambda表达式中提取字段信息
 * 
 * @param <T> 输入类型（实体类）
 * @param <R> 返回类型（字段类型）
 * @author qaiu
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
    // 继承Function接口，同时支持序列化
}
