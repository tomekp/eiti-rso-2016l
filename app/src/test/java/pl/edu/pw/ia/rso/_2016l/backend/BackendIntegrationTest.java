package pl.edu.pw.ia.rso._2016l.backend;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RsoBackendApp.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0", "backend.storageDirectory=", "httpClient.timeout=2000"})
public class BackendIntegrationTest {

    @Value("${local.server.port}")
    int port;
    private WebTarget target;
    private Client client;

    @Before
    public void setUp() throws Exception {
        this.client = ClientBuilder.newClient();
        this.target = client.target(UriBuilder.fromUri("http://localhost/api/").port(port));
    }

    @After
    public void tearDown() throws Exception {
        this.client.close();
    }

    @Test
    public void shouldDownloadAndShareFiles() throws Exception {
        String urlToDownload = UriBuilder.fromUri("http://localhost/randomFile.bin").port(port).build().toString();
        Response orderDownloadResponse = target.path("files/1")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity("{\"urlToDownload\": \"" + urlToDownload + "\"}", MediaType.APPLICATION_JSON_TYPE));

        Assert.assertEquals(HttpStatus.ACCEPTED.value(), orderDownloadResponse.getStatus());
        Assert.assertEquals(UriBuilder.fromUri(target.getUri()).path("/files/1").build(), orderDownloadResponse.getLocation());


        Response fileContentResponse = Awaitility.await()
                .atMost(Duration.ONE_SECOND)
                .until(() -> target.path("files/1")
                        .request()
                        .get(), ResponseMatcher.statusIs(HttpStatus.OK));

        Assert.assertEquals(HttpStatus.OK.value(), fileContentResponse.getStatus());
        InputStream fileContentStream = fileContentResponse.readEntity(InputStream.class);


        byte[] expectedSha512 = DigestUtils.sha512(getClass().getResourceAsStream("/static/randomFile.bin"));
        byte[] actualSha512 = DigestUtils.sha512(fileContentStream);
        Assert.assertArrayEquals(expectedSha512, actualSha512);
    }

    @Test(timeout = 10000L)
    public void shouldDownloadFilesAsynchronouslyAndMarkAsFailed() throws Exception {
        String urlToDownload = "http://1.1.1.1/randomFile";
        Response orderDownloadResponse = target.path("files/2")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity("{\"urlToDownload\": \"" + urlToDownload + "\"}", MediaType.APPLICATION_JSON_TYPE));

        Assert.assertEquals(HttpStatus.ACCEPTED.value(), orderDownloadResponse.getStatus());

        Awaitility.await("file is in progress")
                .atMost(Duration.ONE_SECOND)
                .until(() -> target.path("files/2")
                        .request()
                        .get(), ResponseMatcher.statusIs(HttpStatus.GATEWAY_TIMEOUT));

        Awaitility.await("file is failed")
                .atMost(Duration.FIVE_SECONDS)
                .until(() -> target.path("files/2")
                        .request()
                        .get(), ResponseMatcher.statusIs(HttpStatus.FAILED_DEPENDENCY));
    }

}
