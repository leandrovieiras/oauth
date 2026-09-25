package net.oauth.client.httpclient3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.oauth.OAuth;
import net.oauth.client.ExcerptInputStream;
import net.oauth.http.HttpResponseMessage;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

public class HttpMethodResponse extends HttpResponseMessage {
   private final HttpMethod method;
   private final byte[] requestBody;
   private final String requestEncoding;

   public HttpMethodResponse(HttpMethod method, byte[] requestBody, String requestEncoding) throws IOException {
      super(method.getName(), new URL(method.getURI().toString()));
      this.method = method;
      this.requestBody = requestBody;
      this.requestEncoding = requestEncoding;
      this.headers.addAll(this.getHeaders());
   }

   public int getStatusCode() {
      return this.method.getStatusCode();
   }

   public InputStream openBody() throws IOException {
      return this.method.getResponseBodyAsStream();
   }

   private List<Map.Entry<String, String>> getHeaders() {
      List<Map.Entry<String, String>> headers = new ArrayList();
      Header[] allHeaders = this.method.getResponseHeaders();
      if (allHeaders != null) {
         for(Header header : allHeaders) {
            headers.add(new OAuth.Parameter(header.getName(), header.getValue()));
         }
      }

      return headers;
   }

   public void dump(Map<String, Object> into) throws IOException {
      super.dump(into);
      StringBuilder request = new StringBuilder(this.method.getName());
      request.append(" ").append(this.method.getPath());
      String query = this.method.getQueryString();
      if (query != null && query.length() > 0) {
         request.append("?").append(query);
      }

      request.append("\r\n");

      Header[] var7;
      for(Header header : var7 = this.method.getRequestHeaders()) {
         request.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
      }

      request.append("\r\n");
      if (this.requestBody != null) {
         request.append(new String(this.requestBody, this.requestEncoding));
      }

      into.put("HTTP request", request.toString());
      request = new StringBuilder();
      query = this.method.getStatusLine().toString();
      request.append(query).append("\r\n");

      for(Header header : var7 = this.method.getResponseHeaders()) {
         String name = header.getName();
         query = header.getValue();
         request.append(name).append(": ").append(query).append("\r\n");
      }

      request.append("\r\n");
      if (this.body != null) {
         request.append(new String(((ExcerptInputStream)this.body).getExcerpt(), this.getContentCharset()));
      }

      into.put("HTTP response", request.toString());
   }
}
