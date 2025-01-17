package com.github.catvod.spider;

import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.ProxyVideo;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy extends Spider {

    private static int port = -1;

    public static Object[] proxy(Map<String, String> params) throws Exception {
        switch (params.get("do")) {
            case "ck":
                return new Object[]{200, "text/plain; charset=utf-8", new ByteArrayInputStream("ok".getBytes("UTF-8"))};
            case "ali":
                return Ali.proxy(params);
            case "bili":
                return Bili.proxy(params);
            case "webdav":
                return WebDAV.vod(params);
            case "local":
                return Local.proxy(params);
            case "proxy":
                return commonProxy(params);
            default:
                return null;
        }
    }
    private static final List<String> keys = Arrays.asList("url", "header", "do", "Content-Type", "User-Agent", "Host");

    private static Object[] commonProxy(Map<String, String> params) throws Exception {
        String url = new String(Base64.decode(params.get("url"),Base64.DEFAULT), Charset.defaultCharset());
        Map<String, String> header = new Gson().fromJson(new String(Base64.decode(params.get("header"),Base64.DEFAULT), Charset.defaultCharset()), Map.class);
        if (header == null) header = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!keys.contains(entry.getKey())) header.put(entry.getKey(), entry.getValue());
        }
        return new Object[]{ProxyVideo.proxy(url, header)};
    }

    static void adjustPort() {
        if (Proxy.port > 0) return;
        int port = 9978;
        while (port < 10000) {
            String resp = OkHttp.string("http://127.0.0.1:" + port + "/proxy?do=ck", null);
            if (resp.equals("ok")) {
                SpiderDebug.log("Found local server port " + port);
                Proxy.port = port;
                break;
            }
            port++;
        }
    }

    public static int getPort() {
        adjustPort();
        return port;
    }

    public static String getUrl() {
        adjustPort();
        return "http://127.0.0.1:" + port + "/proxy";
    }
}
