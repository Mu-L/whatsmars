package org.hongxi.whatsmars.dubbo.demo.provider;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SentinelRulesTest {

    @Test
    public void t() {
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

        String flowRules = JSON.toJSONString(List.of(flowRule, methodFlowRule), JSONWriter.Feature.PrettyFormat);

        System.out.println(flowRules);

        List<FlowRule> flowRules2 = JSON.parseObject(flowRules, new TypeReference<>() {});

        assert flowRules2.size() == 2;
    }
}
