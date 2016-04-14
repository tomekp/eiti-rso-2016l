package pl.edu.pw.ia.rso._2016l.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
public class DataManagerImpl implements DataManager {

    private final DataStorage dataStorage;
    private final FileDownloader fileDownloader;

    @Autowired
    public DataManagerImpl(DataStorage dataStorage, FileDownloader fileDownloader) {
        this.dataStorage = dataStorage;
        this.fileDownloader = fileDownloader;
    }

    @Override
    public FileQueryResult queryFile(@NotNull FileId fileId) {
        InputStream content = dataStorage.read(fileId);

        if (content != null) {
            return new FileQueryResult(FileQueryResult.FileState.DOWNLOADED, content);
        }

        if (dataStorage.isFailed(fileId)) {
            return new FileQueryResult(FileQueryResult.FileState.FAILED, null);
        }

        if (dataStorage.isInProgress(fileId)) {
            return new FileQueryResult(FileQueryResult.FileState.IN_PROGRESS, null);
        }


        return new FileQueryResult(FileQueryResult.FileState.NOT_FOUND, null);
    }

    @Override
    public void scheduleDownload(String urlToDownload, FileId fileId) {
        //TODO jakieś weryfikacje?
        try {
            fileDownloader.downloadAsynchronously(new URL(urlToDownload), fileId);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
