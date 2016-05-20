package pl.edu.pw.ia.rso._2016l.frontend;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.backend.BackendEndpoint;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(FrontendEndpoint.class);
    }

}
