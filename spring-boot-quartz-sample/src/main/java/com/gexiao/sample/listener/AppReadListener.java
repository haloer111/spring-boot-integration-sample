package com.gexiao.sample.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexiao.sample.entity.ScheduleJob;
import com.gexiao.sample.service.ScheduleJobService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * spring mvc启动完毕后触发的监听事件
 */
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
