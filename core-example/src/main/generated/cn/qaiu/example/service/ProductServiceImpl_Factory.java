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
public final class ProductServiceImpl_Factory implements Factory<ProductServiceImpl> {
  private final Provider<JooqExecutor> executorProvider;

  public ProductServiceImpl_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public ProductServiceImpl get() {
    return newInstance(executorProvider.get());
  }

  public static ProductServiceImpl_Factory create(Provider<JooqExecutor> executorProvider) {
    return new ProductServiceImpl_Factory(executorProvider);
  }

  public static ProductServiceImpl newInstance(JooqExecutor executor) {
    return new ProductServiceImpl(executor);
  }
}
