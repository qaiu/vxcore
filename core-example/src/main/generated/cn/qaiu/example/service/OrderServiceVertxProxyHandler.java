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

import cn.qaiu.example.service.OrderService;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import io.vertx.serviceproxy.ProxyHandler;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceBinder;
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
public class OrderServiceVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 
  private final Vertx vertx;
  private final OrderService service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;
  private final boolean includeDebugInfo;

  public OrderServiceVertxProxyHandler(Vertx vertx, OrderService service){
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public OrderServiceVertxProxyHandler(Vertx vertx, OrderService service, long timeoutInSecond){
    this(vertx, service, true, timeoutInSecond);
  }

  public OrderServiceVertxProxyHandler(Vertx vertx, OrderService service, boolean topLevel, long timeoutInSecond){
    this(vertx, service, true, timeoutInSecond, false);
  }

  public OrderServiceVertxProxyHandler(Vertx vertx, OrderService service, boolean topLevel, long timeoutSeconds, boolean includeDebugInfo) {
      this.vertx = vertx;
      this.service = service;
      this.includeDebugInfo = includeDebugInfo;
      this.timeoutSeconds = timeoutSeconds;
      try {
        this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
            new ServiceExceptionMessageCodec());
      } catch (IllegalStateException ex) {}
      if (timeoutSeconds != -1 && !topLevel) {
        long period = timeoutSeconds * 1000 / 2;
        if (period > 10000) {
          period = 10000;
        }
        this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
      } else {
        this.timerID = -1;
      }
      accessed();
    }

  private void checkTimedOut(long id) {
    long now = System.nanoTime();
    if (now - lastAccessed > timeoutSeconds * 1000000000) {
      close();
    }
  }

    @Override
    public void close() {
      if (timerID != -1) {
        vertx.cancelTimer(timerID);
      }
      super.close();
    }

    private void accessed() {
      this.lastAccessed = System.nanoTime();
    }

  public void handle(Message<JsonObject> msg) {
    try{
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) throw new IllegalStateException("action not specified");
      accessed();
      switch (action) {
        case "findByUserId": {
          service.findByUserId(json.getValue("userId") == null ? null : (json.getLong("userId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "findByStatus": {
          service.findByStatus((java.lang.String)json.getValue("status")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "findByTimeRange": {
          service.findByTimeRange((java.lang.String)json.getValue("startTime"),
                                  (java.lang.String)json.getValue("endTime")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "findUserOrders": {
          service.findUserOrders(json.getValue("userId") == null ? null : (json.getLong("userId").longValue()),
                                  json.getValue("page") == null ? null : (json.getLong("page").longValue()),
                                  json.getValue("size") == null ? null : (json.getLong("size").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "countUserOrders": {
          service.countUserOrders(json.getValue("userId") == null ? null : (json.getLong("userId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "calculateUserTotalAmount": {
          service.calculateUserTotalAmount(json.getValue("userId") == null ? null : (json.getLong("userId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getPendingOrders": {
          service.getPendingOrders().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "updateOrderStatus": {
          service.updateOrderStatus(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue()),
                                  (java.lang.String)json.getValue("status")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "createOrder": {
          service.createOrder(json.getValue("userId") == null ? null : (json.getLong("userId").longValue()),
                                  json.getValue("productId") == null ? null : (json.getLong("productId").longValue()),
                                  json.getValue("quantity") == null ? null : (json.getLong("quantity").intValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getOrderById": {
          service.getOrderById(json.getValue("id") == null ? null : (json.getLong("id").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getOrdersByOrderNo": {
          service.getOrdersByOrderNo((java.lang.String)json.getValue("orderNo")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getUserOrders": {
          service.getUserOrders(json.getValue("userId") == null ? null : (json.getLong("userId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getProductOrders": {
          service.getProductOrders(json.getValue("productId") == null ? null : (json.getLong("productId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getOrdersByStatus": {
          service.getOrdersByStatus((java.lang.String)json.getValue("status")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "payOrder": {
          service.payOrder(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue()),
                                  (java.lang.String)json.getValue("paymentMethod")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "shipOrder": {
          service.shipOrder(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "confirmDelivery": {
          service.confirmDelivery(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "cancelOrder": {
          service.cancelOrder(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getOrderStatistics": {
          service.getOrderStatistics().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getUserOrderStatistics": {
          service.getUserOrderStatistics(json.getValue("userId") == null ? null : (json.getLong("userId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "searchOrders": {
          service.searchOrders((java.lang.String)json.getValue("keyword")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "deleteOrder": {
          service.deleteOrder(json.getValue("orderId") == null ? null : (json.getLong("orderId").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getAllOrders": {
          service.getAllOrders().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getById": {
          service.getById((Long)json.getValue("arg0")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getAll": {
          service.getAll().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "page": {
          service.page(json.getValue("arg0") == null ? null : (json.getLong("arg0").longValue()),
                                  json.getValue("arg1") == null ? null : (json.getLong("arg1").longValue())).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "count": {
          service.count().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "create": {
          service.create((Object)json.getValue("arg0")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "update": {
          service.update((Object)json.getValue("arg0")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "delete": {
          service.delete((Long)json.getValue("arg0")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "search": {
          service.search((java.lang.String)json.getValue("arg0")).onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        case "getStatistics": {
          service.getStatistics().onComplete(HelperUtils.createHandler(msg, includeDebugInfo));
          break;
        }
        default: throw new IllegalStateException("Invalid action: " + action);
      }
    } catch (Throwable t) {
      if (includeDebugInfo) msg.reply(new ServiceException(500, t.getMessage(), HelperUtils.generateDebugInfo(t)));
      else msg.reply(new ServiceException(500, t.getMessage()));
      throw t;
    }
  }
}
