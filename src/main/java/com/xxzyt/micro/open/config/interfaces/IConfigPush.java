package com.xxzyt.micro.open.config.interfaces;


import com.xxzyt.micro.open.config.setting.AbstractConfig;
import com.xxzyt.micro.open.data.GatewayResponse;

/**
 * @author 李吉昆
 * @date 2019-12-17
 */
public interface IConfigPush {

    /**
     * 获取一个配置信息的结构
     *
     * @param config 配置对象
     * @return
     */
    String getConfigStructure(Class<? extends AbstractConfig> config);


    /**
     * 推送一个配置结构
     *
     * @param name   配置模板名称
     * @param config 配置对象
     */
    GatewayResponse pushConfigStructure(String name, Class<? extends AbstractConfig> config);
}
