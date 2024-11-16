package org.sugar.media;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.NodeService;

@SpringBootTest
class MediaApplicationTests {


    @Autowired
    private ZlmApiService zlmApiService;

    @Resource
    private NodeService nodeService;
    @Test
    void contextLoads() {

        String test = SecureUtil.sha1("test");
        StaticLog.info("{}",test);


        this.zlmApiService.getApiList("http://192.168.1.6","JDG6XqSrvtzLTWw54xgQW8yEO6WjLrqO");
    }


    @Test
    void setZlmConfig(){
        NodeModel node = this.nodeService.getNode(1307362892365955072L);

        boolean b = this.zlmApiService.syncZlmConfig(node);

        StaticLog.info("{}",b);


    }


}
