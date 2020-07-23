package com.gexiao.sample.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gexiao.sample.entity.ScheduleJob;
import org.quartz.SchedulerException;

public interface ScheduleJobService extends IService<ScheduleJob> {

    /**
     * 开始任务
     */
    boolean startTask(Integer id) throws SchedulerException;

    /**
     * 暂停任务
     */
    boolean pauseTask(Integer id) throws SchedulerException;



    /**
     * 增加定时作业任务
     * @param job 定时作业任务
     */
    void addJob(ScheduleJob job);

    /**
     * 删除定时作业任务
     * @param job
     */
    void deleteJob(ScheduleJob job);
}
