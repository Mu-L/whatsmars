package org.hongxi.whatsmars.dubbo.demo.consumer.runner;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 限流、降级规则
 */
@Component
public class SentinelRulesRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(DemoService.class.getName() + ":slowHello(java.lang.String)");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        flowRule.setCount(3);
        FlowRuleManager.loadRules(List.of(flowRule));

        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(DemoService.class.getName() + ":slowHello(java.lang.String)");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule.setCount(300);
        degradeRule.setTimeWindow(10);
        DegradeRuleManager.loadRules(List.of(degradeRule));
    }
}
