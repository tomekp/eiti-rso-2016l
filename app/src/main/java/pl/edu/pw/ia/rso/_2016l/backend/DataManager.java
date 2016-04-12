package pl.edu.pw.ia.rso._2016l.backend;

import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.validation.constraints.NotNull;

public interface DataManager {
    @NotNull
    FileQueryResult queryFile(@NotNull FileId fileId);

    void scheduleDownload(String urlToDownload, FileId fileId);
}
