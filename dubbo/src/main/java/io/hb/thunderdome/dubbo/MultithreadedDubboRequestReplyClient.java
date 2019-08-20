package io.hb.thunderdome.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import io.hb.thunderdome.dubbo.services.DubboService;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import org.HdrHistogram.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class MultithreadedDubboRequestReplyClient {

  private static final Logger logger = LogManager.getLogger(MultithreadedDubboRequestReplyClient.class);

  public static void main(String... args) throws Exception {
    String host = System.getProperty("host", "127.0.0.1");
    int port = Integer.getInteger("port", 8001);
    int count = Integer.getInteger("count", 1_000_000);
    int concurrency = Integer.getInteger("concurrency", 64);
    int threads = Integer.getInteger("threads", 3);
    boolean useEpoll = Boolean.getBoolean("usePoll");

    System.setProperty("java.net.preferIPv4Stack", "true");
    ReferenceConfig<DubboService> reference = new ReferenceConfig<>();
    reference.setApplication(new ApplicationConfig("first-dubbo-consumer"));
    reference.setRegistry(new RegistryConfig("multicast://224.5.6.7:1234"));
    reference.setInterface(DubboService.class);
    DubboService simpleService = reference.get();

    ThreadFactory tf = new DefaultThreadFactory("client-elg-", true);
    NioEventLoopGroup worker = new NioEventLoopGroup(0, tf);

    Histogram histogram = new Histogram(3600000000000L, 3);
    logger.info("starting test - sending {}", count);
    CountDownLatch latch = new CountDownLatch(threads);
    long start = System.nanoTime();
    Flux.range(1, threads)
        .flatMap(i -> {
          logger.info("start thread -> {}" + i);

          return Mono.fromRunnable(() -> {
            int i1 = count / threads;
            try {
              latch.countDown();
              latch.await();
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            execute(i1, simpleService, histogram, concurrency);
          }).subscribeOn(Schedulers.elastic());
        })
        .blockLast();

    histogram.outputPercentileDistribution(System.out, 1000.0d);
    double completedMillis = (System.nanoTime() - start) / 1_000_000d;
    double rps = count / ((System.nanoTime() - start) / 1_000_000_000d);
    logger.info("test complete in {} ms", completedMillis);
    logger.info("test rps {}", rps);
  }

  private static void execute(int count, DubboService simpleService, Histogram histogram, int concurrency) {
//    System.out.println(simpleService.requestReply("a").toString());
    Flux.range(1, count).flatMap(integer -> {
          long s = System.nanoTime();
          final String reply = simpleService.requestReply("hello");
//          System.out.println(reply.toString());
          return Flux.just(reply).doOnComplete(() -> {
            histogram.recordValue(System.nanoTime() - s);
          });
        },
        concurrency)
        .blockLast();
  }
}
