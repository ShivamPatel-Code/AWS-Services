package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final Region region;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, @Value("${aws.region}") String regionStr, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.region = Region.of(regionStr);
        this.s3Presigner = s3Presigner;
    }

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

    // Generate a pre-signed URL valid for a specified duration (in seconds)
    public ResponseEntity<?> generatePresignedUrl(String bucketName, String key, long expirationSeconds) {
        // Create an S3Presigner instance
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedGetObjectRequest.url().toString();
            return ResponseEntity.ok(url);
        } catch (HttpServerErrorException.InternalServerError e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating a pre-signed url: " + e.getMessage());
        }
    }
}
