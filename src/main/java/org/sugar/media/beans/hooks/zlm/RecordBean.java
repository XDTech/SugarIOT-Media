package org.sugar.media.beans.hooks.zlm;

import lombok.Data;

import java.util.List;

/**
 * Date:2026/06/29 09:54:34
 * Author：Tobin
 * Description:
 */

@Data
public class RecordBean {

    private Integer code;
    private DataBean data;

    @Data
    public static class DataBean {
        private List<String> paths;
        private String rootPath;
    }
}
