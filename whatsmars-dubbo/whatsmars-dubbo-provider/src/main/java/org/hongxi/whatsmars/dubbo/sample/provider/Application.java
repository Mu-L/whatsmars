package org.hongxi.whatsmars.dubbo.sample.provider;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;

public class Application {

    private static final String NACOS_URL = "nacos://127.0.0.1:8848?username=nacos&password=7fDJZBbiLzO2";

    public static void main(String[] args) {
        startWithBootstrap();
    }

    private static void startWithBootstrap() {
        ServiceConfig<DemoService> service = new ServiceConfig<>();
        service.setInterface(DemoService.class);
        service.setRef(new DemoServiceImpl());

        ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
        configCenterConfig.setAddress(NACOS_URL);

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(new ApplicationConfig("dubbo-demo-provider"))
                .configCenter(configCenterConfig)
                .registry(new RegistryConfig(NACOS_URL))
                .metadataReport(new MetadataReportConfig(NACOS_URL))
                .protocol(new ProtocolConfig(CommonConstants.TRIPLE, -1))
                .service(service)
                .start()
                .await();
    }
}