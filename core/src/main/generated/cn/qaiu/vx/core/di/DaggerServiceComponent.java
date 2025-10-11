package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.verticle.ServiceVerticle;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerServiceComponent {
  private DaggerServiceComponent() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static ServiceComponent create() {
    return new Builder().build();
  }

  public static final class Builder {
    private ServiceModule serviceModule;

    private Builder() {
    }

    public Builder serviceModule(ServiceModule serviceModule) {
      this.serviceModule = Preconditions.checkNotNull(serviceModule);
      return this;
    }

    public ServiceComponent build() {
      if (serviceModule == null) {
        this.serviceModule = new ServiceModule();
      }
      return new ServiceComponentImpl(serviceModule);
    }
  }

  private static final class ServiceComponentImpl implements ServiceComponent {
    private final ServiceComponentImpl serviceComponentImpl = this;

    private Provider<Set<Class<?>>> provideServiceClassesProvider;

    private Provider<Set<Class<?>>> provideDaoClassesProvider;

    private Provider<Set<Class<?>>> provideComponentClassesProvider;

    private Provider<Set<Class<?>>> provideRepositoryClassesProvider;

    private Provider<Set<Class<?>>> provideControllerClassesProvider;

    private Provider<Map<String, Set<Class<?>>>> provideAnnotatedClassesMapProvider;

    private Provider<Map<String, String>> provideAnnotatedClassNamesMapProvider;

    private ServiceComponentImpl(ServiceModule serviceModuleParam) {

      initialize(serviceModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ServiceModule serviceModuleParam) {
      this.provideServiceClassesProvider = DoubleCheck.provider(ServiceModule_ProvideServiceClassesFactory.create(serviceModuleParam));
      this.provideDaoClassesProvider = DoubleCheck.provider(ServiceModule_ProvideDaoClassesFactory.create(serviceModuleParam));
      this.provideComponentClassesProvider = DoubleCheck.provider(ServiceModule_ProvideComponentClassesFactory.create(serviceModuleParam));
      this.provideRepositoryClassesProvider = DoubleCheck.provider(ServiceModule_ProvideRepositoryClassesFactory.create(serviceModuleParam));
      this.provideControllerClassesProvider = DoubleCheck.provider(ServiceModule_ProvideControllerClassesFactory.create(serviceModuleParam));
      this.provideAnnotatedClassesMapProvider = DoubleCheck.provider(ServiceModule_ProvideAnnotatedClassesMapFactory.create(serviceModuleParam));
      this.provideAnnotatedClassNamesMapProvider = DoubleCheck.provider(ServiceModule_ProvideAnnotatedClassNamesMapFactory.create(serviceModuleParam));
    }

    @Override
    public void inject(ServiceVerticle serviceVerticle) {
    }

    @Override
    public Set<Class<?>> serviceClasses() {
      return provideServiceClassesProvider.get();
    }

    @Override
    public Set<Class<?>> daoClasses() {
      return provideDaoClassesProvider.get();
    }

    @Override
    public Set<Class<?>> componentClasses() {
      return provideComponentClassesProvider.get();
    }

    @Override
    public Set<Class<?>> repositoryClasses() {
      return provideRepositoryClassesProvider.get();
    }

    @Override
    public Set<Class<?>> controllerClasses() {
      return provideControllerClassesProvider.get();
    }

    @Override
    public Map<String, Set<Class<?>>> annotatedClassesMap() {
      return provideAnnotatedClassesMapProvider.get();
    }

    @Override
    public Map<String, String> annotatedClassNamesMap() {
      return provideAnnotatedClassNamesMapProvider.get();
    }
  }
}
