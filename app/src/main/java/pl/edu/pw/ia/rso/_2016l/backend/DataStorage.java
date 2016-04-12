package pl.edu.pw.ia.rso._2016l.backend;

import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.function.Supplier;

public interface DataStorage {

    void save(@NotNull FileId fileId, @NotNull Supplier<InputStream> content);

    boolean isFailed(@NotNull FileId fileId);

    boolean isInProgress(@NotNull FileId fileId);

    @Nullable
    InputStream read(@NotNull FileId fileId);

}
