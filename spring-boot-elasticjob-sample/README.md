> 本文介绍 SpringBoot 整合 Elastic-Job 分布式调度任务（简单任务）。

Elastic-Job 是当当网开源的分布式任务调度解决方案，是业内使用较多的分布式调度解决方案。

![](https://dalaoyang-prod.oss-cn-beijing.aliyuncs.com/dalaoyang.cn/article/99/1)

这里主要介绍 Elastic-Job-Lite，Elastic-Job-Lite 定位为轻量级无中心化解决方案，使用 jar 包的形式提供最轻量级的分布式任务的协调服务，外部依赖仅 Zookeeper。

架构图如下：

![](https://dalaoyang-prod.oss-cn-beijing.aliyuncs.com/dalaoyang.cn/article/99/2)

## 前期准备

- 如果配置`JobEventConfiguration`，那么它只支持`mysql5.x`的版本
- 引入`spring-boot-starter-web`记得需要配置jdbc相关的连接属性

2.1 加入依赖
--------

新建项目，在项目中加入 Elastic-Job 依赖，完整 pom 如代码清单所示。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.dalaoyang</groupId>
    <artifactId>springboot2_elasticjob</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot2_elasticjob</name>
    <description>springboot2_elasticjob</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.dangdang</groupId>
            <artifactId>elastic-job-lite-core</artifactId>
            <version>${elastic-job.version}</version>
        </dependency>
        <dependency>
            <groupId>com.dangdang</groupId>
            <artifactId>elastic-job-lite-spring</artifactId>
            <version>${elastic-job.version}</version>
        </dependency>
        <!--使用job任务监控，依赖数据源的注入，所以需要引入jdbc包-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

2.2 配置文件
--------

配置文件中需要配置一下 zookeeper 地址和 namespace 名称

```properties
regCenter.serverList=localhost:2181
regCenter.namespace=springboot2_elasticjob
```

2.3 配置 zookeeper
----------------

接下来需要配置一下 zookeeper，创建一个` JobRegistryCenterConfig`，内容如下：

```java
@Configuration
@ConditionalOnExpression("'${regCenter.serverList}'.length() > 0")
public class JobRegistryCenterConfig {

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter(@Value("${regCenter.serverList}") final String serverList,
                                             @Value("${regCenter.namespace}") final String namespace) {
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
    }

}
```

2.4 定义 Elastic-Job 任务
---------------------

配置一个简单的任务，这里以在日志中打印一些参数为例，如下所示。

```java
public class MySimpleJob implements SimpleJob {
    Logger logger = LoggerFactory.getLogger(MySimpleJob.class);

    @Override
    public void execute(ShardingContext shardingContext) {
        logger.info(String.format("Thread ID: %s, 作业分片总数: %s, " +
                        "当前分片项: %s.当前参数: %s," +
                        "作业名称: %s.作业自定义参数: %s"
                ,
                Thread.currentThread().getId(),
                shardingContext.getShardingTotalCount(),
                shardingContext.getShardingItem(),
                shardingContext.getShardingParameter(),
                shardingContext.getJobName(),
                shardingContext.getJobParameter()
        ));

    }
}
```

2.5 配置任务
--------

配置任务的时候，这里定义了四个参数，分别是：

* `cron`：cron 表达式，用于控制作业触发时间。

* `shardingTotalCount`：作业分片总数

*   `shardingItemParameters`：分片序列号和参数用等号分隔，多个键值对用逗号分隔 。片序列号从 0 开始，不可大于或等于作业分片总数  如：`0=a,1=b,2=c`
    
*   `jobParameters`：作业自定义参数 作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业 
    
    例如：每次获取的数据量、作业实例从数据库读取的主键等。

本文配置如下：

```java
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
```

## 2.6配置作业事件

如果我们想要将作业运行的内容做操作，就需要用到`JobEventConfiguration`。目前只有一个实现的子类：`JobEventRdbConfiguration`，作用是将内容记录到DB中，配置如下：

```java
@Configuration
public class JobEventConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public JobEventConfiguration jobEventConfiguration() {
        return new JobEventRdbConfiguration(dataSource);
    }
}
```

启动项目，就可以看到控制台的输出了，如下所示：

![](https://dalaoyang-prod.oss-cn-beijing.aliyuncs.com/dalaoyang.cn/article/99/3)