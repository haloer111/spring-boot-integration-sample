package com.gexiao.sample.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.gexiao.sample.job.MySimpleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务配置类
 *
 * @author gexiao
 */
@Configuration
public class MyJobConfig {

    @Autowired
    private ZookeeperRegistryCenter regCenter;

    @Autowired
    private JobEventConfiguration jobEventConfiguration;

    @Bean
    public SimpleJob mySimpleJob() {
        return new MySimpleJob();
    }


    @Bean(initMethod = "init")
    public JobScheduler simpleJobScheduler(@Value("${elastic.job.cron}")
                                                   String cron,
                                           @Value("${elastic.job.shardingTotalCount}")
                                                   int shardingTotalCount,
                                           @Value("${elastic.job.shardingItemParameters}")
                                                   String shardingItemParameters,
                                           @Value("${elastic.job.jobParameters}")
                                                   String jobParameters) {
        Class<? extends SimpleJob> jobClass = mySimpleJob().getClass();
        // 定义任务的核心配置
        JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder(jobClass.getName(), cron, shardingTotalCount).
                shardingItemParameters(shardingItemParameters).jobParameter(jobParameters).build();
        // 定义simpleJob的配置
        SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(simpleCoreConfig, jobClass.getCanonicalName());
        // 定义作业配置
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfiguration)
                .overwrite(false)
                .build();
        // spring的任务调度
        return new SpringJobScheduler(
                mySimpleJob(),
                regCenter,
                liteJobConfiguration,
                jobEventConfiguration);
    }


}
