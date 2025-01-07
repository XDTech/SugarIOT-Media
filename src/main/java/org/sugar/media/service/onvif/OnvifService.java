package org.sugar.media.service.onvif;

import be.teletask.onvif.DiscoveryManager;
import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.DiscoveryListener;
import be.teletask.onvif.listeners.OnvifDeviceInformationListener;
import be.teletask.onvif.models.Device;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifDeviceInformation;
import cn.hutool.core.lang.Console;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sugar.media.service.onvif.DiscoveryListenerService;
import org.sugar.media.service.onvif.OnvifResponseService;

import java.util.List;

/**
 * Date:2025/01/07 12:58:19
 * Author：Tobin
 * Description:
 */

@Service
@Slf4j
public class OnvifService {


    @Resource
    private DiscoveryListenerService discoveryListenerService;
    private final DiscoveryManager manager = new DiscoveryManager();


    public OnvifService() {
        manager.setDiscoveryTimeout(1000);
    }

    public void getManager() {

        manager.discover(discoveryListenerService);

    }


}
