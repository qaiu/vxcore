package cn.qaiu.example.service;

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
public final class MultiDataSourceUserServiceImpl_Factory implements Factory<MultiDataSourceUserServiceImpl> {
  private final Provider<JooqExecutor> executorProvider;

  private MultiDataSourceUserServiceImpl_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public MultiDataSourceUserServiceImpl get() {
    return newInstance(executorProvider.get());
  }

  public static MultiDataSourceUserServiceImpl_Factory create(
      Provider<JooqExecutor> executorProvider) {
    return new MultiDataSourceUserServiceImpl_Factory(executorProvider);
  }

  public static MultiDataSourceUserServiceImpl newInstance(JooqExecutor executor) {
    return new MultiDataSourceUserServiceImpl(executor);
  }
}
