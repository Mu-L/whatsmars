package org.hongxi.whatsmars.job.job;

import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;
import org.hongxi.whatsmars.common.util.DateUtils;
import org.hongxi.whatsmars.job.entity.Foo;
import org.hongxi.whatsmars.job.repository.FooRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FooDataflowJob implements DataflowJob<Foo> {
    
    private static final Logger logger = LoggerFactory.getLogger(FooDataflowJob.class);

    @Autowired
    private FooRepository fooRepository;
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        logger.info("Item: {} | Time: {} | Thread: {} | DATAFLOW FETCH",
                shardingContext.getShardingItem(),
                LocalDateTime.now().format(DateUtils.TIME_FORMATTER),
                Thread.currentThread().getId());
        return fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        logger.info("Item: {} | Time: {} | Thread: {} | DATAFLOW PROCESS",
                shardingContext.getShardingItem(),
                LocalDateTime.now().format(DateUtils.TIME_FORMATTER),
                Thread.currentThread().getId());
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}
