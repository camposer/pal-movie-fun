package org.superbiz.moviefun.blobstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStream {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public BufferedInputStream(InputStream inputStream) throws IOException {
        createByteArrayOutputStream(inputStream);
    }

    private void createByteArrayOutputStream(InputStream inputStream) throws IOException {
        int nRead;
        while ((nRead = inputStream.read()) != -1) {
            baos.write(nRead);
        }
        baos.flush();
    }

    public int getLength() {
        return getByteArray().length;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getByteArray());
    }

    public byte[] getByteArray() { return baos.toByteArray(); }
}
