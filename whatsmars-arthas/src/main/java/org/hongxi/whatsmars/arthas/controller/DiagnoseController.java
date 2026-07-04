package org.hongxi.whatsmars.arthas.controller;

import org.hongxi.whatsmars.arthas.service.DiagnoseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Arthas 诊断演示 Controller
 *
 * <p>启动应用后，通过浏览器访问以下接口触发业务逻辑，
 * 同时打开 Arthas 终端（http://localhost:8563）执行对应命令。
 *
 * <p>常用命令速查：
 * <pre>
 *   dashboard                          -- 实时系统面板（线程、内存、GC）
 *   thread                             -- 列出所有线程
 *   thread -n 3                        -- 列出 CPU 占用最高的 3 个线程
 *   thread &lt;threadId&gt;                  -- 打印指定线程堆栈
 *   sc *DiagnoseService                -- 搜索类
 *   sc -d org.hongxi.whatsmars.arthas.service.DiagnoseService
 *                                      -- 查看类详细信息（ClassLoader 等）
 *   sm org.hongxi.whatsmars.arthas.service.DiagnoseService
 *                                      -- 列出类的所有方法
 *   jad org.hongxi.whatsmars.arthas.service.DiagnoseService
 *                                      -- 反编译类
 *   watch ... findUser '{params,returnObj,throwExp}' -x 2
 *                                      -- 观察方法入参、返回值、异常
 *   trace ... processOrder             -- 追踪方法内部调用耗时
 *   trace ... processOrder '#cost &gt; 100'
 *                                      -- 只打印耗时 &gt; 100ms 的调用
 *   stack ... logAction                -- 打印 logAction 的调用来源堆栈
 *   ognl '@org.hongxi.whatsmars.arthas.service.DiagnoseService@echo("hello")'
 *                                      -- 调用静态方法
 *   ognl '#ctx=@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(@org.springframework.web.context.ContextLoader@getCurrentWebApplicationContext().getServletContext()), #ctx.getBean("diagnoseService").getClassInfo()'
 *                                      -- 通过 Spring 上下文调用 Bean
 * </pre>
 */
@RestController
@RequestMapping("/arthas")
public class DiagnoseController {

    private final DiagnoseService diagnoseService;

    public DiagnoseController(DiagnoseService diagnoseService) {
        this.diagnoseService = diagnoseService;
    }

    /**
     * watch 演示：观察 findUser 的入参、返回值和异常
     * <p>Arthas: watch ... findUser '{params,returnObj,throwExp}' -x 2
     */
    @GetMapping("/watch")
    public Map<String, Object> watch(@RequestParam(required = false) Long id) {
        return diagnoseService.findUser(id);
    }

    /**
     * trace 演示：追踪 processOrder 内部各步骤耗时
     * <p>Arthas: trace ... processOrder
     */
    @GetMapping("/trace")
    public String trace(@RequestParam(defaultValue = "1") Long orderId) {
        return diagnoseService.processOrder(orderId);
    }

    /**
     * stack 演示：查看 logAction 是被谁调用的
     * <p>Arthas: stack ... logAction
     */
    @GetMapping("/stack")
    public String stack(@RequestParam(defaultValue = "A") String from) {
        if ("B".equalsIgnoreCase(from)) {
            diagnoseService.triggerLogFromB();
        } else {
            diagnoseService.triggerLogFromA();
        }
        return "logged from " + from;
    }

    /**
     * thread 演示：触发 CPU 密集计算，用 thread -n 3 查看高 CPU 线程
     * <p>Arthas: thread -n 3
     */
    @GetMapping("/thread")
    public String thread(@RequestParam(defaultValue = "10000000") long iterations) {
        long result = diagnoseService.busyLoop(iterations);
        return "busyLoop done, result=" + result;
    }

    /**
     * sc / sm / jad 演示：查看 DiagnoseService 的类信息
     * <p>Arthas: sc -d ...DiagnoseService
     * <p>Arthas: sm ...DiagnoseService
     * <p>Arthas: jad ...DiagnoseService
     */
    @GetMapping("/classinfo")
    public String classInfo() {
        return diagnoseService.getClassInfo();
    }

    /**
     * ognl 演示：静态方法，可通过 ognl 命令直接调用
     * <p>Arthas: ognl '@...DiagnoseService@echo("hello arthas")'
     */
    @GetMapping("/ognl")
    public String ognl(@RequestParam(defaultValue = "hello") String message) {
        return DiagnoseService.echo(message);
    }

    /**
     * 综合演示：一次性触发多个方法调用，方便配合多个 Arthas 命令同时观察
     */
    @GetMapping("/all")
    public Map<String, Object> all(@RequestParam(defaultValue = "1") Long id) {
        diagnoseService.logAction("all-trigger");
        String order = diagnoseService.processOrder(id);
        Map<String, Object> user = diagnoseService.findUser(id);
        return Map.of(
                "user", user == null ? "not-found" : user,
                "order", order,
                "classInfo", diagnoseService.getClassInfo()
        );
    }
}
