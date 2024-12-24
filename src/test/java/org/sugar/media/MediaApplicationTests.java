package org.sugar.media;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.UriComponentsBuilder;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.node.NodeService;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MediaApplicationTests {





    @Resource
    private NodeService nodeService;


    @Resource
    private ChannelService channelService;

    @Resource
    private MediaCacheService mediaCacheService;

    @Test
    void contextLoads() {


        this.channelService.checkChannelCode("100001","10000100001320000008");

//        String test = SecureUtil.sha1("test");
//        StaticLog.info("{}", test);


        //  this.zlmApiService.getApiList("http://192.168.1.6", "JDG6XqSrvtzLTWw54xgQW8yEO6WjLrqO");
    }


    @Test
    void setZlmConfig() {
//        NodeModel node = this.nodeService.getNode(1307362892365955072L);
//
//        boolean b = this.zlmApiService.syncZlmConfig(node);
//
//        StaticLog.info("{}", b);


        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost/index/api/addStreamProxy");

        // add param
        builder.queryParam("secret", "__defaultVhost__");
        builder.queryParam("vhost", true);
        builder.queryParam("app", true);
        builder.queryParam("stream", true);
        StaticLog.info("{}", builder.toUriString());
    }

    @Test
    void redisTest() {

        TimeInterval timer = DateUtil.timer();

        List<Long> onlineList = this.mediaCacheService.getOnlineList();
        List<NodeModel> nodeAll = this.nodeService.getNodeAll(onlineList);
        List<String> collect = nodeAll.stream().map(NodeModel::getIp).toList();

        System.out.println("A耗时：" + timer.intervalRestart());

        List<NodeModel> nodeAll1 = this.nodeService.getNodeAll();

        List<String> list = nodeAll1.stream()
                .filter(nodeModel -> this.mediaCacheService.isOnline(nodeModel.getId()))  // 只保留在线的节点
                .map(NodeModel::getIp)  // 提取 IP 地址
                .toList();  // 收集成列表

        System.out.println("B耗时：" + timer.intervalRestart());

        return;
//        String mediaStatus = this.mediaCacheService.getMediaStatus(1L);
//        StaticLog.info("{}", mediaStatus);
//
//        if (StrUtil.isBlank(mediaStatus)) {
//            StaticLog.info("{}", mediaStatus);
//        }

    }


}
