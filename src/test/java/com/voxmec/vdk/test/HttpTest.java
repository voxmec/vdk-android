package com.voxmec.vdk.test;

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
import static org.junit.Assert.assertNotNull;

public class HttpTest {
   @Test
    public void testHttp_GetOk() throws Exception{
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
   public void testHttp_GetWithParams() throws Exception{
      MockWebServer svr = new MockWebServer();
      svr.enqueue(new MockResponse().setBody("{dialog}"));
      svr.start();
      HttpUrl url = svr.url("/test");

      HttpService http = new HttpService();
      Map<String,String> params = new HashMap<String, String>();
      params.put("p0", "hello");
      params.put("p1", "test");
      Response rsp = http.get(url.toString(), params);

      assertEquals(rsp.body().string(), "{dialog}");

      RecordedRequest req = svr.takeRequest();
      assertEquals(req.getPath(), "/test?p0=hello&p1=test");
      svr.shutdown();
   }
}