package com.gexiao.sample.service.impl;

import com.gexiao.sample.service.TestService;
import org.springframework.stereotype.Service;

@Service("testService")
public class TestServiceImpl implements TestService {
    @Override
    public void test() {
        System.out.println("测试定时任务调用");
    }
}
