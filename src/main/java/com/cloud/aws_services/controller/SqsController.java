package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.SqsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/aws/sqs")
public class SqsController {

    private final SqsService sqsService;

    // Send a message
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String queueUrl,
                                              @RequestParam String message) {
        String messageId = String.valueOf(sqsService.sendMessage(queueUrl, message));
        return ResponseEntity.ok("Message sent. Message ID: " + messageId);
    }

    // Receive messages
    @GetMapping("/receive")
    public ResponseEntity<List<String>> receiveMessages(@RequestParam String queueUrl,
                                                        @RequestParam(defaultValue = "10") int maxMessages,
                                                        @RequestParam(defaultValue = "10") int waitTimeSeconds) {
        List<Message> messages = sqsService.receiveMessages(queueUrl, maxMessages, waitTimeSeconds);
        List<String> response = messages.stream()
                .map(m -> "MessageId: " + m.messageId() + ", Body: " + m.body() + ", ReceiptHandle: " + m.receiptHandle())
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Delete a message
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMessage(@RequestBody Map<String, String> payload) {
        String queueUrl = payload.get("queueUrl");
        String receiptHandle = payload.get("receiptHandle");
        sqsService.deleteMessage(queueUrl, receiptHandle);
        return ResponseEntity.ok("Message deleted successfully.");
    }
}

