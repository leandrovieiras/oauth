package net.oauth.client.httpclient3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import net.oauth.client.ExcerptInputStream;
import net.oauth.http.HttpClient;
import net.oauth.http.HttpMessage;
import net.oauth.http.HttpResponseMessage;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class HttpClient3 implements HttpClient {
   private final HttpClientPool clientPool;
   private static final HttpClientPool SHARED_CLIENT = new SingleClient();

   public HttpClient3() {
      this(SHARED_CLIENT);
   }

   public HttpClient3(HttpClientPool clientPool) {
      this.clientPool = clientPool;
   }

   public HttpResponseMessage execute(HttpMessage request, Map<String, Object> parameters) throws IOException {
      String method = request.method;
      String url = request.url.toExternalForm();
      InputStream body = request.getBody();
      boolean isDelete = "DELETE".equalsIgnoreCase(method);
      boolean isPost = "POST".equalsIgnoreCase(method);
      boolean isPut = "PUT".equalsIgnoreCase(method);
      byte[] excerpt = (byte[])null;
      HttpMethod httpMethod;
      if (!isPost && !isPut) {
         if (isDelete) {
            httpMethod = new DeleteMethod(url);
         } else {
            httpMethod = new GetMethod(url);
         }
      } else {
         EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod)(isPost ? new PostMethod(url) : new PutMethod(url));
         if (body != null) {
            ExcerptInputStream e = new ExcerptInputStream(body);
            String length = request.removeHeaders("Content-Length");
            entityEnclosingMethod.setRequestEntity(length == null ? new InputStreamRequestEntity(e) : new InputStreamRequestEntity(e, Long.parseLong(length)));
            excerpt = e.getExcerpt();
         }

         httpMethod = entityEnclosingMethod;
      }

      for(Map.Entry<String, Object> p : parameters.entrySet()) {
         String name = (String)p.getKey();
         String value = p.getValue().toString();
         if ("followRedirects".equals(name)) {
            httpMethod.setFollowRedirects(Boolean.parseBoolean(value));
         } else if ("readTimeout".equals(name)) {
            httpMethod.getParams().setIntParameter("http.socket.timeout", Integer.parseInt(value));
         }
      }

      for(Map.Entry<String, String> header : request.headers) {
         httpMethod.addRequestHeader((String)header.getKey(), (String)header.getValue());
      }

      org.apache.commons.httpclient.HttpClient client = this.clientPool.getHttpClient(new URL(httpMethod.getURI().toString()));
      client.executeMethod(httpMethod);
      return new HttpMethodResponse(httpMethod, excerpt, request.getContentCharset());
   }

   private static class SingleClient implements HttpClientPool {
      private final org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();

      SingleClient() {
         this.client.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
      }

      public org.apache.commons.httpclient.HttpClient getHttpClient(URL server) {
         return this.client;
      }
   }
}
