package pl.edu.pw.ia.rso._2016l.common;

import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;

public final class HttpStatusCodeAdapter implements Response.StatusType {
    private final HttpStatus httpStatus;

    public HttpStatusCodeAdapter(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public int getStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Response.Status.Family getFamily() {
        switch (httpStatus.series()) {
            case INFORMATIONAL:
                return Response.Status.Family.INFORMATIONAL;
            case SUCCESSFUL:
                return Response.Status.Family.SUCCESSFUL;
            case REDIRECTION:
                return Response.Status.Family.REDIRECTION;
            case CLIENT_ERROR:
                return Response.Status.Family.CLIENT_ERROR;
            case SERVER_ERROR:
                return Response.Status.Family.SERVER_ERROR;
        }
        return Response.Status.Family.OTHER;
    }

    @Override
    public String getReasonPhrase() {
        return httpStatus.getReasonPhrase();
    }
}
