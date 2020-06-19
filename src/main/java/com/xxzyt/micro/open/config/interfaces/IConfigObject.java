package com.xxzyt.micro.open.config.interfaces;


import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.xxzyt.micro.open.config.setting.AbstractConfig;

/**
 * @author 李吉昆
 * @date 2019-12-17
 */
public interface IConfigObject {


    /**
     * 获取一个单个配置信息
     *
     * @param config 通过指定的字段获取
     */
    <T extends AbstractConfig> T getConfig(T config, SFunction<T, ?>... func);


    /**
     * 获取一个单个配置信息 （精确的配置，无数据时不填充默认数据）
     *
     * @param config 通过指定的字段获取
     */
    <T extends AbstractConfig> T getDefiniteConfig(T config, SFunction<T, ?>... func);

    /**
     * 获取一个配置模板下的默认数据
     *
     * @param config
     * @param <T>
     * @return
     */
    <T extends AbstractConfig> T getDefaultConfig(Class<T> config);

    /**
     * 存储一个配置信息 如果某个字段为 null 则不修改不创建
     *
     * @param config 配置信息
     */
    <T> Boolean saveConfig(AbstractConfig config, SFunction<T, ?>... func);


    /**
     * 存储一个配置信息 如果某个字段为 null 则不修改不创建
     *
     * @param config 配置信息
     */
    Boolean saveDefaultConfig(AbstractConfig config);
}
