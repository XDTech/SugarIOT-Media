package org.sugar.media.hooks;

import cn.hutool.log.StaticLog;
import jakarta.servlet.http.PushBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Date:2024/11/13 18:08:41
 * Author：Tobin
 * Description: zlm hook api
 */

@RestController
@RequestMapping("/zlm")
public class ZlmHookController {



    @PostMapping("/keepalive")
    public void keepalive(@RequestBody Map<String,Object> body){
        StaticLog.info("{}",body);

    }

    @PostMapping("/server/started")
    public void started(@RequestBody Map<String,Object> body){
        StaticLog.info("{}",body);

    }
}
