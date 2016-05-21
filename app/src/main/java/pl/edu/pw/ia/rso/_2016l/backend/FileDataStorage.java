package pl.edu.pw.ia.rso._2016l.backend;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pw.ia.rso._2016l.common.FileId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

@Slf4j
@Component
public class FileDataStorage implements DataStorage {

    private final String storageDirectory;

    @Autowired
    public FileDataStorage(@Value("${backend.storageDirectory}") String storageDirectory) {
        this.storageDirectory = StringUtils.isEmpty(storageDirectory) ? tempDirectory() : storageDirectory;
        new File(this.storageDirectory).mkdirs();
        log.info("Using {} as storage.", this.storageDirectory);
    }

    private static String tempDirectory() {
        return Files.createTempDir().getAbsolutePath();
    }

    @Override
    public void save(@NotNull FileId fileId, @NotNull Supplier<InputStream> content) {
        File file = fileForFileId(fileId);
        log.info("Saving content of {} to {}", fileId, file.getAbsolutePath());
        try {
            boolean isNewFile = file.createNewFile();
            if (!isNewFile) {
                throw new FileAlreadyExistsException(file.getAbsolutePath(), fileId.toString(), "file already exists");
            }
            FileUtils.copyInputStreamToFile(content.get(), file);
        } catch (FileAlreadyExistsException e) {
            log.info("File " + file.getAbsolutePath() + " already exists. " + fileId + " is already downloaded/in progress.", e);
        } catch (Exception e) {
            log.error("Failed to download " + fileId, e);
            markFile(fileId, ".failed");
        }
        markFile(fileId, ".done");
    }

    private void markFile(FileId fileId, String postfix) {
        File markFile = new File(pathForFileId(fileId).toString() + postfix);
        log.debug("Marking file {} ({}) as {}", fileId, markFile.getAbsolutePath(), postfix);
        try {
            if (!markFile.createNewFile()) {
                throw new IllegalStateException("Unable to mark " + fileId + " with " + postfix);
            }
        } catch (Exception e) {
            log.error("Error while marking file: " + markFile.getAbsolutePath(), e);
        }
    }


    @Override
    public boolean isFailed(@NotNull FileId fileId) {
        return exists(fileId, ".failed");
    }

    @Override
    public boolean isInProgress(@NotNull FileId fileId) {
        return exists(fileId, "");
    }

    private boolean isDone(@NotNull FileId fileId) {
        return exists(fileId, ".done");
    }

    private boolean exists(FileId fileId, String postfix) {
        File markFile = new File(pathForFileId(fileId).toString() + postfix);
        return markFile.exists();
    }

    private File fileForFileId(@NotNull FileId fileId) {
        return pathForFileId(fileId).toFile();
    }

    private Path pathForFileId(@NotNull FileId fileId) {
        return Paths.get(storageDirectory, String.valueOf(fileId.getId()));
    }

    @Nullable
    @Override
    public InputStream read(@NotNull FileId fileId) {
        File file = fileForFileId(fileId);
        log.info("Reading content of {} from {}", fileId, file.getAbsolutePath());
        if (isDone(fileId) && !isFailed(fileId)) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}
