package com.xxzyt.micro.open.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.xxzyt.micro.open.OpenClient;
import com.xxzyt.micro.open.config.interfaces.IConfigObject;
import com.xxzyt.micro.open.config.setting.AbstractConfig;
import com.xxzyt.micro.open.data.GatewayResponse;
import com.xxzyt.micro.open.utils.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 李吉昆
 * @date 2019-12-17
 */
public class ConfigObject implements IConfigObject {
    private OpenClient openClient;
    private static final Logger log = LoggerFactory.getLogger(ConfigObject.class);


    public static ConfigObject getInstance() {
        ConfigObject configObject = new ConfigObject();
        configObject.openClient = OpenClient.getInstance();
        return configObject;
    }

    public static ConfigObject getInstance(String appId) {
        ConfigObject configObject = new ConfigObject();
        configObject.openClient = OpenClient.getInstance(appId);
        return configObject;
    }

    /**
     * 获取一个单个配置信息，自动使用默认数据填充为null的字段
     *
     * @param config 通过指定的字段获取
     */
    public <T extends AbstractConfig> T getConfig(T config, SFunction<T, ?>... func) {
        return get10703(config, convertWhere(config, func), true);
    }

    /**
     * 获取一个单个配置信息，
     * 可指定一个关键字来计数获取的次数，大于该次数时切换下一个配置信息
     *
     * @param config 配置依赖类型
     * @param key    计数关键字
     * @param number 对比次数
     */
    public <T extends AbstractConfig> T getConfig(Class<T> config, String key, Integer number) {
        JSONObject where = new JSONObject();
        where.put("key", key);
        where.put("number", number);
        where.put("isDefault", true);
        where.put("groupId", config.getName());
        try {
            return get10705(config.newInstance(), where);
        } catch (InstantiationException e) {
            log.error(L.TAG, e);
        } catch (IllegalAccessException e) {
            log.error(L.TAG, e);
        }
        return null;
    }

    /**
     * 获取一个单个配置信息 （精确的配置，无数据时不填充默认数据）
     *
     * @param config 通过指定的字段获取
     */
    public <T extends AbstractConfig> T getDefiniteConfig(T config, SFunction<T, ?>... func) {
        return get10703(config, convertWhere(config, func), false);
    }

    /**
     * 获取一个配置模板下的默认数据
     *
     * @param config
     * @param <T>
     * @return
     */
    public <T extends AbstractConfig> T getDefaultConfig(Class<T> config) {
        try {
            return get10703(config.newInstance(), null, true);
        } catch (InstantiationException e) {
            log.error(L.TAG, e);
        } catch (IllegalAccessException e) {
            log.error(L.TAG, e);
        }
        return null;
    }

    /**
     * 特性：局部更新数据
     * 存储一个配置信息
     * 1、如果某个字段只为 null 则不修改之前的数据
     * 2、同一个配置模板下只允许有一个默认的配置数据
     *
     * @param config 配置信息
     */
    public <T> Boolean saveConfig(AbstractConfig config, SFunction<T, ?>... func) {
        return save(config, convertWhere(config, func), false);
    }

    /**
     * 特性：局部更新数据
     * 1、如果某个字段只为 null 则不修改之前的数据
     * 2、如果该配置模板下没有任何数据，则首次创建的数据自动为该配置模板的默认数据
     *
     * @param config 配置信息
     */
    public Boolean saveDefaultConfig(AbstractConfig config) {
        return save(config, null, true);
    }

    private <T> JSONObject convertWhere(AbstractConfig config, SFunction<T, ?>... func) {
        JSONObject where = new JSONObject();
        //解析条件
        for (SFunction<T, ?> tsFunction : func) {
            SerializedLambda resolve = LambdaUtils.resolve(tsFunction);
            String implMethodName = resolve.getImplMethodName();
            String get = toLowerCaseFirstOne(implMethodName.replaceFirst("get", ""));
            try {
                Method declaredMethod = config.getClass().getDeclaredMethod(implMethodName);
                Object value = declaredMethod.invoke(config);
                if (value == null) {

                }
                where.put(get, value);
            } catch (IllegalAccessException e) {
                log.error(L.TAG, e);
            } catch (NoSuchMethodException e) {
                log.error(L.TAG, e);
            } catch (InvocationTargetException e) {
                log.error(L.TAG, e);
            }
        }
        return where;
    }


    private String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    private <T extends AbstractConfig> T get10705(AbstractConfig config, JSONObject where) {
        return getContent(where, config, 10705);
    }

    private <T extends AbstractConfig> T get10703(AbstractConfig config, JSONObject where, Boolean isDefault) {
        JSONObject content = new JSONObject();
        content.put("where", where);
        content.put("isDefault", isDefault);
        content.put("groupId", config.getClass().getName());
        return getContent(content, config, 10703);
    }

    private <T extends AbstractConfig> T getContent(JSONObject content, AbstractConfig config, Integer fun) {
        GatewayResponse<JSONObject> jsonObjectGatewayResponse = openClient.execute(fun, content).toEntity(JSONObject.class);
        if (jsonObjectGatewayResponse.isSuccess()) {
            JSONObject data = jsonObjectGatewayResponse.getData();
            String value = data.getString("value");
            return (T) JSONObject.parseObject(value, config.getClass());
        } else {
            log.info("getContent:::::" + jsonObjectGatewayResponse.toString());
            return null;
        }
    }

    private Boolean save(AbstractConfig config, JSONObject where, Boolean isDefault) {
        Object value = JSON.toJSON(config);
        JSONObject content = new JSONObject();
        content.put("id", config.getClass().getName());
        content.put("value", value.toString());
        content.put("where", where);
        content.put("isDefault", isDefault);
        GatewayResponse<JSONObject> jsonObjectGatewayResponse = this.openClient.execute(10701, content).toEntity(JSONObject.class);
        log.info("配置存储：" + jsonObjectGatewayResponse.isSuccess() + " " + jsonObjectGatewayResponse.getBizMsg());
        return jsonObjectGatewayResponse.isSuccess();
    }

}
