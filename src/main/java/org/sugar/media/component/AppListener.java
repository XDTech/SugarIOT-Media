package org.sugar.media.component;

import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sugar.media.service.onvif.OnvifDiscoveryService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.tenant.TenantService;
import org.sugar.media.sipserver.SipServer;


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


    @Resource
    private OnvifDiscoveryService onvifService;

    @Resource
    private TenantService tenantService;


    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {

        // 写入一个系统自带的租户和账户
        this.tenantService.createRoot();

        // 节点列表写入缓存
        this.zlmNodeService.write2Cache();

        //  sip设备写入缓存
        this.deviceService.write2cache();


        // 最后启动sip server

        this.sipServer.run();


//        this.onvifService.Discovery();

    }
}
