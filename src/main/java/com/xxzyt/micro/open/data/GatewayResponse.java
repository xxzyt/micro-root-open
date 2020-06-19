package com.xxzyt.micro.open.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: trace
 * @date: 2019-08-15.
 * @Description:
 * @Email: tracenet@126.com
 */
@Data
public class GatewayResponse<T> implements Serializable {

    private String resultId;

    private String code;

    private String msg;

    private String bizCode;

    private String bizMsg;

    private T data;

    private Long dataTotal;

    public boolean isSuccess() {
        return this.bizCode == null ? false : this.bizCode.equals("200");
    }
}
