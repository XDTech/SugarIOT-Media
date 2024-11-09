package org.sugar.media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.sugar.media.service.ZlmApiService;

@SpringBootTest
class MediaApplicationTests {


    @Autowired
    private ZlmApiService zlmApiService;
    @Test
    void contextLoads() {

        this.zlmApiService.getApiList("http://192.168.1.6","JDG6XqSrvtzLTWw54xgQW8yEO6WjLrqO");
    }

}
