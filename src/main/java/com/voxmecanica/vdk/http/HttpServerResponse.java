package com.voxmecanica.vdk.http;

import com.voxmecanica.vdk.VoxException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * *
 * A class that represents an HTTP Server response to a client request.
 *
 * @author vladimir vivien
 *
 */
public class HttpServerResponse {

    private String requestedResource;
    private HttpResponse response;
    private byte[] content;

    public HttpServerResponse(String requestedPath, HttpResponse httpResponse) {
        if (httpResponse == null) {
            throw new IllegalArgumentException("HttpResponse connot be null for HttpClientResponse constructor.");
        }
        requestedResource = requestedPath;
        response = httpResponse;
        content = this.getContent(); // grab content rightaway. 
    }

    public String getRequestedResource() {
        return requestedResource;
    }

    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    public long getContentLength() {
        return (response.getEntity() != null) ? response.getEntity().getContentLength() : 0;
    }

    public String getContentType() {
        return (response.getEntity() != null && response.getEntity().getContentType() != null)
                ? response.getEntity().getContentType().getValue()
                : null;
    }

    public String getContentEncoding() {
        return (response.getEntity() != null && response.getEntity().getContentEncoding() != null)
                ? response.getEntity().getContentEncoding().getValue()
                : null;
    }

    public Header[] getHttpHeaders() {
        return response.getAllHeaders();
    }

    public Header[] getHttpHeadersByName(String name) {
        return response.getHeaders(name);
    }

    public byte[] getContent() {
        if (content != null) {
            return content;
        }
        try {
            content = (response.getEntity() != null) ? EntityUtils.toByteArray(response.getEntity()) : new byte[0];
            return content;
        } catch (IOException ex) {
            throw new VoxException(ex);
        }
    }
}
