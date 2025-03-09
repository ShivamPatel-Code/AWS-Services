package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/aws/s3")
public class AwsController {

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
}
