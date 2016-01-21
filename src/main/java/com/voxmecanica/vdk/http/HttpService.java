package com.voxmecanica.vdk.http;

import android.net.http.AndroidHttpClient;
import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.logging.Logger;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/***
 * HttpService manages http interaction with server.
 */
public class HttpService {

    public static class Config {
        public static final String HTTP_USER_AGENT = "VoxMecanica/(Voice Client)";
        public static final String DEFAULT_CHARCSET = "UTF-8";
        public static final String DEFAULT_SUBMIT_METHOD = "GET";
        public static final int DEFAULT_HTTP_PORT = 80;
        public static final int DEFAULT_HTTPS_PORT = 443;
    }

    private Logger LOG = new Logger("HttpService");
    private boolean started;
    private AndroidHttpClient httpClient;
    private ClientConnectionManager connectionMgr;
    private ExecutorService threadPool;

    public HttpService() {
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
