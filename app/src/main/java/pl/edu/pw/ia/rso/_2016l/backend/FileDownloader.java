package pl.edu.pw.ia.rso._2016l.backend;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
@Slf4j
public class FileDownloader {

    private final DataStorage dataStorage;
    private final CloseableHttpClient httpClient;

    @Autowired
    public FileDownloader(DataStorage dataStorage, CloseableHttpClient httpClient) {
        this.dataStorage = dataStorage;
        this.httpClient = httpClient;
    }

    @Async
    public Future<Void> downloadAsynchronously(URL urlToDownload, FileId fileId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            dataStorage.save(fileId, () -> {
                log.debug("Starting download of {}", urlToDownload);
                try {
                    CloseableHttpResponse response = httpClient.execute(new HttpGet(urlToDownload.toURI()));
                    log.info("Response for {} is: {}", urlToDownload, response);
                    HttpEntity entity = response.getEntity();
                    return new DelegatingInputStream(entity.getContent()) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            response.close();
                        }
                    };
                } catch (Exception e) {
                    future.completeExceptionally(e);
                    throw new RuntimeException(e);
                } finally {
                    future.complete(null);
                }
            });
        }).start();
        return future;
    }
}

@RequiredArgsConstructor
class DelegatingInputStream extends InputStream {
    @Delegate
    private final InputStream delegate;
}
