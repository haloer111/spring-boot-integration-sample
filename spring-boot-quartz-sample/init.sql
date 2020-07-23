-- 定时作业表
DROP TABLE IF EXISTS `schedule_job`;
CREATE TABLE `schedule_job`
(
    id              varchar(255) primary key,
    job_name        varchar(255) unique key comment '任务名称',
    cron_expression varchar(255) comment 'cron表达式',
    bean_name        varchar(255) comment 'bean名称',
    method_name        varchar(255) comment '方法名称',
    status          int(1) comment '状态 1.启动 2.暂停',
    delete_flag      tinyint(1) comment '是否删除 0.否 1.是',
    creator_id       int comment '创建人',
    creator_name       varchar(255) comment '创建人名称',
    created_time     datetime comment '创建时间',
    updated_time     datetime comment '更新时间'
)
    engine = InnoDB
    default charset = utf8mb4
    comment = '定时作业表';
