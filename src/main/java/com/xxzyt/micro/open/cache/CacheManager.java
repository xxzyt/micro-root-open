package com.xxzyt.micro.open.cache;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: trace
 * @date: 2019-12-09.
 * @Description:
 * @Email: tracenet@126.com
 */
public class CacheManager {
    private static Map<String, CacheData> CACHE_DATA = new ConcurrentHashMap<String, CacheData>();

    public static String getData(String key) {
        CacheData cacheData = CACHE_DATA.get(key);
        if (cacheData != null && (cacheData.getExpire().getTime() > System.currentTimeMillis())) {
            return cacheData.getData();
        }
        clear(key);
        return null;
    }

    public static void setData(String key, String data, Date expire) {
        CACHE_DATA.put(key, new CacheData(data, expire));
    }

    public static void clear(String key) {
        CACHE_DATA.remove(key);
    }


    private static class CacheData {
        CacheData(String data, Date expire) {
            this.data = data;
            this.expire = expire;
        }

        private String data;

        private Date expire;

        public String getData() {
            return data;
        }

        public Date getExpire() {
            return expire;
        }
    }

//    public static void main(String[] args) {
//        Date date = new Date();
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTime(date);
//        calendar.add(Calendar.SECOND, 10);
//        calendar.add(Calendar.SECOND, -5);
//
//        CacheManager.setData("a", "123", calendar.getTime());
//
//        while (true) {
//            final String a = CacheManager.getData("a");
//            System.out.println(a);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}
