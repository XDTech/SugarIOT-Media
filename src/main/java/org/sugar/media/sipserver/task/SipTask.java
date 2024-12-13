package org.sugar.media.sipserver.task;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.utils.SipConfUtils;

/**
 * Date:2024/12/13 21:23:20
 * Author：Tobin
 * Description: 在这里维护sip的定时任务，需要在过期时间之前再次请求到设备，比如刷新订阅
 */

@Slf4j
@Component
public class SipTask {


}
