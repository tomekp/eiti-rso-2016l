package pl.edu.pw.ia.rso._2016l.frontend;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Path("/files")
@Slf4j
public class FrontendEndpoint {

    private final AtomicInteger counter = new AtomicInteger(0);

    private final BackendGatewayFactory backendGatewayFactory;

    @Autowired
    public FrontendEndpoint(BackendGatewayFactory backendGatewayFactory) {
        this.backendGatewayFactory = backendGatewayFactory;
    }

    @POST
    @Consumes()
    @Path("/")
    public Response newFile(Map<String, String> params) {
        String urlToDownload = params.get("urlToDownload");
        FileId fileId = new FileId(counter.incrementAndGet());
        List<ProxyingResult> proxyingResults = backendGatewayFactory.forFile(fileId).newFile(urlToDownload);
        log.info("Downloading of file {} from {} scheduled: {}", fileId, urlToDownload, proxyingResults);
        return Response
                .accepted()
                .location(URI.create("/api/files/" + fileId.getId()))
                .entity(new GenericEntity<List<ProxyingResult>>(proxyingResults) {})
                .build();
    }

    @GET
    @Path("/{fileId}")
    public Response getFile(@PathParam("fileId") Long id) {
        FileId fileId = new FileId(id);
        InputStream file = backendGatewayFactory.forFile(fileId).getFile();
        return Response.ok((StreamingOutput) output -> {
            try {
                IOUtils.copy(file, output);
            } finally {
                IOUtils.closeQuietly(file);
                IOUtils.closeQuietly(output);
            }
        }).build();
    }

}

