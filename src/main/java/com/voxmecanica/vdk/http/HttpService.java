package com.voxmecanica.vdk.http;

import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/***
 * HttpService manages http interaction with server.
 */
public class HttpService {

    private Logger LOG = new Logger("HttpService");
    private okhttp3.OkHttpClient okClient;

    public HttpService() {
        okClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(Config.DEFAULT_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(Config.DEFAULT_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(Config.DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
    }

    // ResponseAsString returns the body of response as string.
    public static String ResponseAsString(okhttp3.Response rsp) {
        String result = "";
        if (rsp != null && rsp.body().contentLength() > 0) {
            try {
                result = rsp.body().string();
            } catch (IOException ex) {
                throw new VoxException("HttpService - Unable to access response as string:", ex);
            }
        }
        return result;
    }

    //Get gets a remote resource using default request configs
    public okhttp3.Response get(String url, Map<String, String> params) {
        okhttp3.HttpUrl httpUrl = okhttp3.HttpUrl.parse(url);
        okhttp3.HttpUrl.Builder builder = httpUrl.newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                builder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        httpUrl = builder.build();

        okhttp3.Request req = new okhttp3.Request.Builder()
                .url(httpUrl)
                .header("User-Agent", Config.HTTP_USER_AGENT)
                .build();
        okhttp3.Response rsp;
        try {
            rsp = okClient.newCall(req).execute();
        } catch (IOException ex) {
            throw new VoxException("HttpService - Error on http Get:", ex);
        }

        if (!rsp.isSuccessful()) {
            throw new VoxException("HttpService - Response not successful, got code " + rsp.code());
        }
        return rsp;
    }

    //post posts a remote resource using default request configs
    public okhttp3.Response post(String url, Map<String, String> params) {
        okhttp3.HttpUrl httpUrl = okhttp3.HttpUrl.parse(url);
        okhttp3.FormBody.Builder formBldr = new okhttp3.FormBody.Builder();

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                formBldr.add(param.getKey(), param.getValue());
            }
        }

        okhttp3.Request req = new okhttp3.Request.Builder()
                .url(httpUrl)
                .header("User-Agent", Config.HTTP_USER_AGENT)
                .post(formBldr.build())
                .build();
        okhttp3.Response rsp;
        try {
            rsp = okClient.newCall(req).execute();
        } catch (IOException ex) {
            throw new VoxException("HttpService - Error on http Get:", ex);
        }

        if (!rsp.isSuccessful()) {
            throw new VoxException("HttpService - Response not successful, got code " + rsp.code());
        }
        return rsp;
    }

    // serve calls either get or post.
    public okhttp3.Response serve(String url, Map<String, String> params, String method) {
        if (method.toUpperCase().equals("GET")) {
            return get(url, params);
        } else if (method.toUpperCase().equals("POST")) {
            return post(url, params);
        } else {
            throw new UnsupportedOperationException("HttpService - Unsupported http method " + method);
        }
    }

    // serve uses okhttp3.Request to generate a request
    public okhttp3.Response serve(okhttp3.Request req) {
        okhttp3.Response rsp;
        try {
            rsp = okClient.newCall(req).execute();
        } catch (IOException ex) {
            throw new VoxException("HttpService - Error on http Get:", ex);
        }

        return rsp;
    }

    private void validateUri(URI uri) {
        if (uri == null) {
            throw new VoxException("HttpService - Specified Http Uri is null.");
        }

        if (!uri.getScheme().equalsIgnoreCase("http")
                && !uri.getScheme().equalsIgnoreCase("https")) {
            throw new VoxException("HttpService - Vox Mecanica only supports htttp:// or https:// schemes.");
        }
    }

    public static class Config {
        public static final String HTTP_USER_AGENT = "VoxMecanica/(Voice Client)";
        public static final String DEFAULT_CHARCSET = "UTF-8";
        public static final String DEFAULT_SUBMIT_METHOD = "GET";

        public static final long DEFAULT_CONNECT_TIMEOUT_SEC = 17;
        public static final long DEFAULT_WRITE_TIMEOUT_SEC = 21;
        public static final long DEFAULT_READ_TIMEOUT_SEC = 21;

    }

}
