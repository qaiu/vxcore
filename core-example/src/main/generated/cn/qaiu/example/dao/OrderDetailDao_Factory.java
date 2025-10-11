package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class OrderDetailDao_Factory implements Factory<OrderDetailDao> {
  private final Provider<JooqExecutor> executorProvider;

  public OrderDetailDao_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public OrderDetailDao get() {
    return newInstance(executorProvider.get());
  }

  public static OrderDetailDao_Factory create(Provider<JooqExecutor> executorProvider) {
    return new OrderDetailDao_Factory(executorProvider);
  }

  public static OrderDetailDao newInstance(JooqExecutor executor) {
    return new OrderDetailDao(executor);
  }
}
