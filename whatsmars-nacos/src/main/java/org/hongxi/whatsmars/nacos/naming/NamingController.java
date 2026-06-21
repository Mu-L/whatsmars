package org.hongxi.whatsmars.nacos.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.hongxi.whatsmars.common.result.Result;
import org.hongxi.whatsmars.common.result.ResultHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos Naming Service Controller
 * 提供 Nacos 服务注册与发现的 REST API
 */
@RestController
@RequestMapping("/nacos/naming")
public class NamingController {

    private static final Logger log = LoggerFactory.getLogger(NamingController.class);

    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private static final String DEFAULT_CLUSTER = "DEFAULT";

    @Autowired
    private NamingService namingService;

    // 用于存储监听器，key: serviceName_groupName, value: EventListener
    private final ConcurrentMap<String, EventListener> listenerMap = new ConcurrentHashMap<>();

    // ==================== 服务注册相关接口 ====================

    /**
     * 注册服务实例（简单方式）
     */
    @PostMapping("/register/simple")
    public Result<Void> registerSimple(@RequestParam String serviceName,
                                 @RequestParam String ip,
                                 @RequestParam int port,
                                 @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            namingService.registerInstance(serviceName, groupName, ip, port);
            log.info("服务注册成功: {} ({}), {}:{}", serviceName, groupName, ip, port);
            return ResultHelper.newResult(true, "服务注册成功");
        } catch (NacosException e) {
            log.error("服务注册失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 注册服务实例（完整方式）
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestParam String serviceName,
                                         @RequestParam String ip,
                                         @RequestParam int port,
                                         @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName,
                                         @RequestParam(required = false, defaultValue = DEFAULT_CLUSTER) String clusterName,
                                         @RequestParam(required = false) Double weight,
                                         @RequestParam(required = false) Boolean healthy,
                                         @RequestParam(required = false) Map<String, String> metadata) {
        try {
            Instance instance = createInstance(ip, port, weight, healthy, clusterName, metadata);
            namingService.registerInstance(serviceName, groupName, instance);
            
            log.info("服务注册成功: {} ({}), {}:{}, cluster: {}", 
                serviceName, groupName, ip, port, clusterName);

            return ResultHelper.newResult(true, "服务注册成功");
        } catch (NacosException e) {
            log.error("服务注册失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    // ==================== 服务注销相关接口 ====================

    /**
     * 注销服务实例
     */
    @DeleteMapping("/deregister")
    public Result<Void> deregister(@RequestParam String serviceName,
                                   @RequestParam String ip,
                                   @RequestParam int port,
                                   @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName,
                                   @RequestParam(required = false, defaultValue = DEFAULT_CLUSTER) String clusterName) {
        try {
            namingService.deregisterInstance(serviceName, groupName, ip, port, clusterName);
            log.info("服务注销成功: {} ({}), {}:{}", serviceName, groupName, ip, port);
            return ResultHelper.newResult(true, "服务注销成功");
        } catch (NacosException e) {
            log.error("服务注销失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    // ==================== 服务发现相关接口 ====================

    /**
     * 获取所有实例
     */
    @GetMapping("/instances/all")
    public Result<List<Instance>> getAllInstances(@RequestParam String serviceName,
                                                @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName, groupName);
            log.info("获取服务 {} ({}) 的所有实例，共 {} 个", serviceName, groupName, instances.size());
            return ResultHelper.newSuccessResult(instances);
        } catch (NacosException e) {
            log.error("获取服务实例失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 获取健康实例列表
     */
    @GetMapping("/instances/healthy")
    public Result<List<Instance>> getHealthyInstances(@RequestParam String serviceName,
                                                       @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
            log.info("获取服务 {} ({}) 的健康实例，共 {} 个", serviceName, groupName, instances.size());
            return ResultHelper.newSuccessResult(instances);
        } catch (NacosException e) {
            log.error("获取健康实例失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 获取不健康实例列表
     */
    @GetMapping("/instances/unhealthy")
    public Result<List<Instance>> getUnhealthyInstances(@RequestParam String serviceName,
                                                         @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceName, groupName, false);
            log.info("获取服务 {} ({}) 的不健康实例，共 {} 个", serviceName, groupName, instances.size());
            return ResultHelper.newSuccessResult(instances);
        } catch (NacosException e) {
            log.error("获取不健康实例失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 获取指定集群的健康实例
     */
    @GetMapping("/instances/cluster")
    public Result<List<Instance>> getClusterInstances(@RequestParam String serviceName,
                                                       @RequestParam String clusterName,
                                                       @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            List<String> clusters = Collections.singletonList(clusterName);
            List<Instance> instances = namingService.selectInstances(serviceName, groupName, clusters, true);
            log.info("获取服务 {} ({}) 在集群 {} 的健康实例，共 {} 个", serviceName, groupName, clusterName, instances.size());
            return ResultHelper.newSuccessResult(instances);
        } catch (NacosException e) {
            log.error("获取集群实例失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 随机选择一个健康实例（负载均衡）
     */
    @GetMapping("/instances/one")
    public Result<Instance> selectOneHealthyInstance(@RequestParam String serviceName,
                                                      @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            Instance instance = namingService.selectOneHealthyInstance(serviceName, groupName);
            if (instance != null) {
                log.info("选中服务 {} ({}) 的实例: {}:{}", serviceName, groupName, instance.getIp(), instance.getPort());
                return ResultHelper.newSuccessResult(instance);
            } else {
                return ResultHelper.newErrorResult(404, "未找到健康实例");
            }
        } catch (NacosException e) {
            log.error("选择实例失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    // ==================== 服务监听相关接口 ====================

    /**
     * 订阅服务变化
     */
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestParam String serviceName,
                                  @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            String key = serviceName + "_" + groupName;
            
            // 如果已存在监听器，先取消订阅
            if (listenerMap.containsKey(key)) {
                unsubscribe(serviceName, groupName);
            }
            
            EventListener listener = event -> {
                log.info("收到服务变化事件: {}", event.getClass().getSimpleName());
                if (event instanceof NamingEvent namingEvent) {
                    log.info("服务名: {}, 实例数量: {}",
                        namingEvent.getServiceName(), namingEvent.getInstances().size());
                    namingEvent.getInstances().forEach(inst -> 
                        log.info("  - {}:{}, healthy={}", inst.getIp(), inst.getPort(), inst.isHealthy())
                    );
                }
            };
            
            namingService.subscribe(serviceName, groupName, listener);
            listenerMap.put(key, listener);
            
            log.info("成功订阅服务: {} ({})", serviceName, groupName);
            return ResultHelper.newResult(true, "订阅成功");
        } catch (NacosException e) {
            log.error("订阅服务失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 取消订阅服务
     */
    @DeleteMapping("/unsubscribe")
    public Result<Void> unsubscribe(@RequestParam String serviceName,
                                    @RequestParam(required = false, defaultValue = DEFAULT_GROUP) String groupName) {
        try {
            String key = serviceName + "_" + groupName;
            EventListener listener = listenerMap.remove(key);
            
            if (listener != null) {
                namingService.unsubscribe(serviceName, groupName, listener);
                log.info("取消订阅服务: {} ({})", serviceName, groupName);
                return ResultHelper.newResult(true, "取消订阅成功");
            } else {
                return ResultHelper.newErrorResult(404, "未找到订阅记录");
            }
        } catch (NacosException e) {
            log.error("取消订阅失败", e);
            return ResultHelper.newErrorResult(e.getErrCode(), e.getErrMsg());
        }
    }

    /**
     * 创建 Instance 对象的辅助方法
     */
    private Instance createInstance(String ip, int port, Double weight,
                                    Boolean healthy, String clusterName, Map<String, String> metadata) {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(weight != null ? weight : 1.0);
        instance.setHealthy(healthy != null ? healthy : true);
        instance.setEnabled(true);
        instance.setClusterName(clusterName != null ? clusterName : DEFAULT_CLUSTER);

        if (metadata != null && !metadata.isEmpty()) {
            metadata.forEach(instance::addMetadata);
        }

        return instance;
    }
}
