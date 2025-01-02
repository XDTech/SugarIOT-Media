package org.sugar.media.beans.hooks.zlm;

import lombok.Data;

import java.util.List;

/**
 * Date:2025/01/02 10:25:36
 * Author：Tobin
 * Description:
 */

@Data
public class StreamInfoRootBean {

    private int code;

    private List<StreamInfoBean> data;
}
