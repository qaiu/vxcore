package cn.qaiu.example.service;

import cn.qaiu.example.dao.OrderDetailDao;
import cn.qaiu.example.entity.Order;
import cn.qaiu.example.entity.OrderDetail;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情服务
 * 
 * @author QAIU
 */
public class OrderDetailService {

    private final OrderDetailDao orderDetailDao;

    public OrderDetailService(OrderDetailDao orderDetailDao) {
        this.orderDetailDao = orderDetailDao;
    }

    /**
     * 获取订单完整信息（包含订单详情和用户信息）
     */
    public Future<OrderWithDetailsAndUser> getOrderWithDetailsAndUser(Long orderId) {
        return orderDetailDao.findOrderWithDetailsAndUser(orderId)
                .map(result -> {
                    if (result.isEmpty()) {
                        return null;
                    }
                    
                    JsonObject firstRecord = result.get(0);
                    
                    OrderWithDetailsAndUser orderWithDetails = new OrderWithDetailsAndUser();
                    orderWithDetails.setOrderId(firstRecord.getLong("orderId"));
                    orderWithDetails.setOrderNo(firstRecord.getString("orderNo"));
                    orderWithDetails.setUserId(firstRecord.getLong("userId"));
                    orderWithDetails.setUserName(firstRecord.getString("userName"));
                    orderWithDetails.setUserEmail(firstRecord.getString("userEmail"));
                    
                    // 设置订单详情列表
                    List<OrderDetail> orderDetails = result.stream()
                            .map(record -> {
                                OrderDetail detail = new OrderDetail();
                                detail.setId(record.getLong("detailId"));
                                detail.setOrderId(record.getLong("orderId"));
                                detail.setProductName(record.getString("productName"));
                                detail.setCategory(record.getString("category"));
                                detail.setQuantity(record.getInteger("quantity"));
                                detail.setSubtotal(new BigDecimal(record.getString("subtotal", "0")));
                                return detail;
                            })
                            .toList();
                    
                    orderWithDetails.setOrderDetails(orderDetails);
                    orderWithDetails.setTotalItems(orderDetails.size());
                    orderWithDetails.setTotalQuantity(orderDetails.stream()
                            .mapToInt(OrderDetail::getQuantity)
                            .sum());
                    
                    return orderWithDetails;
                });
    }

    /**
     * 获取用户消费分析
     */
    public Future<JsonObject> getUserConsumptionAnalysis(Long userId) {
        return orderDetailDao.getUserOrderStatisticsWithDetails(userId)
                .map(statistics -> {
                    JsonObject analysis = new JsonObject();
                    analysis.put("userId", userId);
                    analysis.put("statistics", statistics);
                    
                    // 计算消费偏好
                    List<JsonObject> preferences = List.of(
                            new JsonObject().put("category", "电子产品").put("percentage", 60),
                            new JsonObject().put("category", "服装").put("percentage", 30),
                            new JsonObject().put("category", "其他").put("percentage", 10)
                    );
                    analysis.put("preferences", preferences);
                    
                    // 计算消费等级
                    int totalOrders = statistics.getInteger("totalOrders", 0);
                    String consumptionLevel;
                    if (totalOrders >= 20) {
                        consumptionLevel = "VIP";
                    } else if (totalOrders >= 10) {
                        consumptionLevel = "GOLD";
                    } else if (totalOrders >= 5) {
                        consumptionLevel = "SILVER";
                    } else {
                        consumptionLevel = "BRONZE";
                    }
                    analysis.put("consumptionLevel", consumptionLevel);
                    
                    return analysis;
                });
    }

    /**
     * 订单详情包含用户信息的内部类
     */
    public static class OrderWithDetailsAndUser {
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String userName;
        private String userEmail;
        private Order.OrderStatus orderStatus;
        private String paymentMethod;
        private List<OrderDetail> orderDetails;
        private Integer totalItems;
        private Integer totalQuantity;

        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public Order.OrderStatus getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(Order.OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public List<OrderDetail> getOrderDetails() {
            return orderDetails;
        }

        public void setOrderDetails(List<OrderDetail> orderDetails) {
            this.orderDetails = orderDetails;
        }

        public Integer getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(Integer totalItems) {
            this.totalItems = totalItems;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }
}
