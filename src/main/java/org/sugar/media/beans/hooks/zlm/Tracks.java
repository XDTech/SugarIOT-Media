
package org.sugar.media.beans.hooks.zlm;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tracks {

    private int channels;

    @JsonProperty("codec_id")
    private int codecId;

    @JsonProperty("codec_id_name")
    private String codecIdName;

    @JsonProperty("codec_type")
    private int codecType;

    private long duration;
    private int frames;
    private boolean ready;

    @JsonProperty("sample_bit")
    private int sampleBit;

    @JsonProperty("sample_rate")
    private int sampleRate;

    private int fps;

    @JsonProperty("gop_interval_ms")
    private int gopIntervalMs;

    @JsonProperty("gop_size")
    private int gopSize;

    private int height;

    @JsonProperty("key_frames")
    private int keyFrames;

    private int width;


}