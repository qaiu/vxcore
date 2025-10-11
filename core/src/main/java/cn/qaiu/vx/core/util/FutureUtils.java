package cn.qaiu.vx.core.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.concurrent.ExecutionException;

/**
 * Future工具类
 * 提供Future和Promise的同步获取方法
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class FutureUtils {

   /**
    * 同步获取Future的结果
    * 注意：此方法会阻塞当前线程，仅在测试或特殊场景下使用
    * 
    * @param future 要获取结果的Future
    * @param <T> 结果类型
    * @return Future的结果
    * @throws RuntimeException 如果获取结果时发生异常
    */
   public static <T> T getResult(Future<T> future) {
       try {
           return future.toCompletionStage().toCompletableFuture().get();
       } catch (InterruptedException | ExecutionException e) {
           throw new RuntimeException(e);
       }
   }
   /**
    * 同步获取Promise的结果
    * 注意：此方法会阻塞当前线程，仅在测试或特殊场景下使用
    * 
    * @param promise 要获取结果的Promise
    * @param <T> 结果类型
    * @return Promise的结果
    */
    public static <T> T getResult(Promise<T> promise) {
       return promise.future().toCompletionStage().toCompletableFuture().join();
    }
}
