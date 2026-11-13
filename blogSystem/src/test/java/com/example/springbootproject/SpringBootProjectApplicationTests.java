package com.example.springbootproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "aliyun.oss.access-key-id=test",
        "aliyun.oss.access-key-secret=test",
        "spring.mail.username=test@qq.com",
        "spring.mail.password=test"
})
class SpringBootProjectApplicationTests {

    @Test
    void contextLoads() {
    }

}
