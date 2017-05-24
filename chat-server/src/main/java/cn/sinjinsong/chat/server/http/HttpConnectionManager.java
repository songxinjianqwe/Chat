package cn.sinjinsong.chat.server.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * Created by SinjinSong on 2017/5/24.
 */
@Component("httpConnectionManager")
public class HttpConnectionManager {

    private PoolingHttpClientConnectionManager manager = null;
    
    @PostConstruct
    public void init() {
        LayeredConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        manager.setMaxTotal(200);
        manager.setDefaultMaxPerRoute(20);
    }

    public CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .build();

        return httpClient;
    }

    public InputStream openStream(String url) {
        try {
            CloseableHttpClient client = getHttpClient();
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = client.execute(httpget);
            return response.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean copyStream(String url,OutputStream os) {
        try {
            CloseableHttpClient client = getHttpClient();
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = client.execute(httpget);
            response.getEntity().writeTo(os);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
}
