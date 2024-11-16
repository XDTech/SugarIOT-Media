package org.sugar.media.hooks;

import cn.hutool.log.StaticLog;
import jakarta.servlet.http.PushBuilder;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;

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
    public ResponseBean keepalive(@RequestBody Map<String,Object> body){
        StaticLog.info("{}",body);

        return ResponseBean.success();

    }

    @PostMapping("/server/started")
    public ResponseBean started(@RequestBody Map<String,Object> body){
        StaticLog.info("{}",body);
        return ResponseBean.success();
    }
}
