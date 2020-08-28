package com.gexiao.sample;

import com.gexiao.sample.entity.Order;
import com.gexiao.sample.producer.RabbitSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringBootRabbitmqSampleApplicationTests {

    @Autowired
    private RabbitSender rabbitSender;

    @Test
    public void testSender1() throws Exception {
        Map<String ,Object> map = new HashMap<>();
        map.put("number","12345");
        map.put("send_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        rabbitSender.send("你好,springboot!!gexiao",map);
    }

    @Test
    public void testSendOrder() throws Exception {
        Order order = new Order("12334","测试01","这个测试发送实体对象");
        rabbitSender.send(order);
    }
}
