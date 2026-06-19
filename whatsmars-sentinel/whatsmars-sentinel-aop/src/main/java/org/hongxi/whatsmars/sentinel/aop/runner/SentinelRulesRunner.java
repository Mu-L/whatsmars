package org.hongxi.whatsmars.sentinel.aop.runner;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SentinelRulesRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("hi");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(3);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(List.of(flowRule));
    }
}
