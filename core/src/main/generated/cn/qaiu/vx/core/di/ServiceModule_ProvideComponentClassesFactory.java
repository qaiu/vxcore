package cn.qaiu.vx.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Set;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class ServiceModule_ProvideComponentClassesFactory implements Factory<Set<Class<?>>> {
  private final ServiceModule module;

  public ServiceModule_ProvideComponentClassesFactory(ServiceModule module) {
    this.module = module;
  }

  @Override
  public Set<Class<?>> get() {
    return provideComponentClasses(module);
  }

  public static ServiceModule_ProvideComponentClassesFactory create(ServiceModule module) {
    return new ServiceModule_ProvideComponentClassesFactory(module);
  }

  public static Set<Class<?>> provideComponentClasses(ServiceModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideComponentClasses());
  }
}
