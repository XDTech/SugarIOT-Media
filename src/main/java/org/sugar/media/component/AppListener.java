package org.sugar.media.component;

import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.sipserver.utils.SipCacheService;


/**
 * 程序预启动
 */
@Component
public class AppListener {


    @Resource
    private ZlmNodeService zlmNodeService;


    @Resource
    private DeviceService deviceService;



    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {


        // 节点列表写入缓存
        this.zlmNodeService.write2Cache();

        //  设备写入缓存
        this.deviceService.write2cache();

    }
}
