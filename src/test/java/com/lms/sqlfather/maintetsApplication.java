package com.lms.sqlfather;

import com.lms.sqlfather.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class maintetsApplication {

    @Resource
    UserService userService;
    @Test
    public void test(){
        userService.userRegister("zhangsan22","zhangsan12333","12345678","12345678","xxx@163.com","user");

    }
}
