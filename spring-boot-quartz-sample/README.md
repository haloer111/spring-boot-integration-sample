# 整合quartz
quartz有几个重要的概念。

- `Scheduler`：与调度程序交互的主要api

- `Job`：由希望由调度程序执行的组件实现的接口

- `JobDetail`：用于定义作业的实例

- `Trigger`：定义执行给作业的计划的组件

- `JobBuilder`：用于定义/构建`JobDetail`实例，用于定义作业的实例

- `TriggerBuilder`：用于定义/构建触发器的实例

## 与springboot2.x集成

1.添加maven依赖

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-quartz</artifactId>
        </dependency>
```

2.构建Job实体类，用于用户自定义任务的执行时间

```java
/**
 * 定时作业实体类
 */
@TableName("schedule_job")
@Data
public class ScheduleJob implements Serializable {

    private static final long serialVersionUID = 1L;

    // 任务状态：停止
    public static final int STATUS_STOP = 0;
    // 任务状态：启动
    public static final int STATUS_RUN = 1;

    @TableId
    private Integer id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 服务名称
     */
    private String beanName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 状态 1.启动 2.暂停
     */
    private int status;

    /**
     * 是否删除 0.否 1.是
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deleteFlag;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer creatorId;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String creatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
```

3.使用mybatis-plus构建出`ScheduleJob`的`mapper`层，`service`层，`controller`层。这里比较简单，笔者不在罗列。

4.在`ScheduleJobServiceImpl`创建startTask，pauseTask方法

```java
@Service
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobMapper, ScheduleJob> implements ScheduleJobService {
    public static final String SCHEDULE_JOB = "scheduleJob";
    @Autowired
    private Scheduler scheduler;

    @Override
    @Transactional
    public boolean startTask(Integer id) throws SchedulerException {
        ScheduleJob scheduleJob = Optional.ofNullable(getById(id)).orElseThrow(() -> new RuntimeException("定时任务不存在，id [{" + id + "}]"));
        // 创建定时任务
        addJob(scheduleJob);
        // 修改任务状态
        ScheduleJob updateJob = new ScheduleJob();
        updateJob.setUpdatedTime(LocalDateTime.now());
        updateJob.setId(id);
        updateJob.setStatus(ScheduleJob.STATUS_RUN);
        updateById(updateJob);
        return true;
    }

    @Override
    @Transactional
    public boolean pauseTask(Integer id) throws SchedulerException {
        ScheduleJob scheduleJob = Optional.ofNullable(getById(id)).orElseThrow(() -> new RuntimeException("定时任务不存在，id [{" + id + "}]"));
        // 删除任务
        deleteJob(scheduleJob);
        // 任务状态改成暂停
        ScheduleJob updateJob = new ScheduleJob();
        updateJob.setUpdatedTime(LocalDateTime.now());
        updateJob.setId(id);
        updateJob.setStatus(ScheduleJob.STATUS_STOP);
        updateById(updateJob);
        return true;
    }


    @Override
    public void addJob(ScheduleJob job) {
        try {
            // 创建触发器
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getId().toString())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();

            // 创建任务
            JobDetail jobDetail = JobBuilder.newJob(TaskJob.class)
                    .withIdentity(job.getId().toString())
                    .build();

            // 传入调度的数据，在QuartzFactory中需要使用
            jobDetail.getJobDataMap().put(SCHEDULE_JOB, job);

            // 调度作业
            scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteJob(ScheduleJob job) {
        try {
            JobKey jobKey = new JobKey(job.getId().toString());
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行的具体job任务
     */
    static class TaskJob extends QuartzJobBean {

        @Override
        protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            //获取调度数据
            ScheduleJob scheduleJob = (ScheduleJob) jobExecutionContext.getMergedJobDataMap().get(SCHEDULE_JOB);

            //获取对应的Bean
            Object object = SpringContextUtils.getBean(scheduleJob.getBeanName());
            try {
                //利用反射执行对应方法
                Method method = object.getClass().getMethod(scheduleJob.getMethodName());
                method.invoke(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

**思路**：一个schedule对应一个`Job`、`JobDetail`、`Trigger`。把这三者关系关联出来。当用于执行`startTask`方法时，就会立即触发。

4.创建监听器，启动程序的时候就会收集所以`STATUS_RUN`状态的任务来执行。

```java
@Component
public class AppReadListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ScheduleJobService scheduleJobService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        List<ScheduleJob> list = scheduleJobService.list(new LambdaQueryWrapper<ScheduleJob>().eq(ScheduleJob::getStatus, ScheduleJob.STATUS_RUN));
        if (list != null && list.size() > 0) {
            list.forEach(job -> {
                try {
                    scheduleJobService.startTask(job.getId());
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}
```

##测试

1.执行`init.sql`，初始化数据结构。

1.添加一条自定义任务，每三秒执行一次

```
POST localhost:8080/

{
    "id":1,
    "jobName": "test",
    "cronExpression": "0/3 * * * * ?",
    "beanName": "testService",
    "methodName": "test",
    "creatorId": 1,
    "creatorName": "str",
    "deleteFlag":0
}
```

2.启动任务，观察console，得到内容：`测试定时任务调用`

```
GET localhost:8080/start/task?id=1
```

3.暂停任务，观察console，没有内容输出，表示成功。

```
GET localhost:8080/create/task?id=1
```