package net.oauth.client.httpclient3;

import java.net.URL;
import org.apache.commons.httpclient.HttpClient;

public interface HttpClientPool {
   HttpClient getHttpClient(URL var1);
}
