package com.gexiao.sample.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gexiao.sample.entity.ScheduleJob;
import com.gexiao.sample.service.ScheduleJobService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleController {

    @Autowired
    private ScheduleJobService service;

    /**
     * 新增或编辑
     */
    @PostMapping
    public Result save(@RequestBody ScheduleJob scheduleJob) {
        return Result.ok(service.save(scheduleJob));
    }

    /**
     * 新增或编辑
     */
    @PutMapping
    public Result update(@RequestBody ScheduleJob scheduleJob) {
        int count = service.count(new QueryWrapper<ScheduleJob>().eq("id", scheduleJob.getId()));
        if (count == 0) {
            return Result.fail("没有找到对应的定时任务，id [{" + scheduleJob.getId() + "}]");
        }
        service.updateById(scheduleJob);
        return Result.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result delete(int id) {
        int count = service.count(new QueryWrapper<ScheduleJob>().eq("id", id));
        if (count == 0) {
            return Result.fail("没有找到对应的定时任务，id [{" + id + "}]");
        }
        return Result.ok(service.removeById(id));
    }

    /**
     * 查询
     */
    @GetMapping
    public Result find(int id) {
        ScheduleJob scheduleJob = service.getById(id);
        return Result.ok(scheduleJob);

    }

    /**
     * 分页查询
     */
    @PostMapping("/list")
    public Result list() {
        return Result.ok(service.list());
    }

    /**
     * 暂停任务
     */
    @GetMapping("/pause/task")
    public Result pauseTask(Integer id) throws SchedulerException {
        return Result.ok(service.pauseTask(id));
    }
    /**
     * 启动任务
     */
    @GetMapping("/start/task")
    public Result startTask(Integer id) throws SchedulerException {
        return Result.ok(service.startTask(id));
    }
}
