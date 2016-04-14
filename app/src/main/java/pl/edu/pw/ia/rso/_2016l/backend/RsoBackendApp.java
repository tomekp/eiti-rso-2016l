package pl.edu.pw.ia.rso._2016l.backend;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RsoBackendApp {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(RsoBackendApp.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(environment.getProperty("poolMaxTotal", Integer.class, 10));
        return connectionManager;
    }

    @Bean
    @Autowired
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager, @Value("${httpClient.timeout:30000}") int timeout) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(timeout)
                        .setConnectTimeout(timeout)
                        .setConnectionRequestTimeout(timeout)
                        .build())
                .build();
    }

}
