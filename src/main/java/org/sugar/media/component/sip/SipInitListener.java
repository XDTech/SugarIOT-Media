package org.sugar.media.component.sip;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
public class SipInitListener implements CommandLineRunner {


    private SipFactory sipFactory;
    private SipStack sipStack;
    private SipProvider sipProvider;


    @Resource
    private SipProcessListener sipProcessListener;

    @Override
    public void run(String... args) throws Exception {
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
            ListeningPoint lp = sipStack.createListeningPoint("0.0.0.0", 5060, ListeningPoint.UDP);
            sipProvider = sipStack.createSipProvider(lp);
            sipProvider.addSipListener(sipProcessListener);

            sipStack.start();
           log.info("SIP Server started on port 5060...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
