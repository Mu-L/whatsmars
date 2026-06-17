package org.hongxi.whatsmars.dubbo.demo.consumer;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
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
        flowRule.setResource(DemoService.class.getName() + ":slowHello(java.lang.String)");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        flowRule.setCount(3);

        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(DemoService.class.getName() + ":slowHello(java.lang.String)");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule.setCount(300);
        degradeRule.setTimeWindow(10);

        String flowRules = JSON.toJSONString(List.of(flowRule), JSONWriter.Feature.PrettyFormat);
        String degradeRules = JSON.toJSONString(List.of(degradeRule), JSONWriter.Feature.PrettyFormat);

        System.out.println(flowRules);
        System.out.println(degradeRules);

        List<FlowRule> flowRules2 = JSON.parseObject(flowRules, new TypeReference<>() {});
        List<DegradeRule> degradeRules2 = JSON.parseObject(degradeRules, new TypeReference<>() {});

        assert flowRules2.size() == 1 && degradeRules2.size() == 1;
    }
}
