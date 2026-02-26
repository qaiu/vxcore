# Apache Bench (ab) 性能测试报告

**测试时间**: 2025-02-26  
**工具**: Apache Bench 2.3  
**说明**: 使用 `127.0.0.1` 进行压测（macOS 下使用 `localhost` 可能导致 ab 报错 `apr_socket_connect(): Invalid argument`）

| 服务 | 地址 | 说明 |
|------|------|------|
| **VXCore** | http://127.0.0.1:18084/hello | 本框架 demo-08-routing |
| **Quarkus** | http://127.0.0.1:8080/hello | Quarkus RESTEasy |

**响应体差异（公平性说明）**：两端的 `/hello` 响应体长度不一致，对本框架不利（本框架多传则多算、多序列化）。
- VXCore 响应体：**96 字节**（如 `"data":"from Vx.core REST"`）
- Quarkus 响应体：**93 字节**（如 `"data":"Hello RESTEasy"`）
- **结论**：若要做等长对比，Quarkus 端应在响应体中**补足 3 个字符**（即 3 字节），使与 VXCore 均为 96 字节后再测，结果才公平。

**响应头与总传输量（ab 的“传输速率”含头+体）**：ab 的 **Total transferred** = 响应头 + 响应体，因此“传输速率 (KB/s)”差距主要来自**每请求总字节数**不同，不是仅响应体。

| 项目 | VXCore (18084) | Quarkus (8080) | 差距 |
|------|----------------|----------------|------|
| **响应头** | ~402 字节 | ~85 字节 | **VXCore 多 ~317 字节** |
| **响应体** | 96 字节 | 93 字节 | 3 字节 |
| **总传输/请求** | **498 字节** | **178 字节** | VXCore 多 320 字节 |

**响应头差距明细**：
- **VXCore**：`access-control-allow-origin`、`date`、`access-control-allow-methods`、`access-control-allow-headers`、`access-control-max-age`、`content-type`、`content-length`、`x-response-time` 等 → 约 **402 字节**。
- **Quarkus**：仅 `content-length`、`Content-Type` → 约 **85 字节**。
- **响应头差距**：402 − 85 = **317 字节**（VXCore 多发的头约占每请求总传输的绝大部分差异）。

**用 Quarkus 的响应体补充（公平对比）**：  
要使 Quarkus 单请求总传输与 VXCore 相同（498 字节），需在 Quarkus 的**响应体**里补足总差量 498 − 178 = **320 字节**：
- 当前 Quarkus 响应体：93 字节  
- 补充后响应体：93 + 320 = **413 字节**（即**再补 320 个字符**）  
- 补充后总传输：85（头）+ 413（体）= **498 字节**，与 VXCore 一致。

**统一参数**: 总请求数 `-n 10000`，变化并发 `-c`（10 / 100 / 200）

---

## 1. 测试结果汇总

### 1.1 并发 10（-n 10000 -c 10）

| 指标 | VXCore (18084) | Quarkus (8080) | 差距 |
|------|----------------|----------------|------|
| **RPS (请求/秒)** | 9,311 | 12,366 | Quarkus 高约 **32.8%** |
| **平均响应时间 (ms)** | 1.074 | 0.809 | VXCore 慢约 **32.8%** |
| **P50 (ms)** | 1 | 1 | 相当 |
| **P95 (ms)** | 2 | 1 | VXCore 略慢 |
| **P99 (ms)** | 4 | 3 | VXCore 略慢 |
| **最大响应 (ms)** | 109 | 93 | 相当 |
| **单次响应体** | 96 bytes | 93 bytes | - |

### 1.2 并发 100（-n 10000 -c 100）

| 指标 | VXCore (18084) | Quarkus (8080) | 差距 |
|------|----------------|----------------|------|
| **RPS (请求/秒)** | 14,005 | 21,141 | Quarkus 高约 **50.9%** |
| **平均响应时间 (ms)** | 7.14 | 4.73 | VXCore 慢约 **50.9%** |
| **P50 (ms)** | 4 | 3 | Quarkus 略优 |
| **P95 (ms)** | 13 | 11 | Quarkus 略优 |
| **P99 (ms)** | 93 | 24 | **Quarkus 明显更稳** |
| **最大响应 (ms)** | 171 | 41 | **Quarkus 明显更稳** |
| **传输速率 (KB/s)** | 6,810 | 3,675 | VXCore 更高（响应体略大） |

### 1.3 并发 200（-n 10000 -c 200）

| 指标 | VXCore (18084) | Quarkus (8080) | 差距 |
|------|----------------|----------------|------|
| **RPS (请求/秒)** | 9,693 | 9,901 | 基本持平（Quarkus 高约 2.1%） |
| **平均响应时间 (ms)** | 20.63 | 20.20 | 相当 |
| **P50 (ms)** | 9 | 10 | 相当 |
| **P95 (ms)** | 43 | 45 | 相当 |
| **P99 (ms)** | 379 | 248 | **Quarkus 尾延迟更优** |
| **最大响应 (ms)** | 394 | 326 | **Quarkus 尾延迟更优** |

---

## 2. 结论与差距分析

### 2.1 吞吐（RPS）

- **低并发 (c=10)**：Quarkus 约 32.8% 更高 RPS。
- **中并发 (c=100)**：Quarkus 约 50.9% 更高 RPS，差距最大。
- **高并发 (c=200)**：两者 RPS 接近（约 9.7k），均受限于本机或 ab 客户端。

在 100 并发下，Quarkus 的 RPS 优势最明显；并发继续升高时，两者都出现瓶颈，吞吐趋于接近。

### 2.2 延迟与稳定性

- **中高并发下尾延迟**：Quarkus 的 P99、最大响应时间明显更优（例如 c=100 时 P99 24ms vs 93ms，c=200 时 248ms vs 379ms）。
- **原因推测**：
  - Quarkus 基于 Netty，针对高并发、低延迟做过大量优化。
  - VXCore 基于 Vert.x（也是事件驱动），但在路由、序列化、线程调度等细节上仍有优化空间；高并发下偶发长尾可能来自锁竞争、GC 或调度抖动。

### 2.3 可能优化方向（VXCore）

1. **路由与参数解析**：减少反射、缓存路由表与参数绑定，降低单请求开销。
2. **响应序列化**：统一、精简 JSON 序列化路径，避免多余拷贝与临时对象。
3. **事件循环与 Worker**：检查是否过多工作落在 Event Loop 上，合理使用 Worker 池；减少跨线程通信。
4. **连接与线程模型**：对照 Vert.x 官方调优建议（如 event loop 数量、worker pool size），结合 ab 的并发数做小规模对比测试。
5. **JVM/GC**：在相同堆大小与 GC 下与 Quarkus 对比，排除 GC 导致的尾延迟尖刺。

### 2.4 总结

| 维度 | 结论 |
|------|------|
| **吞吐** | 低、中并发下 Quarkus 领先（约 33%～51%）；高并发下两者接近。 |
| **延迟** | 中高并发下 Quarkus 平均延迟与尾延迟（P99、max）均优于 VXCore。 |
| **稳定性** | Quarkus 在高并发下 P99/max 更稳定，长尾更少。 |

在简单 GET /hello 场景下，Quarkus 整体性能优于 VXCore，尤其在 **c=100** 时差距最大；VXCore 通过上述方向优化，有望缩小与 Quarkus 的差距。

---

## 3. 复现命令

```bash
# 使用 127.0.0.1，避免 macOS 下 localhost 导致的 ab 连接错误

# VXCore
ab -n 10000 -c 10  http://127.0.0.1:18084/hello
ab -n 10000 -c 100 http://127.0.0.1:18084/hello
ab -n 10000 -c 200 http://127.0.0.1:18084/hello

# Quarkus
ab -n 10000 -c 10  http://127.0.0.1:8080/hello
ab -n 10000 -c 100 http://127.0.0.1:8080/hello
ab -n 10000 -c 200 http://127.0.0.1:8080/hello
```

---

## 4. 附录：原始 ab 输出（并发 100）

### VXCore (127.0.0.1:18084, -c 100)

```
Requests per second:    14005.48 [#/sec] (mean)
Time per request:       7.140 [ms] (mean)
Percentage of the requests served within a certain time (ms)
  50%      4   66%      5   75%      5   80%      5
  90%      7   95%     13   98%     80   99%     93  100%    171 (longest request)
```

### Quarkus (127.0.0.1:8080, -c 100)

```
Requests per second:    21140.76 [#/sec] (mean)
Time per request:       4.730 [ms] (mean)
Percentage of the requests served within a certain time (ms)
  50%      3   66%      4   75%      5   80%      6
  90%      8   95%     11   98%     15   99%     24  100%     41 (longest request)
```
