package cn.qaiu.example.controller;

import cn.qaiu.example.service.UserService;
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
public final class UserController_Factory implements Factory<UserController> {
  private final Provider<UserService> userServiceProvider;

  public UserController_Factory(Provider<UserService> userServiceProvider) {
    this.userServiceProvider = userServiceProvider;
  }

  @Override
  public UserController get() {
    return newInstance(userServiceProvider.get());
  }

  public static UserController_Factory create(Provider<UserService> userServiceProvider) {
    return new UserController_Factory(userServiceProvider);
  }

  public static UserController newInstance(UserService userService) {
    return new UserController(userService);
  }
}
