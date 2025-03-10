package com.cloud.aws_services.controller;

import com.cloud.aws_services.service.SqsService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling SQS operations.
 * Exposes endpoints for sending, receiving, and deleting messages
 */
@AllArgsConstructor
@RestController
@RequestMapping("/aws/sqs")
public class SqsController {

    private static final Logger logger = LoggerFactory.getLogger(SqsController.class);
    private final SqsService sqsService;

    /**
     * Endpoint to send a message to an SQS queue.
     *
     * @param queueUrl The URL of the SQS queue.
     * @param message  The message body.
     * @return A confirmation message with the sent message ID.
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String queueUrl,
                                              @RequestParam String message) {
        try {
            String messageId = sqsService.sendMessage(queueUrl, message);
            String responseMsg = "Message sent. Message ID: " + messageId;
            logger.info(responseMsg);
            return ResponseEntity.ok(responseMsg);
        } catch (Exception e) {
            logger.error("Error in sendMessage endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Endpoint to receive messages from an SQS queue.
     *
     * @param queueUrl        The URL of the SQS queue.
     * @param maxMessages     Maximum number of messages to receive.
     * @param waitTimeSeconds Wait time for long polling.
     * @return A list of formatted message details.
     */
    @GetMapping("/receive")
    public ResponseEntity<List<String>> receiveMessages(@RequestParam String queueUrl,
                                                        @RequestParam(defaultValue = "10") int maxMessages,
                                                        @RequestParam(defaultValue = "10") int waitTimeSeconds) {
        try {
            List<Message> messages = sqsService.receiveMessages(queueUrl, maxMessages, waitTimeSeconds);
            List<String> response = messages.stream()
                    .map(m -> "MessageId: " + m.messageId() +
                            ", Body: " + m.body() +
                            ", ReceiptHandle: " + m.receiptHandle())
                    .collect(Collectors.toList());
            logger.info("Returning {} messages from queue {}", response.size(), queueUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in receiveMessages endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Endpoint to delete a message from an SQS queue.
     *
     * @param payload       The receiptHandle has certain characters
     *                      which easier to read in json format from payload
     *
     *      Expecting a map containing:
     *          queueUrl: The URL of the SQS queue.
     *          receiptHandle: The receipt handle of the message.
     * @return A confirmation message.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMessage(@RequestBody Map<String, String> payload) {
        String queueUrl = payload.get("queueUrl");
        String receiptHandle = payload.get("receiptHandle");
        sqsService.deleteMessage(queueUrl, receiptHandle);
        return ResponseEntity.ok("Message deleted successfully.");
    }
}

