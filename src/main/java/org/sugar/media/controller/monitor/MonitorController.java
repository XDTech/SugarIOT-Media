package org.sugar.media.controller.monitor;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.service.monitor.MonitorService;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date:2025/01/26 09:36:41
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {


    @Resource
    private MonitorService monitorService;


    @GetMapping
    public ResponseEntity<?> getSystemInfo() throws InterruptedException {

        Map<String, Object> osInfo = new HashMap<>();
        Map<String, String> sysInfo = this.monitorService.getSysInfo();

        osInfo.put("os", sysInfo);
        Map<String, String> jvmInfo = this.monitorService.getJvmInfo();

        osInfo.put("jvm", jvmInfo);

        List<Map> diskInfo = this.monitorService.getDiskInfo();
        osInfo.put("disk", diskInfo);

        Map<String, String> memoryInfo = this.monitorService.getMemoryInfo();


        osInfo.put("memory", memoryInfo);


        osInfo.put("cpu", this.monitorService.getCurrCpuInfo());


        return ResponseEntity.ok(ResponseBean.success(osInfo));

    }


    @GetMapping(path = "/cpu/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }
}
