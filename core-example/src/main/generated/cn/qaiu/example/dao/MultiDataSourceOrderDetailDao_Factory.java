package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MultiDataSourceOrderDetailDao_Factory implements Factory<MultiDataSourceOrderDetailDao> {
  private final Provider<JooqExecutor> executorProvider;

  private MultiDataSourceOrderDetailDao_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public MultiDataSourceOrderDetailDao get() {
    return newInstance(executorProvider.get());
  }

  public static MultiDataSourceOrderDetailDao_Factory create(
      Provider<JooqExecutor> executorProvider) {
    return new MultiDataSourceOrderDetailDao_Factory(executorProvider);
  }

  public static MultiDataSourceOrderDetailDao newInstance(JooqExecutor executor) {
    return new MultiDataSourceOrderDetailDao(executor);
  }
}
