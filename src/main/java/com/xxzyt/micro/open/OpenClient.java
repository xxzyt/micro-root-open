package com.xxzyt.micro.open;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxzyt.micro.open.cache.CacheManager;
import com.xxzyt.micro.open.data.GatewayRequest;
import com.xxzyt.micro.open.data.GatewayResponse;
import com.xxzyt.micro.open.utils.L;
import com.xxzyt.micro.open.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author: trace
 * @date: 2019-12-09.
 * @Description:
 * @Email: tracenet@126.com
 */
public class OpenClient {
    private static final Logger log = LoggerFactory.getLogger(OpenClient.class);

    private static String domain = "https://v1-api.xxzyt.com/gateway.do";   //::替换为智云开放平台网关地址::

    private String token;
    private String result;
    private final static String CACHE_APP = "APP";
    private final static String CACHE_ACCOUNT = "ACCOUNT";
    private static HashMap<String, String> apps = new HashMap<String, String>();


    public static void initInstance(String domain) {
        initInstance(domain, null, null);
    }

    public static void initInstance(String domain, String appId, String appKey) {
        apps.put(appId, appKey);
        OpenClient.domain = domain;
    }

    /**
     * 获取一个应用级授权的执行客户端
     */
    public static OpenClient getInstance() {
        if (apps.size() == 0) {
            throw new RuntimeException("未初始化开放平台应用信息");
        }
        if (apps.size() != 1) {
            throw new RuntimeException("配置多应用情况下请指定应用编号调用服务");
        }
        Set<String> strings = apps.keySet();
        String identity = null;
        for (String key : strings) {
            identity = key;
            break;
        }
        OpenClient openClient = new OpenClient();
        openClient.loginIn(identity, null, CACHE_APP);
        return openClient;
    }

    /**
     * 获取一个指定的应用授权
     */
    public static OpenClient getInstance(String appId) {
        OpenClient openClient = new OpenClient();
        openClient.loginIn(appId, null, CACHE_APP);
        return openClient;
    }

    /**
     * 获取一个用户级授权的执行客户端
     */
    public static OpenClient getInstance(String identity, String signature) {
        OpenClient openClient = new OpenClient();
        openClient.loginIn(identity, signature, CACHE_ACCOUNT);
        return openClient;
    }

    /**
     * 远程服务执行方法
     */
    public OpenClient execute(Integer method, Object obj) {
        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setMethod(method.toString());
        gatewayRequest.setToken(this.token);
        gatewayRequest.setContent(obj);
        this.result = doResult(gatewayRequest);
        return this;
    }

    /**
     * 返回的结果信息
     */
    public String getResult() {
        return this.result;
    }

    public <T> GatewayResponse<T> toEntity(Class<T> t) {
        JSONObject jsonObject = JSONObject.parseObject(result);
        GatewayResponse gatewayResponse = jsonObject.toJavaObject(GatewayResponse.class);
        if (gatewayResponse.getData() != null && gatewayResponse.getData() instanceof JSONObject) {
            gatewayResponse.setData(((JSONObject) gatewayResponse.getData()).toJavaObject(t));
        }
        return gatewayResponse;
    }

    public <T> GatewayResponse<List<T>> toList(Class<T> t) {
        GatewayResponse gatewayResponse = JSONObject.parseObject(this.result, GatewayResponse.class);
        if (gatewayResponse.getData() instanceof JSONArray) {
            JSONArray data = (JSONArray) gatewayResponse.getData();
            List<T> ts = data.toJavaList(t);
            gatewayResponse.setData(ts);
        }
        return gatewayResponse;
    }

    /**
     * 获取数据令牌
     *
     * @param identity  应用编号或者用户名
     * @param signature 应用密钥或者密码
     * @param ext       类型
     */
    private void loginIn(String identity, String signature, String ext) {
        String cacheKey;
        if (ext.equals(CACHE_APP)) {
            signature = apps.get(identity);
            cacheKey = ext + identity;
        } else {
            cacheKey = ext + identity + signature;
        }
        String token = CacheManager.getData(cacheKey);
        if (token == null) {
            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setAppId(identity);
            gatewayRequest.setMethod("10300");
            JSONObject content = new JSONObject();
            if (ext.equals(CACHE_APP)) {
                gatewayRequest.setAppId(identity);
            } else {
                content.put("identity", identity);
            }
            content.put("signature", signature);
            content.put("source", "API_SOURCE");
            gatewayRequest.setContent(content);
            String s = doResult(gatewayRequest);
            JSONObject jsonObject = JSONObject.parseObject(s);
            String bizCode = jsonObject.getString("biz_code") == null ? jsonObject.getString("code") : jsonObject.getString("biz_code");
            JSONObject data = jsonObject.getJSONObject("data");
            if (bizCode != null && bizCode.equals("200")) {
                this.token = data.getString("token");
                //提前一天清空缓存，防止授权错误
                final Date expire = data.getDate("expire");
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(expire);
                calendar.add(Calendar.DATE, -1);
                CacheManager.setData(cacheKey, this.token, calendar.getTime());
            } else {
                log.error("开放平台授权异常");
                throw new RuntimeException("开放平台授权：" + jsonObject.getString("biz_msg") == null ? jsonObject.getString("msg") : jsonObject.getString("biz_msg"));
            }
        } else {
            this.token = token;
        }
    }

    private String doResult(GatewayRequest gatewayRequest) {
        String requestJson = JSONObject.toJSONString(gatewayRequest);
        try {
            return WebUtils.doPostJson(OpenClient.domain, requestJson);
        } catch (Exception e) {
            log.error(L.TAG, e);
            GatewayResponse response = new GatewayResponse();
            response.setBizCode("500");
            response.setBizMsg(e.getMessage());
            return JSONObject.toJSONString(response);
        }
    }
}
