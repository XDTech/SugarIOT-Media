package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Date:2024/12/27 10:41:16
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishAckBean {
    @JsonProperty("code")
    private int code; // 错误代码，0代表允许推流

    @JsonProperty("msg")
    private String msg; // 不允许推流时的错误提示

    @JsonProperty("enable_hls")
    private Boolean enableHls; // 是否转换成hls-mpegts协议

    @JsonProperty("enable_hls_fmp4")
    private Boolean enableHlsFmp4; // 是否转换成hls-fmp4协议

    @JsonProperty("enable_mp4")
    private Boolean enableMp4; // 是否允许mp4录制

    @JsonProperty("enable_rtsp")
    private Boolean enableRtsp; // 是否转rtsp协议

    @JsonProperty("enable_rtmp")
    private Boolean enableRtmp; // 是否转rtmp/flv协议

    @JsonProperty("enable_ts")
    private Boolean enableTs; // 是否转http-ts/ws-ts协议

    @JsonProperty("enable_fmp4")
    private Boolean enableFmp4; // 是否转http-fmp4/ws-fmp4协议

    @JsonProperty("hls_demand")
    private Boolean hlsDemand; // 该协议是否有人观看才生成

    @JsonProperty("rtsp_demand")
    private Boolean rtspDemand; // 该协议是否有人观看才生成

    @JsonProperty("rtmp_demand")
    private Boolean rtmpDemand; // 该协议是否有人观看才生成

    @JsonProperty("ts_demand")
    private Boolean tsDemand; // 该协议是否有人观看才生成

    @JsonProperty("fmp4_demand")
    private Boolean fmp4Demand; // 该协议是否有人观看才生成

    @JsonProperty("enable_audio")
    private Boolean enableAudio; // 转协议时是否开启音频

    @JsonProperty("add_mute_audio")
    private Boolean addMuteAudio; // 转协议时，无音频是否添加静音aac音频

    @JsonProperty("mp4_save_path")
    private String mp4SavePath; // mp4录制文件保存根目录，置空使用默认

    @JsonProperty("mp4_max_second")
    private Integer mp4MaxSecond; // mp4录制切片大小，单位秒

    @JsonProperty("mp4_as_player")
    private Boolean mp4AsPlayer; // MP4录制是否当作观看者参与播放人数计数

    @JsonProperty("hls_save_path")
    private String hlsSavePath; // hls文件保存保存根目录，置空使用默认

    @JsonProperty("modify_stamp")
    private Integer modifyStamp; // 该流是否开启时间戳覆盖

    @JsonProperty("continue_push_ms")
    private Integer continuePushMs; // 断连续推延时，单位毫秒

    @JsonProperty("auto_close")
    private Boolean autoClose; // 无人观看是否自动关闭流

    @JsonProperty("stream_replace")
    private String streamReplace; // 是否修改流id

}
