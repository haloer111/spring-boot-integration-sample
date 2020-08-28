package com.gexiao.sample.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * elastic-job的注册中心
 *
 * @author gexiao
 */
@Configuration
@ConditionalOnExpression(value = "'${regCenter.serverList}'.length()>0")
public class JobRegistryCenterConfig {

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter(@Value("${regCenter.serverList}") String serverList,
                                             @Value("${regCenter.namespace}") String namespace) {
        ZookeeperConfiguration configuration = new ZookeeperConfiguration(serverList, namespace);
        return new ZookeeperRegistryCenter(configuration);
    }
}
