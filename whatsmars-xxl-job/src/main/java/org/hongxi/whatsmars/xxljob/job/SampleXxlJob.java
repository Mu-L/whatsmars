package org.hongxi.whatsmars.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XXL-JOB 示例任务 Handler，演示常用场景。
 */
@Component
public class SampleXxlJob {

    private static final Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 简单任务：Bean模式（推荐）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() {
        XxlJobHelper.log("XXL-JOB, Hello World.");
        logger.info("demoJobHandler executed at {}", LocalDateTime.now().format(FORMATTER));

        // 可通过 XxlJobHelper.getJobParam() 获取调度中心配置的参数
        String param = XxlJobHelper.getJobParam();
        if (param != null && !param.isEmpty()) {
            XxlJobHelper.log("job param: {}", param);
        }

        XxlJobHelper.handleSuccess("执行成功");
    }

    /**
     * 分片广播任务：所有执行器并行处理，每个执行器处理各自分片
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片参数: 当前分片={}, 总分片={}", shardIndex, shardTotal);
        logger.info("shardingJobHandler: shardIndex={}, shardTotal={}", shardIndex, shardTotal);

        // 业务逻辑：按分片处理数据，例如按 ID % shardTotal == shardIndex 过滤
        XxlJobHelper.handleSuccess("分片" + shardIndex + "执行成功");
    }

    /**
     * 带参数的任务：从调度中心传入的参数执行不同逻辑
     */
    @XxlJob("paramJobHandler")
    public void paramJobHandler() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("received param: {}", param);
        logger.info("paramJobHandler, param={}", param);

        if ("fail".equals(param)) {
            XxlJobHelper.handleFail("参数为 fail，任务失败");
            return;
        }
        XxlJobHelper.handleSuccess("参数: " + param);
    }

    /**
     * 生命周期任务：演示 init / destroy 回调
     */
    @XxlJob(value = "lifecycleJobHandler", init = "init", destroy = "destroy")
    public void lifecycleJobHandler() {
        XxlJobHelper.log("lifecycleJobHandler executed");
    }

    public void init() {
        logger.info("lifecycleJobHandler init");
    }

    public void destroy() {
        logger.info("lifecycleJobHandler destroy");
    }
}
