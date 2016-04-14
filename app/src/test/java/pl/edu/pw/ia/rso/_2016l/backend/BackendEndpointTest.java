package pl.edu.pw.ia.rso._2016l.backend;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class BackendEndpointTest {

    static final FileId TEST_FILE_ID = new FileId(123L);
    DataManager dataManager;
    BackendEndpoint backendEndpoint;

    @Before
    public void setUp() throws Exception {
        this.dataManager = Mockito.mock(DataManager.class);
        this.backendEndpoint = new BackendEndpoint(this.dataManager);
    }

    @Test
    public void shouldOrderDownloadingAfterReceivingOrder() {
        backendEndpoint.newFile(TEST_FILE_ID.getId(), ImmutableMap.of("urlToDownload", "http://example.com/testFile"));

        Mockito.verify(dataManager, Mockito.times(1))
                .scheduleDownload(Mockito.eq("http://example.com/testFile"), Mockito.eq(TEST_FILE_ID));
    }

    @Test
    public void getFileShouldRespond404WhenNotFound() {
        Mockito.when(dataManager.queryFile(Mockito.eq(TEST_FILE_ID)))
                .thenReturn(new FileQueryResult(FileQueryResult.FileState.NOT_FOUND, null));

        Response response = backendEndpoint.getFile(TEST_FILE_ID.getId());

        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getFileShouldRespond421WhenIdNotValidForNode() {
        //TODO
    }

    @Test
    public void getFileShouldRespond504WhenDownloadInProgress() {
        Mockito.when(dataManager.queryFile(Mockito.eq(TEST_FILE_ID)))
                .thenReturn(new FileQueryResult(FileQueryResult.FileState.IN_PROGRESS, null));

        Response response = backendEndpoint.getFile(TEST_FILE_ID.getId());

        Assert.assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), response.getStatus());
    }

    @Test
    public void getFileShouldRespond424WhenDownloadFailed() {
        Mockito.when(dataManager.queryFile(Mockito.eq(TEST_FILE_ID)))
                .thenReturn(new FileQueryResult(FileQueryResult.FileState.FAILED, null));

        Response response = backendEndpoint.getFile(TEST_FILE_ID.getId());

        Assert.assertEquals(HttpStatus.FAILED_DEPENDENCY.value(), response.getStatus());
    }

    @Test
    public void getFileShouldRespond200WithFileContentWhenFileIsDownloaded() throws Exception {
        ByteArrayInputStream data = new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));
        Mockito.when(dataManager.queryFile(Mockito.eq(TEST_FILE_ID)))
                .thenReturn(new FileQueryResult(FileQueryResult.FileState.DOWNLOADED, data));

        Response response = backendEndpoint.getFile(TEST_FILE_ID.getId());

        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        InputStream responseStream = (InputStream) response.getEntity();
        Assert.assertNotNull(responseStream);
        List<String> resultLines = IOUtils.readLines(responseStream, Charsets.UTF_8);

        Assert.assertEquals(1, resultLines.size());
        Assert.assertEquals("test", resultLines.get(0));
    }

}
