package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.*;
import java.util.Iterator;
import java.util.Optional;

import static java.lang.String.format;

public class S3Store implements BlobStore {
    private final AmazonS3Client s3Client;
    private final String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        BufferedInputStream buffered = new BufferedInputStream(blob.inputStream);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        objectMetadata.setContentLength(buffered.getLength());

        s3Client.putObject(photoStorageBucket, getCoverName(blob.name), buffered.getInputStream(), objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            String qualifiedName = getCoverName(name);

            S3Object s3Object = s3Client.getObject(photoStorageBucket, qualifiedName);
            Blob blob = new Blob(name, s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentType());

            return Optional.of(blob);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        ObjectListing objectListing = s3Client.listObjects(photoStorageBucket);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                s3Client.deleteObject(photoStorageBucket, objIter.next().getKey());
            }

            // If the bucket contains many objects, the listObjects() call
            // might not return all of the objects in the first listing. Check to
            // see whether the listing was truncated. If so, retrieve the next page of objects
            // and delete them.
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

        // Delete all object versions (required for versioned buckets).
        VersionListing versionList = s3Client.listVersions(new ListVersionsRequest().withBucketName(photoStorageBucket));
        while (true) {
            Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
            while (versionIter.hasNext()) {
                S3VersionSummary vs = versionIter.next();
                s3Client.deleteVersion(photoStorageBucket, vs.getKey(), vs.getVersionId());
            }

            if (versionList.isTruncated()) {
                versionList = s3Client.listNextBatchOfVersions(versionList);
            } else {
                break;
            }
        }
    }

    private String getCoverName(String albumName) {
        return format("covers/%s", albumName);
    }

}

