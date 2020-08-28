package com.gexiao.sample.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义简单任务
 *
 * @author gexiao
 */
@Slf4j
public class MySimpleJob implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("Thread ID: {}, 作业分片总数: {}, " +
                        "当前分片项: {}.当前参数: {}," +
                        "作业名称: {}.作业自定义参数: {}"
                ,
                Thread.currentThread().getId(),
                shardingContext.getShardingTotalCount(),
                shardingContext.getShardingItem(),
                shardingContext.getShardingParameter(),
                shardingContext.getJobName(),
                shardingContext.getJobParameter()
        );
    }
}
