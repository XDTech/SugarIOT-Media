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

    // key:channelCode
    private ConcurrentHashMap<String, SsrcInfoBean> ssrcMapByChannel = new ConcurrentHashMap<>();

    // 按 ssrc 索引的 map
    private ConcurrentHashMap<String, SsrcInfoBean> ssrcMapBySsrc = new ConcurrentHashMap<>();

    public String createPlaySsrc(SsrcInfoBean ssrcInfoBean) {
        Console.log(ssrcInfoBean.getChannelCode(), "=====");
        String ssrc = StrUtil.format("0{}{}", this.getDomain(), this.getRandomNumber());

        ssrcInfoBean.setSsrc(ssrc);
        ssrcMapByChannel.put(ssrcInfoBean.getChannelCode(), ssrcInfoBean);
        ssrcMapBySsrc.put(ssrc, ssrcInfoBean);
        this.sipCacheService.setSsrc(ssrc, ssrcInfoBean.getChannelCode());

        return ssrc;
    }

    public void updateSsrc(String channelCode, SsrcInfoBean ssrcInfoBean) {


        this.ssrcMapByChannel.put(channelCode, ssrcInfoBean);
        this.sipCacheService.deleteSsrc(channelCode);
    }


    public String createHistorySsrc(SsrcInfoBean ssrcInfoBean) {

        String ssrc = StrUtil.format("1{}{}", this.getDomain(), this.getRandomNumber());
        ssrcMapByChannel.put(ssrcInfoBean.getChannelCode(), ssrcInfoBean);
        ssrcMapBySsrc.put(ssrc, ssrcInfoBean);
        this.sipCacheService.setSsrc(ssrc, ssrcInfoBean.getChannelCode());
        return ssrc;
    }

    public void releaseSsrc(String ssrc, String channelCode) {
        Console.log(ssrc, channelCode, "=====_++");
        this.ssrcMapBySsrc.remove(ssrc);
        this.ssrcMapByChannel.remove(channelCode);
    }

    public void releaseSsrc(String ssrc) {

        if (!this.ssrcMapBySsrc.containsKey(ssrc)) return;

        SsrcInfoBean ssrcInfoBean = this.ssrcMapBySsrc.get(ssrc);
        this.releaseSsrc(ssrc, ssrcInfoBean.getChannelCode());


    }


    // 通过ssrc查找info

    public SsrcInfoBean getSsrc(String ssrc) {
        Console.log(ssrcMapBySsrc.toString());
        Console.log(ssrcMapByChannel.toString());
        if (!this.ssrcMapBySsrc.containsKey(ssrc)) return null;


        return this.ssrcMapBySsrc.get(ssrc);
    }

    public SsrcInfoBean getSsrcByCode(String channelCode) {

        if (!this.ssrcMapByChannel.containsKey(channelCode)) return null;


        return this.ssrcMapByChannel.get(channelCode);
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

            if (this.ssrcMapBySsrc.containsKey(ssrc)) continue;

            return ssrc;

        }
    }
}
