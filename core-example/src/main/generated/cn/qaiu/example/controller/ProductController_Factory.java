package cn.qaiu.example.controller;

import cn.qaiu.example.service.ProductService;
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
public final class ProductController_Factory implements Factory<ProductController> {
  private final Provider<ProductService> productServiceProvider;

  private ProductController_Factory(Provider<ProductService> productServiceProvider) {
    this.productServiceProvider = productServiceProvider;
  }

  @Override
  public ProductController get() {
    return newInstance(productServiceProvider.get());
  }

  public static ProductController_Factory create(Provider<ProductService> productServiceProvider) {
    return new ProductController_Factory(productServiceProvider);
  }

  public static ProductController newInstance(ProductService productService) {
    return new ProductController(productService);
  }
}
