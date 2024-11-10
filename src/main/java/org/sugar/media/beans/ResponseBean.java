package org.sugar.media.beans;

import lombok.Data;

@Data
public class ResponseBean {

    private Long total;

    private Object data;

    public ResponseBean(Long total, Object data) {
        this.total = total;
        this.data = data;
    }
    public static ResponseBean createResponseBean(Long total, Object data) {

        return  new ResponseBean(total,data);
    }

    public static ResponseBean createResponseBean(Object data) {

        return  new ResponseBean(null,data);
    }

}