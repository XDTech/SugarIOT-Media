package org.sugar.media.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor                 //无参构造
@AllArgsConstructor                //有参构造
public class ResponseBean {


    private Integer code;
    private Long total;

    private Object data;

    private String msg;


    public static ResponseBean createResponseBean(Integer Code, Long total, Object data, String msg) {

        return new ResponseBean(Code, total, data, msg);
    }


    public static ResponseBean createResponseBean(Integer Code, Object data, String msg) {

        return new ResponseBean(Code, null, data, msg);
    }

    public static ResponseBean createResponseBean(Integer Code, String msg) {

        return new ResponseBean(Code, null, null, msg);
    }

}