package pl.edu.pw.ia.rso._2016l.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;
import pl.edu.pw.ia.rso._2016l.common.HttpStatusCodeAdapter;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;

@Component
@Path("/files")
@Slf4j
public class BackendEndpoint {

    private final DataManager dataManager;

    @Autowired
    public BackendEndpoint(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @PUT
    @Consumes()
    @Path("/{fileId}")
    public Response newFile(@PathParam("fileId") Long id, Map<String, String> params) {
        String urlToDownload = params.get("urlToDownload");
        FileId fileId = new FileId(id);
        dataManager.scheduleDownload(urlToDownload, fileId);
        log.info("Downloading of file {} from {} scheduled.", fileId, urlToDownload);
        return Response
                .accepted()
                .location(URI.create("/api/files/" + id))
                .entity(MessageFormat.format("Downloading \"{0}\" as \"{1}\"", urlToDownload, id))
                .build();
    }

    @GET
    @Path("/{fileId}")
    public Response getFile(@PathParam("fileId") Long id) {
        FileId fileId = new FileId(id);
        FileQueryResult queryResult = dataManager.queryFile(fileId);
        log.debug("Result of query for {} is: {}", fileId, queryResult);

        Response.StatusType status = Response.Status.INTERNAL_SERVER_ERROR;

        switch (queryResult.getFileState()) {
            case NOT_FOUND:
                status = Response.Status.NOT_FOUND;
                break;
            case IN_PROGRESS:
                status = Response.Status.GATEWAY_TIMEOUT;
                break;
            case FAILED:
                status = new HttpStatusCodeAdapter(HttpStatus.FAILED_DEPENDENCY);
                break;
            case DOWNLOADED:
                return Response
                        .status(Response.Status.OK)
                        .entity(queryResult.getContentStream())
                        .build();
        }

        return Response
                .status(status)
                .build();
    }

}

