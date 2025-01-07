package org.sugar.media.service.onvif;

import be.teletask.onvif.DiscoveryManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.service.onvif.listener.DiscoveryListenerService;

/**
 * Date:2025/01/07 12:58:19
 * Author：Tobin
 * Description:
 */

@Service
@Slf4j
public class OnvifDiscoveryService {


    @Resource
    private DiscoveryListenerService discoveryListenerService;
    private final DiscoveryManager manager = new DiscoveryManager();


    public OnvifDiscoveryService() {
        manager.setDiscoveryTimeout(1000);
    }


    /**
     * 设备发现
     */
    public void Discovery() {

        manager.discover(discoveryListenerService);

    }


}
