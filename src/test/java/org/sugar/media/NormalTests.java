package org.sugar.media;

import cn.hutool.log.StaticLog;
import org.junit.jupiter.api.Test;

/**
 * Date:2024/11/26 15:49:49
 * Author：Tobin
 * Description:
 */

public class NormalTests {


    @Test
    void normalTest(){
        String test = "media_status:1308717789573283840";

        String[] split = test.split("media_status:");
        StaticLog.info("{}", split[1]);
    }
}
