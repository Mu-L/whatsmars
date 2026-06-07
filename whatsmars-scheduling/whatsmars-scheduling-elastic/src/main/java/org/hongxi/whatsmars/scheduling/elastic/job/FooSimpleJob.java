package org.hongxi.whatsmars.scheduling.elastic.job;

import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;
import org.hongxi.whatsmars.common.util.DateUtils;
import org.hongxi.whatsmars.scheduling.elastic.entity.Foo;
import org.hongxi.whatsmars.scheduling.elastic.repository.FooRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FooSimpleJob implements SimpleJob {
    
    private static final Logger logger = LoggerFactory.getLogger(FooSimpleJob.class);
    
    @Autowired
    private FooRepository fooRepository;
    
    @Override
    public void execute(ShardingContext shardingContext) {
        logger.info("Item: {} | Time: {} | Thread: {} | SIMPLE",
                shardingContext.getShardingItem(),
                LocalDateTime.now().format(DateUtils.TIME_FORMATTER),
                Thread.currentThread().getId());
        List<Foo> data = fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}