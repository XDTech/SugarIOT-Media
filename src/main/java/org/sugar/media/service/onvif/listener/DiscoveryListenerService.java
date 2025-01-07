package org.sugar.media.service.onvif.listener;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.*;
import be.teletask.onvif.models.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.service.onvif.OnvifManagerService;

import java.util.List;

/**
 * Date:2025/01/07 13:05:42
 * Author：Tobin
 * Description:
 */


@Service
@Slf4j
public class DiscoveryListenerService implements DiscoveryListener {


    @Resource
    private OnvifManagerService onvifManagerService;


    public DiscoveryListenerService() {

    }

    @Override
    public void onDiscoveryStarted() {


    }


    @Override
    public void onDevicesFound(List<Device> list) {

        log.warn("{}", list.size());
        for (Device device : list) {
            log.warn("host:{},pwd:{},type:{},username:{}", device.getHostName(), device.getPassword(), device.getType(), device.getUsername());


//

            OnvifDevice onvifDevice = new OnvifDevice(device.getHostName(), "admin", "Aa12345678");

            this.onvifManagerService.getDeviceInfo(onvifDevice);


            log.error("end");
        }
    }
}
