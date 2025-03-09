package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/aws")
public class AwsController {

    private final S3Service s3Service;

    @GetMapping("/s3/buckets")
    public List<String> listBuckets(){
        return s3Service.listBuckets();
    }
}
