package cn.qaiu.vx.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Map;
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
public final class ServiceModule_ProvideAnnotatedClassNamesMapFactory implements Factory<Map<String, String>> {
  private final ServiceModule module;

  public ServiceModule_ProvideAnnotatedClassNamesMapFactory(ServiceModule module) {
    this.module = module;
  }

  @Override
  public Map<String, String> get() {
    return provideAnnotatedClassNamesMap(module);
  }

  public static ServiceModule_ProvideAnnotatedClassNamesMapFactory create(ServiceModule module) {
    return new ServiceModule_ProvideAnnotatedClassNamesMapFactory(module);
  }

  public static Map<String, String> provideAnnotatedClassNamesMap(ServiceModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideAnnotatedClassNamesMap());
  }
}
