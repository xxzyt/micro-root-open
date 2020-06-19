package com.xxzyt.micro.open.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxzyt.micro.open.OpenClient;
import com.xxzyt.micro.open.config.enums.FieldType;
import com.xxzyt.micro.open.config.interfaces.IConfigPush;
import com.xxzyt.micro.open.config.setting.AbstractConfig;
import com.xxzyt.micro.open.config.setting.OpenSettingField;
import com.xxzyt.micro.open.data.GatewayResponse;
import com.xxzyt.micro.open.utils.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

/**
 * @author 李吉昆
 * @date 2019-12-17
 */
public class ConfigPush implements IConfigPush {
    private OpenClient openClient;
    private static final Logger log = LoggerFactory.getLogger(ConfigPush.class);

    public static ConfigPush getInstance() {
        ConfigPush configPush = new ConfigPush();
        configPush.openClient = OpenClient.getInstance();
        return configPush;
    }

    public static ConfigPush getInstance(String appId) {
        ConfigPush configPush = new ConfigPush();
        configPush.openClient = OpenClient.getInstance(appId);
        return configPush;
    }

    /**
     * 获取一个配置信息的结构
     *
     * @param config 配置对象
     * @return
     */
    public String getConfigStructure(Class<? extends AbstractConfig> config) {
        JSONObject jsonObject = new JSONObject(config.getDeclaredFields().length, true);
        for (Field declaredField : config.getDeclaredFields()) {
            final OpenSettingField annotation = declaredField.getAnnotation(OpenSettingField.class);
            if (annotation != null) {
                JSONObject filedJson = new JSONObject();
                filedJson.put("value", null);
                filedJson.put("meta", getFiledType(annotation, declaredField));
                jsonObject.put(declaredField.getName(), filedJson);
            }
        }
        return jsonObject.toString();
    }

    /**
     * 推送一个配置结构
     * 特性：增量更新，不移除原有字段
     *
     * @param config 配置对象
     */
    public GatewayResponse pushConfigStructure(String name, Class<? extends AbstractConfig> config) {
        //配置结构信息
        final String configStructure = getConfigStructure(config);
        JSONObject content = new JSONObject();
        content.put("id", config.getName());
        content.put("name", name);
        content.put("config", configStructure);
        final GatewayResponse<JSONObject> objectGatewayResponse = openClient.execute(10700, content).toEntity(JSONObject.class);
        if (objectGatewayResponse.isSuccess()) {
            log.info("推送配置成功！");
        } else {
            throw new RuntimeException(objectGatewayResponse.getBizMsg());
        }
        return objectGatewayResponse;
    }

    private JSONObject getFiledType(OpenSettingField annotation, Field declaredField) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", annotation.name());
        jsonObject.put("description", annotation.description());
        jsonObject.put("type", annotation.type().name());
        //如果是单选或者多选必须是枚举类型
        if (annotation.type().equals(FieldType.RADIO) || annotation.type().equals(FieldType.SELECT)) {
            JSONArray items = new JSONArray();
            if (declaredField.getType().isEnum()) {
                try {
                    Method methodMessage = declaredField.getType().getDeclaredMethod("getMessage", null);
                    List list = Arrays.asList(declaredField.getType().getEnumConstants());
                    for (Object o : list) {
                        String label = methodMessage.invoke(o, null).toString();
                        String value = o.toString();
                        JSONObject item = new JSONObject();
                        item.put("value", value);
                        item.put("label", label);
                        items.add(item);
                    }
                    jsonObject.put("items", items);
                } catch (Exception e) {
                    log.error(L.TAG, e);
                }
            } else {
                throw new RuntimeException("配置模板不符合约定");
            }
        }
        if (annotation.type().equals(FieldType.CHECKBOX)) {
            if (declaredField.getGenericType() instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) declaredField.getGenericType();
                Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
                if (actualTypeArgument.isEnum()) {
                    JSONArray items = new JSONArray();
                    try {
                        Method methodMessage = actualTypeArgument.getDeclaredMethod("getMessage", null);
                        List list = Arrays.asList(actualTypeArgument.getEnumConstants());
                        for (Object o : list) {
                            String label = methodMessage.invoke(o, null).toString();
                            String value = o.toString();
                            JSONObject item = new JSONObject();
                            item.put("value", value);
                            item.put("label", label);
                            items.add(item);
                        }
                        jsonObject.put("items", items);
                    } catch (Exception e) {
                        log.error(L.TAG, e);
                    }
                } else {
                    throw new RuntimeException("配置模板不符合约定");
                }
            } else {
                throw new RuntimeException("配置模板不符合约定");
            }
        }
        if (annotation.type().equals(FieldType.MULTI_UPLOAD)) {
            if (declaredField.getType().getSimpleName().equals("List")) {

            } else {
                throw new RuntimeException("配置模板不符合约定");
            }
        }
        if (annotation.type().equals(FieldType.SWITCH)) {
            if (declaredField.getType().getSimpleName().equals("Boolean")) {
                JSONObject item = new JSONObject();
                item.put("trueValue", "True");
                item.put("falseValue", "False");
                item.put("open", "开");
                item.put("close", "关");
                jsonObject.put("item", item);
            } else {
                throw new RuntimeException("配置模板不符合约定");
            }
        }
        return jsonObject;
    }
}
