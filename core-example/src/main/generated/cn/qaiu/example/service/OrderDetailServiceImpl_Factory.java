package cn.qaiu.example.service;

import cn.qaiu.example.dao.OrderDetailDao;
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
public final class OrderDetailServiceImpl_Factory implements Factory<OrderDetailServiceImpl> {
  private final Provider<OrderDetailDao> orderDetailDaoProvider;

  public OrderDetailServiceImpl_Factory(Provider<OrderDetailDao> orderDetailDaoProvider) {
    this.orderDetailDaoProvider = orderDetailDaoProvider;
  }

  @Override
  public OrderDetailServiceImpl get() {
    return newInstance(orderDetailDaoProvider.get());
  }

  public static OrderDetailServiceImpl_Factory create(
      Provider<OrderDetailDao> orderDetailDaoProvider) {
    return new OrderDetailServiceImpl_Factory(orderDetailDaoProvider);
  }

  public static OrderDetailServiceImpl newInstance(OrderDetailDao orderDetailDao) {
    return new OrderDetailServiceImpl(orderDetailDao);
  }
}
