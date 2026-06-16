package org.hongxi.whatsmars.dubbo.demo.provider.runner;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * QPS 限流
 */
@Component
public class SentinelRulesRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(DemoService.class.getName());
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1000);
        flowRule.setLimitApp("default");

        FlowRule methodFlowRule = new FlowRule();
        methodFlowRule.setResource(DemoService.class.getName() + ":sayHello(java.lang.String)");
        methodFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        methodFlowRule.setCount(10);
        methodFlowRule.setLimitApp("dubbo-demo-consumer");
        FlowRuleManager.loadRules(List.of(flowRule, methodFlowRule));
    }
}
