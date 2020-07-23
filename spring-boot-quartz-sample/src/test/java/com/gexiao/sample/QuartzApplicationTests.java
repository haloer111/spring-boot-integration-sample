package com.gexiao.sample;

import com.gexiao.sample.util.SpringContextUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

@SpringBootTest
class QuartzApplicationTests {

    @Test
    void contextLoads() {
        //获取对应的Bean
        Object object = SpringContextUtils.getBean("testService");
        try {
            //利用反射执行对应方法
            Method method = object.getClass().getMethod("test");
            method.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
