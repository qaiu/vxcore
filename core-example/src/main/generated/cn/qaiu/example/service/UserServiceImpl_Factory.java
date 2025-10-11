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
public final class UserServiceImpl_Factory implements Factory<UserServiceImpl> {
  private final Provider<JooqExecutor> executorProvider;

  public UserServiceImpl_Factory(Provider<JooqExecutor> executorProvider) {
    this.executorProvider = executorProvider;
  }

  @Override
  public UserServiceImpl get() {
    return newInstance(executorProvider.get());
  }

  public static UserServiceImpl_Factory create(Provider<JooqExecutor> executorProvider) {
    return new UserServiceImpl_Factory(executorProvider);
  }

  public static UserServiceImpl newInstance(JooqExecutor executor) {
    return new UserServiceImpl(executor);
  }
}
