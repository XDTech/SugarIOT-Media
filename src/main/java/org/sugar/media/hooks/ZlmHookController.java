package org.sugar.media.hooks;

import cn.hutool.core.convert.Convert;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import jakarta.servlet.http.PushBuilder;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.node.NodeService;

import java.util.Map;

/**
 * Date:2024/11/13 18:08:41
 * Author：Tobin
 * Description: zlm hook api
 */

@RestController
@RequestMapping("/zlm")
public class ZlmHookController {

    @Resource
    private MediaCacheService mediaCacheService;


    @PostMapping("/keepalive")
    public ResponseBean keepalive(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);

        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        this.mediaCacheService.setMediaStatus(mediaServerId, StatusEnum.online.getStatus());

        return ResponseBean.success();

    }

    @PostMapping("/server/started")
    public ResponseBean started(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);
        return ResponseBean.success();
    }
}
