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
    private static SipProvider udpSipProvider;


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


          // 接收所有notify请求，即使没有订阅
            properties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
            properties.setProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true");
            // 为_NULL _对话框传递_终止的_事件
            properties.setProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", "true");
            // 是否自动计算content length的实际长度，默认不计算
            properties.setProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "true");
            // 会话清理策略
            properties.setProperty("gov.nist.javax.sip.RELEASE_REFERENCES_STRATEGY", "Normal");
            // 处理由该服务器处理的基于底层TCP的保持生存超时
            properties.setProperty("gov.nist.javax.sip.RELIABLE_CONNECTION_KEEP_ALIVE_TIMEOUT", "60");
            // 获取实际内容长度，不使用header中的长度信息
            properties.setProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "true");
            // 线程可重入
            properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
            // 定义应用程序打算多久审计一次 SIP 堆栈，了解其内部线程的健康状况（该属性指定连续审计之间的时间（以毫秒为单位））
            properties.setProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS", "30000");

            properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", "gov.nist.javax.sip.stack.NioMessageProcessorFactory");

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


    public static SipProvider udpSipProvider() {
        return SipServer.udpSipProvider;
    }
}
