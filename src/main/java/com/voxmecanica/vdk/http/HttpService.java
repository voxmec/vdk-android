package com.voxmecanica.vdk.http;

import android.net.http.AndroidHttpClient;
import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

/***
 * HttpService manages http interaction with server.
 */
public class HttpService {

    private Logger LOG = new Logger("HttpService");
    private boolean started;
    private AndroidHttpClient httpClient;
    private ClientConnectionManager connectionMgr;
    private ExecutorService threadPool;
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

    @Deprecated
    private void initialize() {
        LOG.i("Initializing Http Client Service");
        threadPool = Executors.newSingleThreadExecutor();
        httpClient = AndroidHttpClient.newInstance(Config.HTTP_USER_AGENT);
        HttpParams params = httpClient.getParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 9000);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 9000);
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

    @Deprecated
    public void start() {
        if (!started) {
            initialize();
            started = true;
        }
    }

    @Deprecated
    public void stop() {
        if (started && connectionMgr != null) {
            LOG.i("Shutting down http client service.");
            threadPool.shutdownNow();
            connectionMgr.closeExpiredConnections();
            connectionMgr = null;
            started = false;
        }
    }

    @Deprecated
    public boolean isStarted() {
        return started;
    }

    public byte[] requestResourceAsBytes(HttpClientRequest request) {
        HttpServerResponse rsp = getServerResponse(request);
        return rsp.getContent();
    }

    @Deprecated
    public HttpServerResponse requestResource(HttpClientRequest request) {
        return getServerResponse(request);
    }

    //TODO - Refactor / Rename
    private HttpServerResponse getServerResponse(final HttpClientRequest req) {
        if (!started) {
            throw new VoxException("Http Client Service not started.");
        }

        final HttpUriRequest uriRequest = req.toHttpUriRequest();
        validateUri(uriRequest.getURI());

        LOG.d("HttpService - Retrieving data from location: " + req.getRequestPath());

        Future<HttpServerResponse> reqResult = threadPool.submit(
                new Callable<HttpServerResponse>() {
                    @Override
                    public HttpServerResponse call() throws Exception {
                        return httpClient.execute(uriRequest, new HttpResponseHandler(uriRequest.getURI().toString()));
                    }
                }
        );

        try {
            HttpServerResponse response = reqResult.get();
            LOG.d("Received response from " + uriRequest.getURI().toString());
            if (response.getStatusCode() != 200) {
                throw new VoxException(
                        String.format("HttpService - Dialog server returned unexpected HTTP code %d", response.getStatusCode())
                );
            }
            return response;
        } catch (ExecutionException ex) {
            throw new VoxException("HttpService - Error while retrieving connection result:", ex);
        } catch (InterruptedException ex) {
            throw new VoxException("HttpService - Connection exception:", ex);
        }
    }

    public static class Config {
        public static final String HTTP_USER_AGENT = "VoxMecanica/(Voice Client)";
        public static final String DEFAULT_CHARCSET = "UTF-8";
        public static final String DEFAULT_SUBMIT_METHOD = "GET";

        public static final long DEFAULT_CONNECT_TIMEOUT_SEC = 17;
        public static final long DEFAULT_WRITE_TIMEOUT_SEC = 21;
        public static final long DEFAULT_READ_TIMEOUT_SEC = 21;

        public static final int DEFAULT_HTTP_PORT = 80;
        public static final int DEFAULT_HTTPS_PORT = 443;
    }

    @Deprecated
    private class HttpResponseHandler implements ResponseHandler<HttpServerResponse> {

        private String originalResourceRequested;

        public HttpResponseHandler(String resourceRequested) {
            originalResourceRequested = resourceRequested;
        }

        @Override
        public HttpServerResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            return new HttpServerResponse(originalResourceRequested, response);
        }
    }
}
