package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.S3Service;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Amazon S3 operations.
 * Provides endpoints to list buckets, list objects in a bucket,
 * upload files, delete objects, download files, copy objects, and generate pre-signed URLs.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/aws/s3")
public class S3Controller {

    private static final Logger logger = LoggerFactory.getLogger(S3Controller.class);
    private final S3Service s3Service;

    /**
     * Retrieves a list of all S3 buckets.
     *
     * @return A list of bucket names.
     */
    @GetMapping("/buckets")
    public ResponseEntity<List<String>> listBuckets() {
        try {
            List<String> buckets = s3Service.listBuckets();
            logger.info("Retrieved {} buckets.", buckets.size());
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            logger.error("Error listing buckets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a list of objects within a specified bucket.
     *
     * @param bucketName The name of the bucket.
     * @return A ResponseEntity containing a list of object keys.
     */
    @GetMapping("/objects")
    public ResponseEntity<List<String>> listObjects(@RequestParam String bucketName) {
        try {
            List<String> objects = s3Service.listObjects(bucketName);
            logger.info("Retrieved {} objects from bucket '{}'.", objects.size(), bucketName);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            logger.error("Error listing objects in bucket {}: {}", bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Uploads a file to the specified S3 bucket.
     *
     * @param bucketName The target bucket name.
     * @param key        The key under which the file will be stored.
     * @param file       The file to upload.
     * @return A ResponseEntity with a status message.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("bucketName") String bucketName,
                                             @RequestParam("key") String key,
                                             @RequestParam("file") MultipartFile file) {
        try {
            s3Service.uploadFile(bucketName, key, file);
            logger.info("File '{}' uploaded successfully to bucket '{}'.", key, bucketName);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (IOException e) {
            logger.error("IO error uploading file '{}' to bucket {}: {}", key, bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error uploading file '{}' to bucket {}: {}", key, bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    /**
     * Deletes an object from the specified S3 bucket.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to delete.
     * @return A ResponseEntity with a status message.
     */
    @DeleteMapping("/delete/object")
    public ResponseEntity<String> deleteObject(@RequestParam("bucketName") String bucketName,
                                               @RequestParam("key") String key) {
        try {
            s3Service.deleteObject(bucketName, key);
            logger.info("Object '{}' deleted successfully from bucket '{}'.", key, bucketName);
            return ResponseEntity.ok("Object deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting object '{}' from bucket {}: {}", key, bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting file: " + e.getMessage());
        }
    }

    /**
     * Downloads an object from the specified S3 bucket.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to download.
     * @return A ResponseEntity containing the file data as a byte array.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String bucketName,
                                               @RequestParam String key) {
        try {
            byte[] fileData = s3Service.downloadFile(bucketName, key);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"");
            logger.info("File '{}' from bucket '{}' downloaded successfully.", key, bucketName);
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error downloading file '{}' from bucket {}: {}", key, bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Copies an object from one location to another within S3.
     *
     * @param payload A map containing sourceBucket, sourceKey, destinationBucket, and destinationKey.
     * @return A ResponseEntity with a status message.
     */
    @PostMapping("/copy")
    public ResponseEntity<String> copyObject(@RequestBody Map<String, String> payload) {
        try {
            String sourceBucket = payload.get("sourceBucket");
            String sourceKey = payload.get("sourceKey");
            String destinationBucket = payload.get("destinationBucket");
            String destinationKey = payload.get("destinationKey");
            s3Service.copyObject(sourceBucket, sourceKey, destinationBucket, destinationKey);
            logger.info("Object copied from {}/{} to {}/{} successfully.", sourceBucket, sourceKey, destinationBucket, destinationKey);
            return ResponseEntity.ok("Object copied successfully.");
        } catch (Exception e) {
            logger.error("Error copying object: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error copying object: " + e.getMessage());
        }
    }

    /**
     * Generates a pre-signed URL for downloading an object.
     *
     * @param bucketName        The name of the bucket.
     * @param key               The key of the object.
     * @param expirationSeconds The URL expiration time in seconds (default is 300 seconds).
     * @return A ResponseEntity containing the pre-signed URL or an error message.
     */
    @GetMapping("/presignedUrl")
    public ResponseEntity<?> generatePresignedUrl(@RequestParam String bucketName,
                                                  @RequestParam String key,
                                                  @RequestParam(defaultValue = "300") long expirationSeconds) {
        try {
            ResponseEntity<?> response = s3Service.generatePresignedUrl(bucketName, key, expirationSeconds);
            logger.info("Pre-signed URL generated successfully for object '{}' in bucket '{}'.", key, bucketName);
            return response;
        } catch (Exception e) {
            logger.error("Error generating pre-signed URL for object '{}' in bucket {}: {}", key, bucketName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating pre-signed URL: " + e.getMessage());
        }
    }
}