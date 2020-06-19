package com.xxzyt.micro.open.data;


import lombok.Data;

import java.io.Serializable;

/**
 * @author: JiKun.Li
 * @date: 2018-11-01.
 * @Description:
 * @Email: tracenet@126.com
 */
@Data
public class GatewayRequest implements Serializable {

    private String appId;

    private String token;

    private String method;

    private Object content;

    private Integer version = 120;

}
