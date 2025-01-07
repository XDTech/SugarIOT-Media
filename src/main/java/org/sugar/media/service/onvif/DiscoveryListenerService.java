package org.sugar.media.service.onvif;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.*;
import be.teletask.onvif.models.*;
import be.teletask.onvif.responses.OnvifResponse;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Date:2025/01/07 13:05:42
 * Author：Tobin
 * Description:
 */


@Service
@Slf4j
public class DiscoveryListenerService implements DiscoveryListener {


    @Resource
    private OnvifResponseService onvifResponseService;

    OnvifManager onvifManager = new OnvifManager(onvifResponseService);

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
            onvifManager.getDeviceInformation(onvifDevice, new OnvifDeviceInformationListener() {
                @Override
                public void onDeviceInformationReceived(@Nonnull OnvifDevice device, @Nonnull OnvifDeviceInformation deviceInformation) {

                    log.warn("接收消息{}", deviceInformation.toString());


                }
            });


            log.error("end");
        }
    }
}
