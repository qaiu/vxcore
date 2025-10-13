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
import cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Order>;
import cn.qaiu.example.entity.Order;
import Object;
import Long;
import cn.qaiu.db.dsl.lambda.LambdaPageResult<Object>;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceVertxEBProxy implements OrderService {
  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public OrderServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public OrderServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {
    }
  }

  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> findByUserId(java.lang.Long userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findByUserId");
    _deliveryOptions.getHeaders().set("action", "findByUserId");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> findByStatus(java.lang.String status){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("status", status);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findByStatus");
    _deliveryOptions.getHeaders().set("action", "findByStatus");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> findByTimeRange(java.lang.String startTime, java.lang.String endTime){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("startTime", startTime);
    _json.put("endTime", endTime);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findByTimeRange");
    _deliveryOptions.getHeaders().set("action", "findByTimeRange");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Order>> findUserOrders(java.lang.Long userId, long page, long size){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("page", page);
    _json.put("size", size);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findUserOrders");
    _deliveryOptions.getHeaders().set("action", "findUserOrders");
    return _vertx.eventBus().<cn.qaiu.db.dsl.lambda.LambdaPageResult<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Long> countUserOrders(java.lang.Long userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "countUserOrders");
    _deliveryOptions.getHeaders().set("action", "countUserOrders");
    return _vertx.eventBus().<java.lang.Long>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.String> calculateUserTotalAmount(java.lang.Long userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "calculateUserTotalAmount");
    _deliveryOptions.getHeaders().set("action", "calculateUserTotalAmount");
    return _vertx.eventBus().<java.lang.String>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getPendingOrders(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getPendingOrders");
    _deliveryOptions.getHeaders().set("action", "getPendingOrders");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> updateOrderStatus(java.lang.Long orderId, java.lang.String status){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);
    _json.put("status", status);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "updateOrderStatus");
    _deliveryOptions.getHeaders().set("action", "updateOrderStatus");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<cn.qaiu.example.entity.Order> createOrder(java.lang.Long userId, java.lang.Long productId, java.lang.Integer quantity){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("productId", productId);
    _json.put("quantity", quantity);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "createOrder");
    _deliveryOptions.getHeaders().set("action", "createOrder");
    return _vertx.eventBus().<cn.qaiu.example.entity.Order>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<cn.qaiu.example.entity.Order> getOrderById(java.lang.Long id){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("id", id);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getOrderById");
    _deliveryOptions.getHeaders().set("action", "getOrderById");
    return _vertx.eventBus().<cn.qaiu.example.entity.Order>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getOrdersByOrderNo(java.lang.String orderNo){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderNo", orderNo);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getOrdersByOrderNo");
    _deliveryOptions.getHeaders().set("action", "getOrdersByOrderNo");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getUserOrders(java.lang.Long userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getUserOrders");
    _deliveryOptions.getHeaders().set("action", "getUserOrders");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getProductOrders(java.lang.Long productId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("productId", productId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getProductOrders");
    _deliveryOptions.getHeaders().set("action", "getProductOrders");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getOrdersByStatus(java.lang.String status){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("status", status);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getOrdersByStatus");
    _deliveryOptions.getHeaders().set("action", "getOrdersByStatus");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> payOrder(java.lang.Long orderId, java.lang.String paymentMethod){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);
    _json.put("paymentMethod", paymentMethod);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "payOrder");
    _deliveryOptions.getHeaders().set("action", "payOrder");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> shipOrder(java.lang.Long orderId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "shipOrder");
    _deliveryOptions.getHeaders().set("action", "shipOrder");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> confirmDelivery(java.lang.Long orderId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "confirmDelivery");
    _deliveryOptions.getHeaders().set("action", "confirmDelivery");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> cancelOrder(java.lang.Long orderId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "cancelOrder");
    _deliveryOptions.getHeaders().set("action", "cancelOrder");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<io.vertx.core.json.JsonObject> getOrderStatistics(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getOrderStatistics");
    _deliveryOptions.getHeaders().set("action", "getOrderStatistics");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<io.vertx.core.json.JsonObject> getUserOrderStatistics(java.lang.Long userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getUserOrderStatistics");
    _deliveryOptions.getHeaders().set("action", "getUserOrderStatistics");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> searchOrders(java.lang.String keyword){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("keyword", keyword);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "searchOrders");
    _deliveryOptions.getHeaders().set("action", "searchOrders");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.lang.Boolean> deleteOrder(java.lang.Long orderId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("orderId", orderId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "deleteOrder");
    _deliveryOptions.getHeaders().set("action", "deleteOrder");
    return _vertx.eventBus().<java.lang.Boolean>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body();
    });
  }
  @Override
  public io.vertx.core.Future<java.util.List<cn.qaiu.example.entity.Order>> getAllOrders(){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getAllOrders");
    _deliveryOptions.getHeaders().set("action", "getAllOrders");
    return _vertx.eventBus().<java.util.List<cn.qaiu.example.entity.Order>>request(_address, _json, _deliveryOptions).map(msg -> {
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
