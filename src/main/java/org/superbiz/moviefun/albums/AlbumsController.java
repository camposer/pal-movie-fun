package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.BufferedInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    private final ClassLoader classLoader = AlbumsController.class.getClassLoader();

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        blobStore.put(new Blob(Long.toString(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType()));
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        String name = Long.toString(albumId);
        Blob blob = blobStore.get(name).orElseGet(() -> {
            try {
                URI defaultCoverURI = classLoader.getResource("default-cover.jpg").toURI();
                Path coverFilePath = Paths.get(defaultCoverURI);

                String contentType = new Tika().detect(coverFilePath);
                return new Blob(name, new FileInputStream(coverFilePath.toFile()), contentType);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        BufferedInputStream bufferedInputStream = new BufferedInputStream(blob.inputStream);
        HttpHeaders headers = createImageHttpHeaders(blob.contentType, bufferedInputStream.getLength());

        return new HttpEntity<>(bufferedInputStream.getByteArray(), headers);
    }

    private HttpHeaders createImageHttpHeaders(String contentType, int imageBytes) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes);
        return headers;
    }

}
