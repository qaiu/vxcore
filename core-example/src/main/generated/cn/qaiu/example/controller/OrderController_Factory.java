package cn.qaiu.example.controller;

import cn.qaiu.example.service.OrderService;
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
public final class OrderController_Factory implements Factory<OrderController> {
  private final Provider<OrderService> orderServiceProvider;

  private OrderController_Factory(Provider<OrderService> orderServiceProvider) {
    this.orderServiceProvider = orderServiceProvider;
  }

  @Override
  public OrderController get() {
    return newInstance(orderServiceProvider.get());
  }

  public static OrderController_Factory create(Provider<OrderService> orderServiceProvider) {
    return new OrderController_Factory(orderServiceProvider);
  }

  public static OrderController newInstance(OrderService orderService) {
    return new OrderController(orderService);
  }
}
