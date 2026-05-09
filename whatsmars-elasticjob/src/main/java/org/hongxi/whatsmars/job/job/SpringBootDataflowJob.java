package org.hongxi.whatsmars.job.job;

import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;
import org.hongxi.whatsmars.job.entity.Foo;
import org.hongxi.whatsmars.job.repository.FooRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class SpringBootDataflowJob implements DataflowJob<Foo> {
    
    private final Logger logger = LoggerFactory.getLogger(SpringBootDataflowJob.class);

    @Autowired
    private FooRepository fooRepository;
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        logger.info("Item: {} | Time: {} | Thread: {} | {}",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "DATAFLOW FETCH");
        return fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        logger.info("Item: {} | Time: {} | Thread: {} | {}",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "DATAFLOW PROCESS");
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}
