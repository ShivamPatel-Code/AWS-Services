package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.S3Service;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
@RestController
@RequestMapping("/aws/s3")
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/buckets")
    public List<String> listBuckets(){
        return s3Service.listBuckets();
    }

    @GetMapping("/objects")
    public ResponseEntity<List<String>> listObjects(@RequestParam String bucketName) {
        List<String> objects = s3Service.listObjects(bucketName);
        return ResponseEntity.ok(objects);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> addFile(@RequestParam("bucketName") String bucketName,
                                          @RequestParam("key") String key,
                                          @RequestParam("file") MultipartFile file) {
        try{
            s3Service.uploadFile(bucketName, key, file);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/object")
    public ResponseEntity<String> addFile(@RequestParam("bucketName") String bucketName,
                                          @RequestParam("key") String key){

            try {
                s3Service.deleteObject(bucketName, key);
                return ResponseEntity.ok("File uploaded successfully.");
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error delete file: " + e.getMessage());
            }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String bucketName,
                                               @RequestParam String key) {
        byte[] fileData = s3Service.downloadFile(bucketName, key);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // Optionally, you can set the content-disposition header to prompt download
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"");
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @PostMapping("/copy")
    public ResponseEntity<String> copyObject(@RequestBody Map<String, String> payload) {
        String sourceBucket = payload.get("sourceBucket");
        String sourceKey = payload.get("sourceKey");
        String destinationBucket = payload.get("destinationBucket");
        String destinationKey = payload.get("destinationKey");
        s3Service.copyObject(sourceBucket, sourceKey, destinationBucket, destinationKey);
        return ResponseEntity.ok("Object copied successfully.");
    }
}
