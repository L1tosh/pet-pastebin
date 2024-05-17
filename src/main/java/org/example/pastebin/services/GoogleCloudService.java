package org.example.pastebin.services;

import com.google.cloud.storage.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class GoogleCloudService {

    private final String bucketName;
    private final Storage storage;

    public GoogleCloudService(@Value("${spring.cloud.gcp.storage.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String generateHash(String content) {
        return DigestUtils.sha256Hex(content);
    }

    public String downloadFile(String hash) {
        Blob blob = storage.get(BlobId.of(bucketName, hash));
        return new String(blob.getContent());
    }

    public void uploadFile(String hash, String text) {
        BlobId blobId = BlobId.of(bucketName, hash);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();

        storage.create(blobInfo, text.getBytes(StandardCharsets.UTF_8));
    }

    public boolean deleteFile(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        return storage.delete(blobId);
    }

}
