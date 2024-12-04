package org.sugar.media.beans.hooks.zlm;

import lombok.Data;

/**
 * Date:2024/12/04 10:36:14
 * Author：Tobin
 * Description:
 */

@Data
public class StreamProxyInfoBean {
        private int code;
        private Data data;
        private String msg;

        // Getter and Setter methods
        // Inner class Data
        public static class Data {
            private int liveSecs;
            private int rePullCount;
            private Src src;
            private int status;
            private int totalReaderCount;
            private String url;

            @Override
            public String toString() {
                return "Data{" +
                        "liveSecs=" + liveSecs +
                        ", rePullCount=" + rePullCount +
                        ", src=" + src +
                        ", status=" + status +
                        ", totalReaderCount=" + totalReaderCount +
                        ", url='" + url + '\'' +
                        '}';
            }

            public int getLiveSecs() {
                return liveSecs;
            }

            public void setLiveSecs(int liveSecs) {
                this.liveSecs = liveSecs;
            }

            public int getRePullCount() {
                return rePullCount;
            }

            public void setRePullCount(int rePullCount) {
                this.rePullCount = rePullCount;
            }

            public Src getSrc() {
                return src;
            }

            public void setSrc(Src src) {
                this.src = src;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public int getTotalReaderCount() {
                return totalReaderCount;
            }

            public void setTotalReaderCount(int totalReaderCount) {
                this.totalReaderCount = totalReaderCount;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            // Getter and Setter methods

            // Inner class Src
            public static class Src {
                private String app;
                private String params;
                private String stream;
                private String vhost;

                // Getter and Setter methods


                @Override
                public String toString() {
                    return "Src{" +
                            "app='" + app + '\'' +
                            ", params='" + params + '\'' +
                            ", stream='" + stream + '\'' +
                            ", vhost='" + vhost + '\'' +
                            '}';
                }

                public String getApp() {
                    return app;
                }

                public void setApp(String app) {
                    this.app = app;
                }

                public String getParams() {
                    return params;
                }

                public void setParams(String params) {
                    this.params = params;
                }

                public String getStream() {
                    return stream;
                }

                public void setStream(String stream) {
                    this.stream = stream;
                }

                public String getVhost() {
                    return vhost;
                }

                public void setVhost(String vhost) {
                    this.vhost = vhost;
                }
            }
    }



}
