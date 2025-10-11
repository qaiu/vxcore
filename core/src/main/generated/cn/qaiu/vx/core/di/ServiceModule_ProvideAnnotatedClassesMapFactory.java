package cn.qaiu.vx.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Map;
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
public final class ServiceModule_ProvideAnnotatedClassesMapFactory implements Factory<Map<String, Set<Class<?>>>> {
  private final ServiceModule module;

  public ServiceModule_ProvideAnnotatedClassesMapFactory(ServiceModule module) {
    this.module = module;
  }

  @Override
  public Map<String, Set<Class<?>>> get() {
    return provideAnnotatedClassesMap(module);
  }

  public static ServiceModule_ProvideAnnotatedClassesMapFactory create(ServiceModule module) {
    return new ServiceModule_ProvideAnnotatedClassesMapFactory(module);
  }

  public static Map<String, Set<Class<?>>> provideAnnotatedClassesMap(ServiceModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideAnnotatedClassesMap());
  }
}
