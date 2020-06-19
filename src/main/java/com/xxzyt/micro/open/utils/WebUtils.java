package com.xxzyt.micro.open.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 网络工具类。
 *
 * @author carver.gu
 * @since 1.0, Sep 12, 2009
 */
public abstract class WebUtils {
    private static final Logger log = LoggerFactory.getLogger(WebUtils.class);

    private static SSLContext ctx = null;
    private static HostnameVerifier verifier = null;
    private static SSLSocketFactory socketFactory = null;

    private static class DefaultTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    static {
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()},
                    new SecureRandom());

            ctx.getClientSessionContext().setSessionTimeout(15);
            ctx.getClientSessionContext().setSessionCacheSize(1000);

            socketFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            log.error(L.TAG, e);
        }
        verifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

    }

    private WebUtils() {
    }

    public static String doPostJson(String strURL, String json) throws IOException {
        BufferedReader reader = null;
        OutputStreamWriter out = null;
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = getConnection(url);
            connection.connect();
            out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.append(json);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder res = new StringBuilder();
            for (; (line = reader.readLine()) != null; res.append(line)) {
            }
            return res.toString();
        } catch (IOException var12) {
            log.error(L.TAG, var12);
            throw var12;
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

    private static HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection = null;
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection connHttps = (HttpsURLConnection) url.openConnection();
            connHttps.setSSLSocketFactory(socketFactory);
            connHttps.setHostnameVerifier(verifier);
            connection = connHttps;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "micro-root-open");
        return connection;
    }
}
