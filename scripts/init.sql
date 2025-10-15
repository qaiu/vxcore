-- VXCore 数据库初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS vxcore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE vxcore;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(255) COMMENT '地址',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    category VARCHAR(50) COMMENT '商品分类',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 创建订单商品关联表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- 插入测试数据
INSERT INTO users (name, email, age, phone, address, status) VALUES
('张三', 'zhangsan@example.com', 25, '13800138001', '北京市朝阳区', 'ACTIVE'),
('李四', 'lisi@example.com', 30, '13800138002', '上海市浦东新区', 'ACTIVE'),
('王五', 'wangwu@example.com', 28, '13800138003', '广州市天河区', 'ACTIVE'),
('赵六', 'zhaoliu@example.com', 35, '13800138004', '深圳市南山区', 'ACTIVE'),
('钱七', 'qianqi@example.com', 22, '13800138005', '杭州市西湖区', 'ACTIVE');

INSERT INTO products (name, description, price, stock, category, status) VALUES
('iPhone 15', '苹果最新款手机', 7999.00, 100, '手机', 'ACTIVE'),
('MacBook Pro', '苹果笔记本电脑', 12999.00, 50, '电脑', 'ACTIVE'),
('AirPods Pro', '苹果无线耳机', 1999.00, 200, '配件', 'ACTIVE'),
('iPad Air', '苹果平板电脑', 4399.00, 80, '平板', 'ACTIVE'),
('Apple Watch', '苹果智能手表', 2999.00, 150, '手表', 'ACTIVE');

INSERT INTO orders (user_id, order_no, amount, status) VALUES
(1, 'ORD202401010001', 9998.00, 'COMPLETED'),
(2, 'ORD202401010002', 12999.00, 'PENDING'),
(3, 'ORD202401010003', 1999.00, 'SHIPPED'),
(4, 'ORD202401010004', 4399.00, 'COMPLETED'),
(5, 'ORD202401010005', 2999.00, 'CANCELLED');

INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 7999.00),
(1, 3, 1, 1999.00),
(2, 2, 1, 12999.00),
(3, 3, 1, 1999.00),
(4, 4, 1, 4399.00),
(5, 5, 1, 2999.00);

-- 创建视图
CREATE VIEW user_order_summary AS
SELECT 
    u.id,
    u.name,
    u.email,
    COUNT(o.id) as order_count,
    COALESCE(SUM(o.amount), 0) as total_amount,
    MAX(o.created_at) as last_order_date
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name, u.email;

-- 创建存储过程
DELIMITER //

CREATE PROCEDURE GetUserOrders(IN user_id BIGINT)
BEGIN
    SELECT 
        o.id,
        o.order_no,
        o.amount,
        o.status,
        o.created_at,
        GROUP_CONCAT(p.name SEPARATOR ', ') as products
    FROM orders o
    LEFT JOIN order_items oi ON o.id = oi.order_id
    LEFT JOIN products p ON oi.product_id = p.id
    WHERE o.user_id = user_id
    GROUP BY o.id, o.order_no, o.amount, o.status, o.created_at
    ORDER BY o.created_at DESC;
END //

DELIMITER ;

-- 创建触发器
DELIMITER //

CREATE TRIGGER update_product_stock_after_order
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE products 
    SET stock = stock - NEW.quantity 
    WHERE id = NEW.product_id;
END //

DELIMITER ;

-- 创建索引优化
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_status ON orders(created_at, status);
CREATE INDEX idx_products_category_status ON products(category, status);

-- 分析表
ANALYZE TABLE users, orders, products, order_items;