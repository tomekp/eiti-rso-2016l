package pl.edu.pw.ia.rso._2016l.backend;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import pl.edu.pw.ia.rso._2016l.RsoBackendApp;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RsoBackendApp.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class BackendEndpointTest {

    @Value("${local.server.port}")
    int port;
    private WebTarget target;
    private Client client;

    @Before
    public void setUp() {
        client = ClientBuilder.newClient();
        this.target = client
                .target(UriBuilder.fromUri("http://localhost").port(port));
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void shouldAcceptDownloadOrder() {
        Response response = target.path("files/1")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity("{\"urlToDownload\": \"http://www.wp.pl\"}", MediaType.APPLICATION_JSON_TYPE));

        Assert.assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus());
        //FIXME Assert.assertEquals(UriBuilder.fromUri(target.getUri()).path("/files/1").build(), response.getLocation());
        Assert.assertEquals("Downloading \"http://www.wp.pl\" as \"1\"", response.readEntity(String.class));
    }

    @Test
    public void getFileShouldRespond404WhenNotFound() {
        //TODO
    }

    @Test
    public void getFileShouldRespond421WhenIdNotValidForNode() {
        //TODO
    }

    @Test
    public void getFileShouldRespond504WhenDownloadInProgress() {
        //TODO
    }

    @Test
    public void getFileShouldRespond424WhenDownloadFailed() {
        //TODO
    }

    @Test
    public void getFileShouldRespond200WithFileContentWhenFileIsDownloaded() {
        //TODO
    }

}
