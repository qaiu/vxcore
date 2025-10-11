package cn.qaiu.vx.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Set;
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
    "KotlinInternalInJava"
})
public final class ServiceModule_ProvideServiceClassesFactory implements Factory<Set<Class<?>>> {
  private final ServiceModule module;

  public ServiceModule_ProvideServiceClassesFactory(ServiceModule module) {
    this.module = module;
  }

  @Override
  public Set<Class<?>> get() {
    return provideServiceClasses(module);
  }

  public static ServiceModule_ProvideServiceClassesFactory create(ServiceModule module) {
    return new ServiceModule_ProvideServiceClassesFactory(module);
  }

  public static Set<Class<?>> provideServiceClasses(ServiceModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideServiceClasses());
  }
}
