package com.financialfinshieldguard.gold.utils;

import java.net.URL;

public class URLParseUtil {


    public static String extractObjectName(String downloadUrl) {
        try {
            // 解析 URL
            URL url = new URL(downloadUrl);

            // 获取路径部分
            String path = url.getPath();

            // 去掉路径中的前导斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            //以下没必要，因为getPath后本来就不包括后面的？查询参数
//            // 去掉查询参数
//            int queryIndex = path.indexOf('?');
//            if (queryIndex != -1) {
//                path = path.substring(0, queryIndex);
//            }

            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
