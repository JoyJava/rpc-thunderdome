package io.fh.thunderdome.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import io.fh.thunderdome.dubbo.services.DefaultDubboServiceImpl;
import io.fh.thunderdome.dubbo.services.DubboService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DubboServer {

  private static final Logger logger = LogManager.getLogger(DubboServer.class);

  public static void main(String... args) throws Exception {
    logger.info("starting server");
//    Hello obj = new HelloImpl(); // #1
//        new EmbeddedZooKeeper(2181, false).start();
//    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"spring/dubbo-demo-provider.xml"});
//    context.start();
//    System.in.read(); // press any key to exit
    System.setProperty("java.net.preferIPv4Stack", "true");
    ServiceConfig<DubboService> service = new ServiceConfig<>();
    service.setApplication(new ApplicationConfig("first-dubbo-provider"));
    service.setRegistry(new RegistryConfig("multicast://224.5.6.7:1234"));
    service.setInterface(DubboService.class);
//    service.setSerialization("protobuf");
    service.setRef(new DefaultDubboServiceImpl());
    service.export();
    logger.info("first-dubbo-provider is running.");
    System.in.read();
  }


}
