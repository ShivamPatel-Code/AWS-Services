package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    public List<String> listBuckets(){
        ListBucketsResponse bucketsResponse = s3Client.listBuckets();
        return bucketsResponse.buckets().stream()
                .map(Bucket::name)
                .collect(Collectors.toList());

    }

    public List<String> listObjects(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    public void uploadFile(String bucketName, String key, MultipartFile file) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
    }

    public void deleteObject(String bucketName, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    public byte[] downloadFile(String bucketName, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    public void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(destinationBucket)
                .destinationKey(destinationKey)
                .build();
        s3Client.copyObject(request);
    }

}
