package com.gexiao.sample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gexiao.sample.dao.ScheduleJobMapper;
import com.gexiao.sample.entity.ScheduleJob;
import com.gexiao.sample.service.ScheduleJobService;
import com.gexiao.sample.util.SpringContextUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

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
