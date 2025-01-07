package org.sugar.media.service.onvif;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifDeviceInformation;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sugar.media.service.onvif.listener.OnvifDeviceListenerService;
import org.sugar.media.service.onvif.listener.OnvifResponseService;

import java.util.concurrent.CompletableFuture;

/**
 * Date:2025/01/07 17:00:26
 * Author：Tobin
 * Description:
 */

@Configuration
public class OnvifManagerService {

    @Resource
    private OnvifResponseService onvifResponseService;

    @Resource
    private OnvifDeviceListenerService onvifDeviceListenerService;

    private OnvifManager onvifManager = new OnvifManager(onvifResponseService);

    /**
     * 获取设备信息
     *
     * @param onvifDevice
     */

    @SneakyThrows
    public OnvifDeviceInformation getDeviceInfo(OnvifDevice onvifDevice) {
        CompletableFuture<OnvifDeviceInformation> future = new CompletableFuture<>();
        this.onvifDeviceListenerService.setFuture(future);
        onvifManager.getDeviceInformation(onvifDevice, onvifDeviceListenerService);
        return future.get();

    }


}
