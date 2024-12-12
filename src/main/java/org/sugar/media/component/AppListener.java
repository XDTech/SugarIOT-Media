package org.sugar.media.component;

import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
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
    private SipCacheService sipCacheService;

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {


        // 节点列表写入缓存
        this.zlmNodeService.write2Cache();

        //  设备写入缓存
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setDeviceId("34020000001110000004");
        deviceBean.setPwd("smile100");
        deviceBean.setId(1L);
        deviceBean.setName("测试枪机");
        deviceBean.setTenantId(1L);

        this.sipCacheService.setSipDevice(deviceBean.getDeviceId(), deviceBean);

    }
}
