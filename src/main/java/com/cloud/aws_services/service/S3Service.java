package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
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

    public void uploadFile(String bucketName, String key, Path filePath){
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), filePath);
    }

}
