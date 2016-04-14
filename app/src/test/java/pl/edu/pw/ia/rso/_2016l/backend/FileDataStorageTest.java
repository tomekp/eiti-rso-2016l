package pl.edu.pw.ia.rso._2016l.backend;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class FileDataStorageTest {

    private File storageDirectory;

    @Before
    public void setUp() throws Exception {
        storageDirectory = Files.createTempDir();
        storageDirectory.mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        storageDirectory.delete();
    }

    @Test
    public void shouldStoreData() throws Exception {
        FileId fileId = new FileId(1);
        byte[] dataBytes = "hello!".getBytes(Charsets.UTF_8);
        InputStream dataStream = new ByteArrayInputStream(dataBytes);

        FileDataStorage fileDataStorage = new FileDataStorage(storageDirectory.getAbsolutePath());

        fileDataStorage.save(fileId, () -> dataStream);

        InputStream resultStream = fileDataStorage.read(fileId);
        Assert.assertNotNull(resultStream);

        List<String> resultLines = IOUtils.readLines(resultStream, Charsets.UTF_8);

        Assert.assertEquals(1, resultLines.size());
        Assert.assertEquals("hello!", resultLines.get(0));
    }
}
