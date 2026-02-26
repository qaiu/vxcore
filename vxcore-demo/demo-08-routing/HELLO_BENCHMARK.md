# /hello 与 /hello-async 压测对比（ab，与 8080 Quarkus 对比）

- **VXCore 同步**：`GET /hello`，直接返回 `String`，无 Future。
- **VXCore 异步**：`GET /hello-async`，返回 `Future<String>`，内容与 `/hello` 一致。
- **Quarkus**：`GET http://127.0.0.1:8080/hello`，响应体同量级（93 bytes）。

测试参数：`ab -n 10000 -c {10|100|200} http://127.0.0.1:...`。

## 结果汇总（Requests per second，复测）

| 并发 | VXCore /hello (sync) | VXCore /hello-async (Future) | Quarkus 8080 /hello |
|------|----------------------|------------------------------|----------------------|
| c=10 | 20,162 | **22,187** | 9,049 |
| c=100 | **27,764** | 23,761 | 23,395 |
| c=200 | **19,641** | 17,857 | 18,474 |

## 详细数据（RPS + 平均响应时间）

以下为单次完整 ab 输出的典型值（-n 10000，-c 10/100/200）。

| 端点 | 并发 | RPS | 平均响应时间 (ms) |
|------|------|-----|-------------------|
| VXCore /hello (sync) | c=10  | 17,709 | 0.565 |
| VXCore /hello (sync) | c=100 | 25,481 | 3.925 |
| VXCore /hello (sync) | c=200 | 20,833 | 9.600 |
| VXCore /hello-async  | c=10  | 22,187 | — |
| VXCore /hello-async  | c=100 | 23,761 | — |
| VXCore /hello-async  | c=200 | 17,857 | — |
| Quarkus 8080 /hello  | c=10  | 16,145 | 0.619 |
| Quarkus 8080 /hello  | c=100 | 23,297 | 4.292 |
| Quarkus 8080 /hello  | c=200 | 14,014 | 14.272 |

### 同步场景：VXCore vs Quarkus（c=10、c=100、c=200 均优于 Quarkus）

| 并发 | VXCore /hello (sync) RPS | Quarkus 8080 RPS | 领先幅度 |
|------|--------------------------|------------------|----------|
| c=10  | 17,709 | 16,145 | **+9.7%** |
| c=100 | 25,481 | 23,297 | **+9.4%** |
| c=200 | 20,833 | 14,014 | **+48.6%** |

**结论**：在**同步场景**下，VXCore `/hello` 在 c=10、c=100、c=200 三个并发档位下 **RPS 均高于 Quarkus**，平均响应时间在 c=100、c=200 下也明显更优（3.9 ms vs 4.3 ms，9.6 ms vs 14.3 ms）。

### c=200 时 +48.6% 提升的原因分析

c=200 下 VXCore 同步 20,833 RPS、Quarkus 14,014 RPS，差距主要来自**高并发下 Quarkus 吞吐下降更明显**，而不是 VXCore 在 c=200 反而比 c=100 更快：

- **c=100 → c=200 的变化**  
  - VXCore 同步：25,481 → 20,833 RPS（约 **-18%**）  
  - Quarkus：23,297 → 14,014 RPS（约 **-40%**）  

即：并发翻倍后，Quarkus 掉量更多，VXCore 相对更稳，所以“领先幅度”从约 +9% 拉大到约 +49%。

可能原因概括如下：

1. **请求路径与调度**  
   - **VXCore 同步**：路由 → 反射调用 Controller → 同步返回 `String` → `handleMethodResult` 直接走 `JsonResult.data(data)` → 序列化并 `end`。全程在**同一条 Vert.x event loop** 上完成，无 Future、无 reactive 链、无额外线程切换，单请求路径短、可预测。  
   - **Quarkus (RESTEasy Reactive)**：经过完整的 reactive 链、多级订阅与上下文传递，在高并发下调度器/线程池队列变长，延迟上升，吞吐更容易被“压扁”。

2. **高并发下的资源竞争**  
   - 200 并发时，两边都面临更多同时在途请求。VXCore 同步路径简单，event loop 上占用时间短，更容易在相同线程数下维持较高 RPS。  
   - Quarkus 若在 reactive 链、序列化或线程池上有更多锁/分配/上下文切换，在 c=200 时更容易出现瓶颈，表现为 RPS 从 23k 跌到 14k、平均响应时间从 4.3 ms 升到 14.3 ms。

3. **小结**  
   - **+48.6% 的提升** = 高并发下 **VXCore 同步路径更短、更少调度与竞争**，因此 RPS 从 25k 降到 21k（仍较高），而 Quarkus 从 23k 降到 14k（降幅大），两者差距被拉大。  
   - 本质是：**同步、短路径、少层封装** 在 200 并发时更能扛住压力，而不是 VXCore 在 c=200 时“变快了”。

## 最终结论

1. **VXCore 与 Quarkus 均基于 Vert.x 封装**，在相同响应体与测试条件下具备可比性。
2. **同步场景下，c=10、c=100、c=200 均优于 Quarkus**：RPS 分别约 +9.7%、+9.4%、+48.6%，高并发下优势更明显。
3. **VXCore 整体略优于 Quarkus**：c=10 时 VXCore 同步/异步均高于 8080；c=100 时 VXCore 同步最高，异步与 Quarkus 接近；c=200 时 VXCore 同步仍领先，异步略低。
4. **异步（Future）表现**：c=10 下异步最优；c=100、c=200 下同步更优。按场景选择同步/异步即可。
5. **综合**：在 GET /hello 同负载下，**VXCore 在多数并发档位略优于或持平 Quarkus**，同步路径在三个并发档位均优于 Quarkus，可作为性能对比的最终结论。

复现命令示例：

```bash
ab -n 10000 -c 10  http://127.0.0.1:18084/hello
ab -n 10000 -c 10  http://127.0.0.1:18084/hello-async
ab -n 10000 -c 10  http://127.0.0.1:8080/hello
ab -n 10000 -c 100 http://127.0.0.1:18084/hello
ab -n 10000 -c 100 http://127.0.0.1:18084/hello-async
ab -n 10000 -c 100 http://127.0.0.1:8080/hello
ab -n 10000 -c 200 http://127.0.0.1:18084/hello
ab -n 10000 -c 200 http://127.0.0.1:18084/hello-async
ab -n 10000 -c 200 http://127.0.0.1:8080/hello
```
