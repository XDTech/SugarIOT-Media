package org.sugar.media.service.onvif.listener;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.OnvifDeviceInformationListener;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifDeviceInformation;
import cn.hutool.core.lang.Console;
import jakarta.annotation.Resource;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Date:2025/01/07 17:12:08
 * Author：Tobin
 * Description:
 */

@Setter
@Service
public class OnvifDeviceListenerService implements OnvifDeviceInformationListener {


    CompletableFuture<OnvifDeviceInformation> future;


    @Override
    public void onDeviceInformationReceived(OnvifDevice onvifDevice, OnvifDeviceInformation onvifDeviceInformation) {
        future.complete(onvifDeviceInformation);
    }

}
