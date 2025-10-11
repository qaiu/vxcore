package cn.qaiu.example.service;

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
public final class OrderServiceImpl_Factory implements Factory<OrderServiceImpl> {
  private final Provider<JooqExecutor> executorProvider;

  public OrderServiceImpl_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public OrderServiceImpl get() {
    return newInstance(executorProvider.get());
  }

  public static OrderServiceImpl_Factory create(Provider<JooqExecutor> executorProvider) {
    return new OrderServiceImpl_Factory(executorProvider);
  }

  public static OrderServiceImpl newInstance(JooqExecutor executor) {
    return new OrderServiceImpl(executor);
  }
}
