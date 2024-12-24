package org.sugar.media.sipserver.manager;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.sipserver.utils.SipConfUtils;
import org.sugar.media.sipserver.utils.SipUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Date:2024/12/23 14:07:33
 * Author：Tobin
 * Description: 管理、维护ssrc
 */

@Service
public class SsrcManager {


    @Resource
    private SipConfUtils sipConfUtils;

    @Resource
    private SipCacheService sipCacheService;

    private ConcurrentHashMap<String, SsrcInfoBean> ssrcMap = new ConcurrentHashMap<>();


    // 通道code ssrc对照map
    private ConcurrentHashMap<String, String> channelMap = new ConcurrentHashMap<>();

    public String createPlaySsrc(SsrcInfoBean ssrcInfoBean) {
        Console.log(ssrcInfoBean.getChannelCode(), "=====");
        String ssrc = StrUtil.format("0{}{}", this.getDomain(), this.getRandomNumber());
        this.ssrcMap.put(ssrc, ssrcInfoBean);
        this.channelMap.put(ssrcInfoBean.getChannelCode(), ssrcInfoBean.getChannelCode());

        this.sipCacheService.setSsrc(ssrc, ssrcInfoBean.getChannelCode());
        return ssrc;
    }

    public void updateSsrc(String ssrc, SsrcInfoBean ssrcInfoBean) {


        this.ssrcMap.put(ssrc, ssrcInfoBean);
        Console.log(ssrcInfoBean.getChannelCode(), "++++");
        this.channelMap.put(ssrcInfoBean.getChannelCode(), ssrcInfoBean.getSsrc());
        this.sipCacheService.deleteSsrc(ssrc);
    }


    public String createHistorySsrc(SsrcInfoBean ssrcInfoBean) {

        String ssrc = StrUtil.format("1{}{}", this.getDomain(), this.getRandomNumber());
        this.ssrcMap.put(ssrc, ssrcInfoBean);
        this.channelMap.put(ssrcInfoBean.getChannelCode(), ssrcInfoBean.getSsrc());
        this.sipCacheService.setSsrc(ssrc, ssrcInfoBean.getChannelCode());
        return ssrc;
    }

    public void releaseSsrc(String ssrc, String channelCode) {
        Console.log(ssrc, channelCode, "=====_++");
        this.ssrcMap.remove(ssrc);
        this.channelMap.remove(channelCode);
    }

    public void releaseSsrc(String ssrc) {
        this.ssrcMap.remove(ssrc);

        channelMap.entrySet().removeIf(entry -> entry.getValue().equals(ssrc));

    }

    public boolean containsSsrc(String ssrc) {
        return this.ssrcMap.containsKey(ssrc);
    }


    public SsrcInfoBean getSsrc(String ssrc) {
        if (!this.containsSsrc(ssrc)) return null;

        return this.ssrcMap.get(ssrc);
    }

    public SsrcInfoBean getSsrcByCode(String channelCode) {

        if (!this.channelMap.containsKey(channelCode)) {
            return null;
        }


        // 包括就返回
        return this.getSsrc(this.channelMap.get(channelCode));

    }

    private String getDomain() {
        StringBuilder domain = new StringBuilder(this.sipConfUtils.getDomain());

        if (domain.length() < 8) {
            // 补0
            int length = 8 - domain.length();
            for (int i = 0; i < length; i++) {
                domain.append("0");
            }
        }

        return domain.length() < 8 ? domain.toString() : domain.substring(3, 8);

    }


    private String getRandomNumber() {
        while (true) {
            String ssrc = Convert.toStr(RandomUtil.randomInt(1000, 9999));

            if (this.ssrcMap.containsKey(ssrc)) continue;

            return ssrc;

        }
    }
}
