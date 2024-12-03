package org.sugar.media.component;

import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sugar.media.service.node.ZlmNodeService;


/**
 * 程序预启动
 */
@Component
public class AppListener {


    @Resource
    private ZlmNodeService zlmNodeService;



    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {


        // 节点列表写入缓存
        this.zlmNodeService.write2Cache();

    }
}
