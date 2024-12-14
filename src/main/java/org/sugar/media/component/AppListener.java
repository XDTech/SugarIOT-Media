package org.sugar.media.component;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.sipserver.SipServer;
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

    @Resource
    private SipServer sipServer;




    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {



        // 节点列表写入缓存
        this.zlmNodeService.write2Cache();

        //  sip设备写入缓存
        this.deviceService.write2cache();


        // 最后启动sip server

        this.sipServer.run();


    }
}
