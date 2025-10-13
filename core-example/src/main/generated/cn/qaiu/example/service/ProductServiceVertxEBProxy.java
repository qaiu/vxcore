/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package cn.qaiu.example.service;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.ProxyUtils;
import cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Product>;
import java.math.BigDecimal;
import Object;
import Long;
import cn.qaiu.db.dsl.lambda.LambdaPageResult<Object>;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class ProductServiceVertxEBProxy implements ProductService {
  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public ProductServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public ProductServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {
    }
  }

  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Product>> findAvailableProducts(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findAvailableProducts");
    _deliveryOptions.getHeaders().set("action", "findAvailableProducts");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Product>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Product>> findByPriceRange(java.lang.String minPrice, java.lang.String maxPrice){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("minPrice", minPrice);
    _json.put("maxPrice", maxPrice);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findByPriceRange");
    _deliveryOptions.getHeaders().set("action", "findByPriceRange");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Product>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Product>> findByCategory(java.lang.Long categoryId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("categoryId", categoryId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findByCategory");
    _deliveryOptions.getHeaders().set("action", "findByCategory");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Product>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Product>> searchProducts(java.lang.String keyword, long page, long size){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("keyword", keyword);
    _json.put("page", page);
    _json.put("size", size);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "searchProducts");
    _deliveryOptions.getHeaders().set("action", "searchProducts");
    return _vertx.eventBus().<cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Product>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Product>> getHotProducts(int limit){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("limit", limit);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getHotProducts");
    _deliveryOptions.getHeaders().set("action", "getHotProducts");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Product>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Long> countProducts(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "countProducts");
    _deliveryOptions.getHeaders().set("action", "countProducts");
    return _vertx.eventBus().<java.lang.Long>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.math.BigDecimal> getAveragePrice(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getAveragePrice");
    _deliveryOptions.getHeaders().set("action", "getAveragePrice");
    return _vertx.eventBus().<java.math.BigDecimal>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<Object> getById(Long arg0){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getById");
    _deliveryOptions.getHeaders().set("action", "getById");
    return _vertx.eventBus().<Object>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<Object>> getAll(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getAll");
    _deliveryOptions.getHeaders().set("action", "getAll");
    return _vertx.eventBus().<java.util.List<Object>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<cn.qaiu.db.dsl.lambda.LambdaPageResult<Object>> page(long arg0, long arg1){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);
    _json.put("arg1", arg1);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "page");
    _deliveryOptions.getHeaders().set("action", "page");
    return _vertx.eventBus().<cn.qaiu.db.dsl.lambda.LambdaPageResult<Object>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Long> count(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "count");
    _deliveryOptions.getHeaders().set("action", "count");
    return _vertx.eventBus().<java.lang.Long>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<Object> create(Object arg0){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "create");
    _deliveryOptions.getHeaders().set("action", "create");
    return _vertx.eventBus().<Object>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> update(Object arg0){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "update");
    _deliveryOptions.getHeaders().set("action", "update");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> delete(Long arg0){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "delete");
    _deliveryOptions.getHeaders().set("action", "delete");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<Object>> search(java.lang.String arg0){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("arg0", arg0);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "search");
    _deliveryOptions.getHeaders().set("action", "search");
    return _vertx.eventBus().<java.util.List<Object>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<io.vertx.core.json.JsonObject> getStatistics(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getStatistics");
    _deliveryOptions.getHeaders().set("action", "getStatistics");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
}
