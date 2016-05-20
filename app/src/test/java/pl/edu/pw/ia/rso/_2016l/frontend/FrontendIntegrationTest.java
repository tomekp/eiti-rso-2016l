package pl.edu.pw.ia.rso._2016l.frontend;

import com.github.dreamhead.moco.*;
import com.github.dreamhead.moco.dumper.HttpRequestDumper;
import com.github.dreamhead.moco.dumper.HttpResponseDumper;
import com.github.dreamhead.moco.monitor.Slf4jMonitor;
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.MocoRequestHit.times;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RsoFrontendApp.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0", "backendEndpoints=http://localhost:8131/apiA;http://localhost:8131/apiB/"})
public class FrontendIntegrationTest {

    @Value("${local.server.port}")
    int port;
    private WebTarget target;
    private Client client;

    private RequestHit mocoRequestHit;
    private HttpServer mocoServer;
    private Runner mocoRunner;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient();
        target = client.target(UriBuilder.fromUri("http://localhost/api/").port(port));

        mocoRequestHit = MocoRequestHit.requestHit();
        mocoServer = Moco.httpServer(8131, new Slf4jMonitor(new HttpRequestDumper(), new HttpResponseDumper()), mocoRequestHit);
        mocoRunner = Runner.runner(mocoServer);
        mocoRunner.start();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        mocoRunner.stop();
    }

    @Test
    public void shouldAssignIdAndPassDownloadOrderToBackendNodes() throws Exception {
        String urlToDownload = UriBuilder.fromUri("http://localhost/randomFile.bin").port(port).build().toString();
        mocoServer.put(by(uri("/apiA/files/1"))).response("Server A is downloading.");
        mocoServer.put(by(uri("/apiB/files/1"))).response("Server B is downloading.");

        Response orderDownloadResponse = target.path("files/")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity("{\"urlToDownload\": \"" + urlToDownload + "\"}", MediaType.APPLICATION_JSON_TYPE));

        Assert.assertEquals(HttpStatus.ACCEPTED.value(), orderDownloadResponse.getStatus());
        Assert.assertEquals(UriBuilder.fromUri(target.getUri()).path("/files/1").build(), orderDownloadResponse.getLocation());

        String expectedRequestBody = "{\"urlToDownload\": \"http://localhost:" + port + "/randomFile.bin\"}";
        mocoRequestHit.verify(and(by(method("PUT")), by(uri("/apiA/files/1")), by(expectedRequestBody)), times(1));
        mocoRequestHit.verify(and(by(method("PUT")), by(uri("/apiB/files/1")), by(expectedRequestBody)), times(1));
    }

    @Test
    public void shouldDownloadFileFromBackendNode() throws Exception {
        String expectedContent = "Some content.";
        mocoServer.get(by(uri("/apiA/files/2"))).response(expectedContent);
        mocoServer.get(by(uri("/apiB/files/2"))).response(expectedContent);

        Response response = target.path("files/2")
                .request()
                .get();

        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals(expectedContent, response.readEntity(String.class));

        mocoRequestHit.verify(by(method("GET")), times(1));
    }

}
