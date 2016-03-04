package com.voxmec.vdk.test;

import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.http.HttpService;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpTest {
    @Test
    public void testHttp_GetOk() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setBody("{dialog}"));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        Response rsp = http.get(url.toString(), null);
        assertEquals(rsp.body().string(), "{dialog}");

        svr.shutdown();
    }

    @Test
    public void testHttp_GetWithParams() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setBody("{dialog}"));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        Map<String, String> params = new HashMap<String, String>();
        params.put("p0", "hello");
        params.put("p1", "test");
        Response rsp = http.get(url.toString(), params);

        assertEquals(rsp.body().string(), "{dialog}");

        RecordedRequest req = svr.takeRequest();
       assertTrue(req.getPath().startsWith("/test?"));
        assertTrue(req.getPath().contains("p0=hello"));
        assertTrue(req.getPath().contains("p1=test"));
        svr.shutdown();
    }

    @Test(expected = VoxException.class)
    public void testHttp_GetFailed() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setResponseCode(404));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        http.get(url.toString(), null);
    }


    @Test
    public void testHttp_PostOk() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setBody("{dialog}"));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        Response rsp = http.post(url.toString(), null);
        assertEquals(rsp.body().string(), "{dialog}");

        svr.shutdown();
    }

    @Test
    public void testHttp_PostWithParams() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setBody("{dialog}"));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        Map<String, String> params = new HashMap<String, String>();
        params.put("p0", "hello");
        params.put("p1", "test");
        Response rsp = http.post(url.toString(), params);

        assertEquals(rsp.body().string(), "{dialog}");

        RecordedRequest req = svr.takeRequest();
        assertEquals(req.getPath(), "/test");
        assertEquals(req.getMethod(), "POST");
        String bod = req.getBody().readUtf8Line();
        assertTrue(bod.contains("p0=hello"));
        assertTrue(bod.contains("p1=test"));
        assertTrue(bod.contains("&"));

        svr.shutdown();
    }

    @Test(expected = VoxException.class)
    public void testHttp_PostFailed() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setResponseCode(404));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        http.post(url.toString(), null);
    }

    @Test
    public void testHttp_ResponseAsString() throws Exception {
        MockWebServer svr = new MockWebServer();
        svr.enqueue(new MockResponse().setBody("{dialog}"));
        svr.start();
        HttpUrl url = svr.url("/test");

        HttpService http = new HttpService();
        Response rsp = http.post(url.toString(), null);
        assertEquals(HttpService.ResponseAsString(rsp), "{dialog}");

        svr.shutdown();
    }
}