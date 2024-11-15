package org.sugar.media.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sugar.media.enums.ResponseEnum;

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

    public static ResponseBean success(Long total, Object data) {
        return new ResponseBean(ResponseEnum.Success.getCode(), total, data, ResponseEnum.Success.getMsg());
    }

    public static ResponseBean success() {
        return new ResponseBean(ResponseEnum.Success.getCode(), null, null, ResponseEnum.Success.getMsg());
    }

    public static ResponseBean success(Object data) {
        return new ResponseBean(ResponseEnum.Success.getCode(), null, data, ResponseEnum.Success.getMsg());
    }


    // fail
    public static ResponseBean fail() {
        return new ResponseBean(ResponseEnum.Fail.getCode(), null, null, ResponseEnum.Fail.getMsg());
    }

    public static ResponseBean fail(String msg) {
        return new ResponseBean(ResponseEnum.Fail.getCode(), null, null, msg);
    }

}