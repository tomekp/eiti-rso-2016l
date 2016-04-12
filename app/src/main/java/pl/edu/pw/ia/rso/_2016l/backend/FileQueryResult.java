package pl.edu.pw.ia.rso._2016l.backend;

import lombok.Value;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.InputStream;

@Value
public final class FileQueryResult {
    @NotNull
    FileState fileState;
    @Nullable
    InputStream contentStream;

    public enum FileState {
        DOWNLOADED, NOT_FOUND, IN_PROGRESS, FAILED
    }
}
