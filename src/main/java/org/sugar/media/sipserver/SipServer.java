package org.sugar.media.sipserver;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.utils.SipConfUtils;

import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import java.util.Properties;

/**
 * Date:2024/12/08 21:42:15
 * Author：Tobin
 * Description:
 */

@Slf4j
@Component
public class SipServer implements CommandLineRunner {


    private SipFactory sipFactory;
    private SipStack sipStack;
    private SipProvider udpSipProvider;


    @Resource
    private SipConfUtils sipConfUtils;


    @Resource
    private SipEventListener sipEventListener;

    @Override
    public void run(String... args) {
        try {
            // 配置 SIP 属性
            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "SIPServer");
            properties.setProperty("javax.sip.PATH_NAME", "gov.nist");

            // 创建 SIP 工厂和栈
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            sipStack = sipFactory.createSipStack(properties);

            // 创建监听器
            ListeningPoint lp = sipStack.createListeningPoint(this.sipConfUtils.getIp(), this.sipConfUtils.getPort(), ListeningPoint.UDP);
            udpSipProvider = sipStack.createSipProvider(lp);
            udpSipProvider.addSipListener(sipEventListener);
            sipStack.start();

            String ipAddress = udpSipProvider.getListeningPoint(ListeningPoint.UDP).getIPAddress();

            log.info("SIP Server started on {} port {}...", ipAddress, this.sipConfUtils.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Bean
    public SipProvider udpSipProvider() {
        return udpSipProvider;
    }
}
