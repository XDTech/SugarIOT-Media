package org.sugar.media.enums;

/**
 * Date:2024/11/17 10:40:23
 * Author：Tobin
 * Description:
 */
public enum StatusEnum {


    online("1"), offline("0");

    private String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
