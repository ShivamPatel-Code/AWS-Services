package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for interacting with Amazon S3.
 * Provides methods to list buckets, list objects in a bucket,
 * upload files, delete objects, download files, copy objects, and generate pre-signed URLs.
 */
@Service
@AllArgsConstructor
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * Lists all S3 buckets in the account.
     *
     * @return A list of bucket names.
     */
    public List<String> listBuckets() {
        try {
            ListBucketsResponse bucketsResponse = s3Client.listBuckets();
            List<String> buckets = bucketsResponse.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
            logger.info("Listed {} buckets.", buckets.size());
            return buckets;
        } catch (S3Exception e) {
            logger.error("Error listing buckets: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to list buckets", e);
        }
    }

    /**
     * Lists objects within the specified bucket.
     *
     * @param bucketName The name of the bucket.
     * @return A list of object keys.
     */
    public List<String> listObjects(String bucketName) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            List<String> objects = response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
            logger.info("Found {} objects in bucket '{}'.", objects.size(), bucketName);
            return objects;
        } catch (S3Exception e) {
            logger.error("Error listing objects in bucket {}: {}", bucketName, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to list objects in bucket " + bucketName, e);
        }
    }

    /**
     * Uploads a file to the specified S3 bucket.
     *
     * @param bucketName The target bucket name.
     * @param key        The key under which the file will be stored.
     * @param file       The file to upload.
     * @throws IOException if file reading fails.
     */
    public void uploadFile(String bucketName, String key, MultipartFile file) throws IOException {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("Uploaded file '{}' to bucket '{}'.", key, bucketName);
        } catch (S3Exception e) {
            logger.error("Error uploading file '{}' to bucket {}: {}", key, bucketName, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Deletes an object from the specified S3 bucket.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to delete.
     */
    public void deleteObject(String bucketName, String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            logger.info("Deleted object '{}' from bucket '{}'.", key, bucketName);
        } catch (S3Exception e) {
            logger.error("Error deleting object '{}' from bucket {}: {}", key, bucketName, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    /**
     * Downloads an object from the specified S3 bucket.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to download.
     * @return The file data as a byte array.
     */
    public byte[] downloadFile(String bucketName, String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            byte[] data = s3Client.getObjectAsBytes(request).asByteArray();
            logger.info("Downloaded object '{}' from bucket '{}'.", key, bucketName);
            return data;
        } catch (S3Exception e) {
            logger.error("Error downloading object '{}' from bucket {}: {}", key, bucketName, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to download object", e);
        }
    }

    /**
     * Copies an object from a source location to a destination.
     *
     * @param sourceBucket      The source bucket name.
     * @param sourceKey         The key of the source object.
     * @param destinationBucket The destination bucket name.
     * @param destinationKey    The key for the new object.
     */
    public void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        try {
            CopyObjectRequest request = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destinationBucket)
                    .destinationKey(destinationKey)
                    .build();
            s3Client.copyObject(request);
            logger.info("Copied object from {}/{} to {}/{}.", sourceBucket, sourceKey, destinationBucket, destinationKey);
        } catch (S3Exception e) {
            logger.error("Error copying object from {}/{} to {}/{}: {}", sourceBucket, sourceKey, destinationBucket, destinationKey, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to copy object", e);
        }
    }

    /**
     * Generates a pre-signed URL for downloading an object, valid for the specified duration.
     *
     * @param bucketName        The name of the bucket.
     * @param key               The key of the object.
     * @param expirationSeconds The duration (in seconds) for which the URL is valid.
     * @return A ResponseEntity containing the URL or an error message.
     */
    public ResponseEntity<?> generatePresignedUrl(String bucketName, String key, long expirationSeconds) {
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
            logger.info("Generated pre-signed URL for object '{}' in bucket '{}'.", key, bucketName);
            return ResponseEntity.ok(url);
        } catch (S3Exception e) {
            logger.error("Unexpected error generating pre-signed URL for object '{}' in bucket {}: {}", key, bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to generate pre-signed URL.", e);
        }
    }
}