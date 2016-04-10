package pl.edu.pw.ia.rso._2016l.backend;

import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;

@Component
@Path("/files")
public class BackendEndpoint {

    @PUT
    @Consumes()
    @Path("/{fileId}")
    public Response newFile(@PathParam("fileId") String id, Map<String, String> params) {
        return Response
                .accepted()
                .location(URI.create("http://example.com/files/1"))
                .entity(MessageFormat.format("Downloading \"{0}\" as \"{1}\"", params.get("urlToDownload"), id))
                .build();
    }

    @GET
    @Path("{fileId}")
    public String getFile(@PathParam("fileId") String id) {
        return "Hello";
    }

}
