package org.hongxi.whatsmars.dubbo.sample.consumer;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.rpc.service.GenericService;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 进程停止时报curator方面的错是正常的，需要await可参考 dubbo-spring-boot AwaitingNonWebApplicationListener
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String ZOOKEEPER_URL = "zookeeper://127.0.0.1:2181";

    public static void main(String[] args) {
        runWithBootstrap();
    }

    private static void runWithBootstrap() {
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setInterface(DemoService.class);
        reference.setGeneric("true");

        ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
        configCenterConfig.setAddress(ZOOKEEPER_URL);

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap
                .application(new ApplicationConfig("dubbo-demo-api-consumer"))
                .configCenter(configCenterConfig)
                .registry(new RegistryConfig(ZOOKEEPER_URL))
                .metadataReport(new MetadataReportConfig(ZOOKEEPER_URL))
                .protocol(new ProtocolConfig(CommonConstants.TRIPLE, -1))
                .reference(reference)
                .start();

        DemoService demoService = bootstrap.getCache().get(reference);
        String message = demoService.sayHello("lily");
        logger.info("sayHello returned: {}", message);

        // generic invoke
        GenericService genericService = (GenericService) demoService;
        Object genericInvokeResult = genericService.$invoke(
                "sayHello", new String[] {String.class.getName()}, new Object[] {"lily"});
        logger.info("generic invoke returned: {}", genericInvokeResult.toString());
    }
}