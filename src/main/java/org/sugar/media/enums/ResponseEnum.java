package org.sugar.media.enums;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Date:2024/11/15 11:31:42
 * Author：Tobin
 * Description:
 */

public enum ResponseEnum {


    Success(0, "Success"), Fail(-1, "Fail"), custom(1, "custom msg");

    public final int code;
    public final String msg;

    ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
