package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.String.format;

public class FileStore implements BlobStore {

    private final ClassLoader classLoader = FileStore.class.getClassLoader();

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = getCoverFile(blob.name);
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        writeFile(blob.inputStream, targetFile);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            File coverFile = getCoverFile(name);
            Path coverFilePath = null;

            if (coverFile.exists()) {
                coverFilePath = coverFile.toPath();
            } else {
                URI defaultCoverURI = classLoader.getResource("default-cover.jpg").toURI();
                coverFilePath = Paths.get(defaultCoverURI);
            }

            String contentType = new Tika().detect(coverFilePath);
            return Optional.of(new Blob(name, new FileInputStream(coverFilePath.toFile()), contentType));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }

    private File getCoverFile(String albumName) {
        String coverFileName = format("covers/%s", albumName);
        return new File(coverFileName);
    }

    private void writeFile(InputStream inputStream, File targetFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            int nRead;
            while ((nRead = inputStream.read()) != -1) {
                fos.write(nRead);
            }
        }
    }

}
