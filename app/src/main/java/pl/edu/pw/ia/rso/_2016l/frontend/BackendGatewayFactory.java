package pl.edu.pw.ia.rso._2016l.frontend;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BackendGatewayFactory {

    private final Client client = ClientBuilder.newClient();
    private final Random random = new Random();
    private final List<String> backendEndpoints;
    private final long backendTimeoutSeconds;

    @Autowired
    public BackendGatewayFactory(@Value("${backendEndpoints}") String backendEndpoints, @Value("${backendTimeoutSeconds:5}") long backendTimeoutSeconds) {
        this.backendEndpoints = parseBackendAddressesConfig(backendEndpoints);
        this.backendTimeoutSeconds = backendTimeoutSeconds;
    }

    private List<String> parseBackendAddressesConfig(String addresses) {
        return Collections.unmodifiableList(
                Arrays.asList(addresses.split(";"))
                        .stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList())
        );
    }

    public BackendGateway forFile(FileId fileId) {
        return new BackendGateway(backendEndpoints, fileId);
    }

    @lombok.Value
    private static class RequestedDownload {
        String server;
        Future<Response> responseFuture;
    }

    public class BackendGateway {

        private final List<String> backendUrls;
        private final FileId fileId;

        BackendGateway(List<String> backendUrls, FileId fileId) {
            this.backendUrls = backendUrls;
            this.fileId = fileId;
        }

        public List<ProxyingResult> newFile(String urlToDownload) {
            return backendUrls.stream()
                    .map(backendUrl -> makeDownloadRequest(backendUrl, urlToDownload))
                    .map(this::toProxyingResult)
                    .collect(Collectors.toList());
        }

        private RequestedDownload makeDownloadRequest(String backendUrl, String urlToDownload) {
            WebTarget target = prepareWebTarget(backendUrl);
            Future<Response> responseFuture = target.request()
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .buildPut(Entity.entity("{\"urlToDownload\": \"" + urlToDownload + "\"}", MediaType.APPLICATION_JSON_TYPE))
                    .submit();
            return new RequestedDownload(target.getUri().toString(), responseFuture);

        }

        private WebTarget prepareWebTarget(String backendUrl) {
            return client.target(UriBuilder.fromUri(backendUrl)).path("/files/").path(String.valueOf(fileId.getId()));
        }

        private ProxyingResult toProxyingResult(RequestedDownload requestedDownload) {
            try {
                Response response = requestedDownload.getResponseFuture().get(backendTimeoutSeconds, TimeUnit.SECONDS);
                String summary = String.format("%s(%d)", response.getStatusInfo().toString(), response.getStatus());
                String details = response.readEntity(String.class);
                return new ProxyingResult(requestedDownload.getServer(), true, summary, details);
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                return new ProxyingResult(requestedDownload.getServer(), false, e.getMessage(), ExceptionUtils.getStackTrace(e));
            }
        }

        public InputStream getFile() {
            String backendUrl = backendUrls.get(random.nextInt(backendUrls.size()));
            WebTarget target = prepareWebTarget(backendUrl);
            Response response = target.request().get();
            return response.readEntity(InputStream.class);
        }
    }

}
