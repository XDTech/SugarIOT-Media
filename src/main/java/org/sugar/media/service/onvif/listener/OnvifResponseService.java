package org.sugar.media.service.onvif.listener;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.OnvifDeviceInformationListener;
import be.teletask.onvif.listeners.OnvifResponseListener;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifDeviceInformation;
import be.teletask.onvif.responses.OnvifResponse;
import cn.hutool.core.lang.Console;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Date:2025/01/07 14:00:48
 * Author：Tobin
 * Description:
 */

@Service
@Slf4j
public class OnvifResponseService implements OnvifResponseListener {

    @Override
    public void onResponse(OnvifDevice onvifDevice, OnvifResponse onvifResponse) {
        log.warn("receive response:{}", onvifDevice.getUsername());
    }

    @Override
    public void onError(OnvifDevice onvifDevice, int i, String s) {
        log.error("receive error:{}", onvifDevice.getUsername());
    }
}
